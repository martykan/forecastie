package cz.martykan.forecastie.notifications;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.utils.formatters.WeatherFormatter;

/**
 * Update notification content for default notification view.
 */
public class DefaultNotificationContentUpdater extends NotificationContentUpdater {
    private WeatherFormatter formatter;

    private boolean roundedTemperature = false;
    private String temperatureUnits = "°C";

    public DefaultNotificationContentUpdater(@NonNull WeatherFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void setRoundedTemperature(boolean isRoundedTemperature) {
        roundedTemperature = isRoundedTemperature;
    }

    @Override
    public void setTemperatureUnits(@NonNull String temperatureUnits) throws NullPointerException {
        //noinspection ConstantConditions
        if (temperatureUnits == null)
            throw new NullPointerException("temperatureUnits is null");

        this.temperatureUnits = temperatureUnits;
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
