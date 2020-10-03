package cz.martykan.forecastie.notifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.utils.formatters.WeatherFormatter;

/**
 * Update notification content for default notification view.
 */
public class DefaultNotificationContentUpdater extends NotificationContentUpdater {
    private WeatherFormatter formatter;

    public DefaultNotificationContentUpdater(@NonNull WeatherFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void updateNotification(@NonNull NotificationCompat.Builder notification,
                                   @NonNull Context context
    ) throws NullPointerException {
        //noinspection ConstantConditions
        if (notification == null)
            throw new NullPointerException("notification is null");
        //noinspection ConstantConditions
        if (context == null)
            throw new NullPointerException("context is null");

        notification
                .setCustomContentView(null)
                .setContent(null)
                .setCustomBigContentView(null)
                .setColorized(false)
                .setColor(NotificationCompat.COLOR_DEFAULT);

        if (formatter.isEnoughValidData(weather)) {
            String temperature = formatter.getTemperature(weather, temperatureUnits,
                    roundedTemperature);
            notification
                    .setContentTitle(temperature)
                    .setContentText(formatter.getDescription(weather))
                    .setLargeIcon(formatter.getWeatherIconAsBitmap(weather, context));
        } else {
            notification.setContentTitle(context.getString(R.string.no_data))
                    .setContentText(null)
                    .setLargeIcon(null);
        }
    }
}