package cz.martykan.forecastie.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

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

    private String typeKey;
    private String typeDefaultKey;
    private String typeSimpleKey;
    private String typeDefault;

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notification;
    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

    private WeatherFormatterType type = WeatherFormatterType.NOTIFICATION_SIMPLE;
    private NotificationContentUpdater contentUpdater;

    @Override
    public void onCreate() {
        prepareSettingsConstants();

        createNotificationChannelIfNeeded(this);
        notificationManager = NotificationManagerCompat.from(this);

        PendingIntent pendingIntent = getNotificationTapPendingIntent();
        configureNotification(pendingIntent);

        startForeground(WEATHER_NOTIFICATION_ID, notification.build());

        readValuesFromStorageAndUpdateNotification();
        observeValuesChanges();
    }

    // catch update of system's dark theme flags
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateNotification();
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
        readNotificationType();
        NotificationContentUpdater updater = getContentUpdater(type);

        String json = prefs.getString("lastToday", "{}");
        long lastUpdate = prefs.getLong("lastUpdate", -1L);
        updater.setWeather(ImmutableWeather.fromJson(json, lastUpdate));
        updater.setRoundedTemperature(prefs.getBoolean("temperatureInteger",
                NotificationContentUpdater.DEFAULT_DO_ROUND_TEMPERATURE));
        updater.setTemperatureUnits(prefs.getString("unit",
                NotificationContentUpdater.DEFAULT_TEMPERATURE_UNITS));
        updater.setWindSpeedUnits(prefs.getString("speedUnit",
                NotificationContentUpdater.DEFAULT_WIND_SPEED_UNITS));
        String windDirectionFormat = prefs.getString("windDirectionFormat",
                NotificationContentUpdater.DEFAULT_WIND_DIRECTION_FORMAT);
        updater.setWindDirectionFormat(windDirectionFormat);
        updater.setPressureUnits(prefs.getString("pressureUnit",
                NotificationContentUpdater.DEFAULT_PRESSURE_UNITS));

        updateNotification();
    }

    /** Retrieve notification type from preferences. */
    private void readNotificationType() {
        String typePref = prefs.getString(typeKey, typeDefault);
        if (typePref != null && typePref.equalsIgnoreCase(typeDefaultKey)) {
            type = WeatherFormatterType.NOTIFICATION_DEFAULT;
        } else if (typePref != null && typePref.equalsIgnoreCase(typeSimpleKey)) {
            type = WeatherFormatterType.NOTIFICATION_SIMPLE;
        } else {
            if (typeDefault == null || typeDefault.equalsIgnoreCase(typeDefaultKey)) {
                type = WeatherFormatterType.NOTIFICATION_DEFAULT;
            } else {
                type = WeatherFormatterType.NOTIFICATION_SIMPLE;
            }
        }
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
        } else if (!NotificationContentUpdaterFactory.doesContentUpdaterMatchType(type, contentUpdater)) {
            contentUpdater = NotificationContentUpdaterFactory.createNotificationContentUpdater(type,
                    contentUpdater);
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
                        boolean roundTemperature = sharedPreferences.getBoolean(key,
                                NotificationContentUpdater.DEFAULT_DO_ROUND_TEMPERATURE);
                        updater.setRoundedTemperature(roundTemperature);
                        break;
                    case "unit":
                        String temperatureUnits = sharedPreferences.getString(key,
                                NotificationContentUpdater.DEFAULT_TEMPERATURE_UNITS);
                        updater.setTemperatureUnits(temperatureUnits);
                        break;
                    case "speedUnit":
                        String windSpeedUnits = sharedPreferences.getString(key,
                                NotificationContentUpdater.DEFAULT_WIND_SPEED_UNITS);
                        updater.setWindSpeedUnits(windSpeedUnits);
                        break;
                    case "windDirectionFormat":
                        String windDirectionFormat = sharedPreferences.getString(key,
                                NotificationContentUpdater.DEFAULT_WIND_DIRECTION_FORMAT);
                        updater.setWindDirectionFormat(windDirectionFormat);
                        break;
                    case "pressureUnit":
                        String pressureUnits = sharedPreferences.getString(key,
                                NotificationContentUpdater.DEFAULT_PRESSURE_UNITS);
                        updater.setPressureUnits(pressureUnits);
                        break;

                    default:
                        needToUpdate = false;
                        break;
                }
                if (key.equalsIgnoreCase(typeKey)) {
                    readNotificationType();
                    needToUpdate = true;
                }
                if (needToUpdate) {
                    updateNotification();
                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    /**
     * Create Notification Channels added in Android O to let a user configure notification
     * per channel and not by app.
     */
    private static void createNotificationChannelIfNeeded(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String name = context.getString(R.string.channel_name);
            NotificationChannel channel = new NotificationChannel(WEATHER_NOTIFICATION_CHANNEL_ID,
                    name, NotificationManager.IMPORTANCE_LOW);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setShowBadge(false);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Update Notification Channels if it has been created.
     * @param context Android context
     */
    public static void updateNotificationChannelIfNeeded(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);
            if (notificationManager != null
                    && notificationManager.getNotificationChannel(WEATHER_NOTIFICATION_CHANNEL_ID) != null) {
                createNotificationChannelIfNeeded(context);
            }
        }
    }

    /**
     * Start foreground service to show and update weather notification.
     *
     * @param context context to create {@link Intent}
     */
    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, WeatherNotificationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    /**
     * Stop service and hide notification.
     *
     * @param context Android context
     */
    public static void stop(@NonNull Context context) {
        Intent intent = new Intent(context, WeatherNotificationService.class);
        context.stopService(intent);
    }

    private void prepareSettingsConstants() {
        typeKey = getString(R.string.settings_notification_type_key);
        typeDefaultKey = getString(R.string.settings_notification_type_key_default);
        typeSimpleKey = getString(R.string.settings_notification_type_key_simple);
        typeDefault = getString(R.string.settings_notification_type_default_value);
    }
}