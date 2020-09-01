package cz.martykan.forecastie.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.activities.MainActivity;
import cz.martykan.forecastie.models.ImmutableWeather;
import cz.martykan.forecastie.notifications.NotificationContentUpdater;
import cz.martykan.forecastie.notifications.NotificationContentUpdaterFactory;
import cz.martykan.forecastie.utils.formatters.WeatherFormatterType;

/**
 * Service for showing and updating notification.
 */
public class WeatherNotificationService extends Service {

    public static int WEATHER_NOTIFICATION_ID = 1;
    private static String WEATHER_NOTIFICATION_CHANNEL_ID = "weather_notification_channel";

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notification;
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

    // TODO move constants in some another place to remove code duplication
    private final boolean DEFAULT_DO_ROUND_TEMPERATURE = false;
    private final String DEFAULT_TEMPERATURE_UNITS = "°C";
    private final String DEFAULT_WIND_SPEED_UNITS = "m/s";
    private final String DEFAULT_WIND_DIRECTION_FORMAT = "none";
    private final String DEFAULT_PRESSURE_UNITS = "hPa/mBar";

    private WeatherFormatterType type = WeatherFormatterType.NOTIFICATION_SIMPLE;
    private NotificationContentUpdater contentUpdater;

    @Override
    public void onCreate() {
        createNotificationChannelIfNeeded();
        notificationManager = NotificationManagerCompat.from(this);

        PendingIntent pendingIntent = getNotificationTapPendingIntent();
        configureNotification(pendingIntent);

        startForeground(WEATHER_NOTIFICATION_ID, notification.build());

        readValuesFromStorageAndUpdateNotification();
        observeValuesChanges();
    }

    private void configureNotification(PendingIntent pendingIntent) {
        notification = new NotificationCompat.Builder(this, WEATHER_NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.cloud)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notification
                    .setDefaults(0)
                    .setVibrate(null)
                    .setSound(null)
                    .setLights(0, 0, 0);
        }
    }

    /**
     * Create pending intent to open {@link MainActivity}
     * @return pending intent to open {@link MainActivity}
     */
    private PendingIntent getNotificationTapPendingIntent() {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        // avoid duplication of MainActivity: create new MainActivity and close previous if there is one
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(mainActivityIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (notificationManager != null)
            notificationManager.cancel(WEATHER_NOTIFICATION_ID);

        stopForeground(true);

        if (onSharedPreferenceChangeListener != null && prefs != null)
            prefs.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Initialize values from Shared Preferences to show weather info into notification.
     */
    private void readValuesFromStorageAndUpdateNotification() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // TODO read type
        NotificationContentUpdater updater = getContentUpdater(type);

        String json = prefs.getString("lastToday", "{}");
        long lastUpdate = prefs.getLong("lastUpdate", -1L);
        updater.setWeather(ImmutableWeather.fromJson(json, lastUpdate));
        updater.setRoundedTemperature(prefs.getBoolean("temperatureInteger", DEFAULT_DO_ROUND_TEMPERATURE));
        updater.setTemperatureUnits(prefs.getString("unit", DEFAULT_TEMPERATURE_UNITS));
        updater.setWindSpeedUnits(prefs.getString("speedUnit", DEFAULT_WIND_SPEED_UNITS));
        String windDirectionFormat = prefs.getString("windDirectionFormat", this.DEFAULT_WIND_DIRECTION_FORMAT);
        updater.setWindDirectionFormat(windDirectionFormat);
        updater.setPressureUnits(prefs.getString("pressureUnit", DEFAULT_PRESSURE_UNITS));

        updateNotification();
    }

    /**
     * Put data into notification.
     */
    private void updateNotification() {
        WeatherFormatterType type = this.type;
        NotificationContentUpdater updater = getContentUpdater(type);
        if (type == WeatherFormatterType.NOTIFICATION_DEFAULT) {
            updater.updateNotification(notification, this);
        } else {
            RemoteViews notificationLayout = updater.prepareRemoteView(this);
            updater.updateNotification(notification, notificationLayout, this);
        }

        notificationManager.notify(WEATHER_NOTIFICATION_ID, notification.build());
    }

    private synchronized NotificationContentUpdater getContentUpdater(
            @NonNull WeatherFormatterType type
    ) {
        if (contentUpdater == null) {
            contentUpdater = NotificationContentUpdaterFactory.createNotificationContentUpdater(type);
        }
        return contentUpdater;
    }

    /**
     * Observe change in the Shared Preferences and update notification when weather info or
     * settings how to show weather is changed.
     *
     * Implementation Note: Observer pattern is preferable than use of startService with data in
     * Intent when some class updates data because Observer pattern grants us one source of truth.
     */
    private void observeValuesChanges() {
        onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                NotificationContentUpdater updater = getContentUpdater(type);
                boolean needToUpdate = true;
                switch (key) {
                    case "lastUpdate": case "lastToday":
                        String json = sharedPreferences.getString("lastToday", "");
                        if (!json.isEmpty()) {
                            long lastUpdate = prefs.getLong("lastUpdate", -1L);
                            ImmutableWeather weather = ImmutableWeather.fromJson(json, lastUpdate);
                            updater.setWeather(weather);
                        } else {
                            needToUpdate = false;
                        }
                        break;
                    case "temperatureInteger":
                        updater.setRoundedTemperature(
                                sharedPreferences.getBoolean(key, DEFAULT_DO_ROUND_TEMPERATURE)
                        );
                        break;
                    case "unit":
                        updater.setTemperatureUnits(
                                sharedPreferences.getString(key, DEFAULT_TEMPERATURE_UNITS)
                        );
                        break;
                    case "speedUnit":
                        updater.setWindSpeedUnits(
                                sharedPreferences.getString(key, DEFAULT_WIND_SPEED_UNITS)
                        );
                        break;
                    case "windDirectionFormat":
                        updater.setWindDirectionFormat(
                                sharedPreferences.getString(key, DEFAULT_WIND_DIRECTION_FORMAT)
                        );
                        break;
                    case "pressureUnit":
                        updater.setPressureUnits(
                                sharedPreferences.getString(key, DEFAULT_PRESSURE_UNITS)
                        );
                        break;
                    default:
                        needToUpdate = false;
                        break;
                }
                if (needToUpdate)
                    updateNotification();
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    /**
     * Create Notification Channels added in Android O to let a user configure notification
     * per channel and not by app.
     */
    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = getString(R.string.channel_name);
            // TODO add listener for Intent.ACTION_LOCALE_CHANGED to update channel name
            NotificationChannel channel = new NotificationChannel(WEATHER_NOTIFICATION_CHANNEL_ID,
                    name, NotificationManager.IMPORTANCE_LOW);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setShowBadge(false);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null)
                notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Start foreground service to show and update weather notification.
     *
     * @param context context to create {@link Intent}
     */
    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, WeatherNotificationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(intent);
        else
            context.startService(intent);
    }
}