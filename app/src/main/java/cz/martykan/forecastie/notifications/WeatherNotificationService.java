package cz.martykan.forecastie.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import java.util.concurrent.Executors;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.activities.MainActivity;
import cz.martykan.forecastie.models.WeatherPresentation;
import cz.martykan.forecastie.notifications.repository.WeatherRepository;
import cz.martykan.forecastie.notifications.ui.NotificationContentUpdater;
import cz.martykan.forecastie.notifications.ui.NotificationContentUpdaterFactory;
import cz.martykan.forecastie.utils.formatters.WeatherFormatterType;

/**
 * Service for showing and updating notification.
 */
public class WeatherNotificationService extends Service {
    public static int WEATHER_NOTIFICATION_ID = 1;
    private static final String WEATHER_NOTIFICATION_CHANNEL_ID = "weather_notification_channel";

    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notification;

    private NotificationContentUpdater contentUpdater;
    private WeatherRepository repository;
    private WeatherRepository.RepositoryListener repositoryListener;

    @Override
    public void onCreate() {
        createNotificationChannelIfNeeded(this);
        notificationManager = NotificationManagerCompat.from(this);

        PendingIntent pendingIntent = getNotificationTapPendingIntent();
        configureNotification(pendingIntent);

        startForeground(WEATHER_NOTIFICATION_ID, notification.build());

        repository = new WeatherRepository(this, Executors.newSingleThreadExecutor());
        repositoryListener = new WeatherRepository.RepositoryListener() {
            @Override
            public void onChange(@NonNull WeatherPresentation newData) {
                Log.e("f", "RepositoryListener: " + newData.toString());
                updateNotification(newData);
            }
        };
        repository.observeWeather(repositoryListener);
    }

    // catch update of system's dark theme flags
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (repository != null) {
            updateNotification(repository.getWeather());
        }
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

        if (repository != null) {
            repositoryListener = null;
            repository.clear();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Put data into notification.
     */
    private void updateNotification(@NonNull WeatherPresentation weatherPresentation) {
        Log.e("f", "notification update: " + weatherPresentation.toString());
        NotificationContentUpdater updater = getContentUpdater(weatherPresentation.getType());
        if (updater.isLayoutCustom()) {
            RemoteViews layout = updater.prepareRemoteView(this);
            updater.updateNotification(weatherPresentation, notification, layout, this);
        } else {
            updater.updateNotification(weatherPresentation, notification, this);
        }

        notificationManager.notify(WEATHER_NOTIFICATION_ID, notification.build());
    }

    private synchronized NotificationContentUpdater getContentUpdater(
            @NonNull WeatherFormatterType type
    ) {
        if (contentUpdater == null
                || !NotificationContentUpdaterFactory.doesContentUpdaterMatchType(type, contentUpdater)) {
            contentUpdater = NotificationContentUpdaterFactory.createNotificationContentUpdater(type);
        }
        return contentUpdater;
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
}