package cz.martykan.forecastie.notifications;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.utils.formatters.WeatherFormatter;
import cz.martykan.forecastie.utils.formatters.WeatherSimpleNotificationFormatter;

public class SimpleNotificationContentUpdater extends NotificationContentUpdater {
    private WeatherFormatter formatter;

    private boolean roundedTemperature = false;
    private String temperatureUnits = "°C";
    private String windSpeedUnits = "m/s";
    private String windDirectionFormat = "arrow";
    private String pressureUnits = "hPa/mBar";

    public SimpleNotificationContentUpdater(@NonNull WeatherFormatter formatter) {
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
    public void setWindSpeedUnits(@NonNull String windSpeedUnits) throws NullPointerException {
        //noinspection ConstantConditions
        if (windSpeedUnits == null)
            throw new NullPointerException("windSpeedUnits is null");

        this.windSpeedUnits = windSpeedUnits;
    }

    @Override
    public void setWindDirectionFormat(@NonNull String windDirectionFormat)
            throws NullPointerException {
        //noinspection ConstantConditions
        if (windDirectionFormat == null)
            throw new NullPointerException("windDirectionFormat is null");

        this.windDirectionFormat = windDirectionFormat;
    }

    @Override
    public void setPressureUnits(@NonNull String pressureUnits) throws NullPointerException {
        //noinspection ConstantConditions
        if (pressureUnits == null)
            throw new NullPointerException("pressureUnits is null");

        this.pressureUnits = pressureUnits;
    }

    @Override
    public void updateNotification(@NonNull NotificationCompat.Builder notification,
                            @NonNull RemoteViews notificationLayout,
                            @NonNull Context context
    ) throws NullPointerException {
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

        if (formatter.isEnoughValidData(weather)) {
            setTemperatureAndDescription(notificationLayout);

            notificationLayout.setViewVisibility(R.id.icon, View.VISIBLE);
            Bitmap weatherIcon = formatter.getWeatherIconAsBitmap(weather, context);
            notificationLayout.setImageViewBitmap(R.id.icon, weatherIcon);

            String wind = formatter.getWind(weather, windSpeedUnits, windDirectionFormat, context);
            notificationLayout.setTextViewText(R.id.wind, wind);

            String pressure = formatter.getPressure(weather, pressureUnits, context);
            notificationLayout.setTextViewText(R.id.pressure, pressure);

            String humidity = formatter.getHumidity(weather, context);
            notificationLayout.setTextViewText(R.id.humidity, humidity);
        } else {
            if (formatter instanceof WeatherSimpleNotificationFormatter
                    && ((WeatherSimpleNotificationFormatter) formatter).isEnoughValidMainData(weather)
            ) {
                setTemperatureAndDescription(notificationLayout);
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

    private void setTemperatureAndDescription(@NonNull RemoteViews notificationLayout) {
        String temperature = formatter.getTemperature(weather, temperatureUnits,
                roundedTemperature);
        notificationLayout.setTextViewText(R.id.temperature, temperature);

        String description = formatter.getDescription(weather);
        notificationLayout.setTextViewText(R.id.description, description);
    }

    @Override
    public RemoteViews prepareRemoteView(@NonNull Context context) throws NullPointerException {
        return new RemoteViews(context.getPackageName(), R.layout.notification_simple);
    }
}