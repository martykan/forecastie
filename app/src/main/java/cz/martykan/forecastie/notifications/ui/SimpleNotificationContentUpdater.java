package cz.martykan.forecastie.notifications.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.models.WeatherPresentation;
import cz.martykan.forecastie.utils.formatters.WeatherFormatter;
import cz.martykan.forecastie.utils.formatters.WeatherSimpleNotificationFormatter;

public class SimpleNotificationContentUpdater extends NotificationContentUpdater {
    private final WeatherFormatter formatter;

    public SimpleNotificationContentUpdater(@NonNull WeatherFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public boolean isLayoutCustom() {
        return true;
    }

    @NonNull
    @Override
    public RemoteViews prepareRemoteView(@NonNull Context context) throws NullPointerException {
        return new RemoteViews(context.getPackageName(), R.layout.notification_simple);
    }

    @Override
    public void updateNotification(@NonNull WeatherPresentation weatherPresentation,
                                   @NonNull NotificationCompat.Builder notification,
                                   @NonNull RemoteViews notificationLayout,
                                   @NonNull Context context) throws NullPointerException {
        //noinspection ConstantConditions
        if (weatherPresentation == null)
            throw new NullPointerException("weatherPresentation is null");
        //noinspection ConstantConditions
        if (notification == null)
            throw new NullPointerException("notification is null");
        //noinspection ConstantConditions
        if (notificationLayout == null)
            throw new NullPointerException("notificationLayout is null");
        //noinspection ConstantConditions
        if (context == null)
            throw new NullPointerException("context is null");

        notification
                // Too much information for decorated view. Only two strings fit.
                /*.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)*/
                .setContent(notificationLayout)
                .setCustomBigContentView(notificationLayout);

        if (formatter.isEnoughValidData(weatherPresentation.getWeather())) {
            setTemperatureAndDescription(notificationLayout, weatherPresentation);

            notificationLayout.setViewVisibility(R.id.icon, View.VISIBLE);
            Bitmap weatherIcon = formatter.getWeatherIconAsBitmap(weatherPresentation.getWeather(), context);
            notificationLayout.setImageViewBitmap(R.id.icon, weatherIcon);

            String wind = formatter.getWind(weatherPresentation.getWeather(),
                    weatherPresentation.getWindSpeedUnits(),
                    weatherPresentation.getWindDirectionFormat(), context);
            notificationLayout.setTextViewText(R.id.wind, wind);

            String pressure = formatter.getPressure(weatherPresentation.getWeather(),
                    weatherPresentation.getPressureUnits(), context);
            notificationLayout.setTextViewText(R.id.pressure, pressure);

            String humidity = formatter.getHumidity(weatherPresentation.getWeather(), context);
            notificationLayout.setTextViewText(R.id.humidity, humidity);
        } else {
            if (formatter instanceof WeatherSimpleNotificationFormatter
                    && ((WeatherSimpleNotificationFormatter) formatter)
                    .isEnoughValidMainData(weatherPresentation.getWeather())
            ) {
                setTemperatureAndDescription(notificationLayout, weatherPresentation);
            } else {
                notificationLayout.setTextViewText(R.id.temperature, "");
                notificationLayout.setTextViewText(R.id.description, "");
            }
            notificationLayout.setViewVisibility(R.id.icon, View.GONE);
            notificationLayout.setTextViewText(R.id.wind, "");
            notificationLayout.setTextViewText(R.id.pressure, context.getString(R.string.no_data));
            notificationLayout.setTextViewText(R.id.humidity, "");
        }
    }

    private void setTemperatureAndDescription(@NonNull RemoteViews notificationLayout,
                                              @NonNull WeatherPresentation weatherPresentation) {
        String temperature = formatter.getTemperature(weatherPresentation.getWeather(),
                weatherPresentation.getTemperatureUnits(),
                weatherPresentation.isRoundedTemperature());
        notificationLayout.setTextViewText(R.id.temperature, temperature);

        String description = formatter.getDescription(weatherPresentation.getWeather());
        notificationLayout.setTextViewText(R.id.description, description);
    }
}