package cz.martykan.forecastie.notifications.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.IconCompat;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.models.WeatherPresentation;
import cz.martykan.forecastie.utils.formatters.WeatherFormatter;

/**
 * Notification content updater populates notification with data from {@link WeatherPresentation}.
 *
 * If {@link #isLayoutCustom() layout is custom}, {@link #prepareRemoteView(Context)} and
 * {@link #updateNotification(WeatherPresentation, NotificationCompat.Builder, RemoteViews, Context)}
 * should be used. Otherwise use
 * {@link #updateNotification(WeatherPresentation, NotificationCompat.Builder, Context)}.
 */
public abstract class NotificationContentUpdater {
    public static final int DEFAULT_NOTIFICATION_ICON = R.drawable.cloud;

    /**
     * Returns {@code true} if notification has custom layout and {@code false} otherwise.
     * @return {@code true} if notification has custom layout and {@code false} otherwise
     */
    public boolean isLayoutCustom() {
        return false;
    }

    /**
     * Update notification with saved data and default view.
     * @param weatherPresentation data to show.
     * @param notification notification to update.
     * @param context android context.
     * @throws NullPointerException if any of parameters are {@code null}
     */
    public void updateNotification(@NonNull WeatherPresentation weatherPresentation,
                                   @NonNull NotificationCompat.Builder notification,
                                   @NonNull Context context
    ) throws NullPointerException {
        setTemperatureAsIcon(weatherPresentation, notification, context);
    }

    /**
     * Create custom layout for notification.
     * @param context Android context
     * @return custom layout for notification
     * @throws NullPointerException if {@code context} is {@code null}
     */
    // cannot make PowerMock work with Robolectric so have to add updateNotification method
    // with RemoteViews as parameter
    @NonNull
    public RemoteViews prepareRemoteView(@NonNull Context context) throws NullPointerException {
        throw new UnsupportedOperationException("prepareRemoteView is not implemented");
    }

    /**
     * Update notification with saved data and custom view returned by
     * {@link #prepareRemoteView(Context)}.
     * @param weatherPresentation data to show.
     * @param notification notification to update.
     * @param notificationLayout custom notification layout.
     * @param context android context.
     * @throws NullPointerException if any of parameters are {@code null}
     */
    @CallSuper
    public void updateNotification(@NonNull WeatherPresentation weatherPresentation,
                                   @NonNull NotificationCompat.Builder notification,
                                   @NonNull RemoteViews notificationLayout,
                                   @NonNull Context context
    ) throws NullPointerException {
        setTemperatureAsIcon(weatherPresentation, notification, context);
    }

    // TODO add tests
    private void setTemperatureAsIcon(
            @NonNull WeatherPresentation weatherPresentation,
            @NonNull NotificationCompat.Builder notification,
            @NonNull Context context
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final IconCompat icon;
            if (weatherPresentation.shouldShowTemperatureInStatusBar()) {
                int color = ContextCompat.getColor(context, R.color.notification_icon_color);
                Bitmap statusBarIcon = WeatherFormatter.getTemperatureAsBitmap(context,
                        weatherPresentation.getWeather(),
                        weatherPresentation.getTemperatureUnits(),
                        color);
                icon = IconCompat.createWithBitmap(statusBarIcon);
            } else {
                icon = IconCompat.createWithResource(context, DEFAULT_NOTIFICATION_ICON);
            }
            notification.setSmallIcon(icon);
        } else {
            notification.setSmallIcon(DEFAULT_NOTIFICATION_ICON);
        }
    }
}