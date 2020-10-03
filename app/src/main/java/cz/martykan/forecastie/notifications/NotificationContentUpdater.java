package cz.martykan.forecastie.notifications;

import android.content.Context;
import android.widget.RemoteViews;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import cz.martykan.forecastie.models.ImmutableWeather;

/**
 * Update notification content for some view. View is specified in children.
 * <br/>
 * Setters need to store values. Notification only updates by invoking
 * {@link #updateNotification(NotificationCompat.Builder, Context)}.
 */
@SuppressWarnings("SameParameterValue")
public abstract class NotificationContentUpdater {
    // TODO maybe these constant should be moved into ImmutableWeather
    /** Do not round temperature by default. */
    public static final boolean DEFAULT_DO_ROUND_TEMPERATURE = false;
    /** Default temperature unit is Celsius. */
    public static final String DEFAULT_TEMPERATURE_UNITS = "°C";
    /** Default wind speed unit is meters per second. */
    public static final String DEFAULT_WIND_SPEED_UNITS = "m/s";
    /** Default wind direction format is arrows. */
    public static final String DEFAULT_WIND_DIRECTION_FORMAT = "arrow";
    /** Default pressure units is hPa/mBar. */
    public static final String DEFAULT_PRESSURE_UNITS = "hPa/mBar";

    protected boolean roundedTemperature = DEFAULT_DO_ROUND_TEMPERATURE;
    protected String temperatureUnits = DEFAULT_TEMPERATURE_UNITS;
    protected String windSpeedUnits = DEFAULT_WIND_SPEED_UNITS;
    protected String windDirectionFormat = DEFAULT_WIND_DIRECTION_FORMAT;
    protected String pressureUnits = DEFAULT_PRESSURE_UNITS;
    /** Weather information. */
    protected ImmutableWeather weather = ImmutableWeather.EMPTY;

    /**
     * Set is temperature should be rounded to integer.
     * @param roundTemperature {@code true} for rounding and {@code false} for not.
     */
    public void setRoundedTemperature(boolean roundTemperature) {
        roundedTemperature = roundTemperature;
    }

    /**
     * Set temperature units.
     * @param temperatureUnits temperature units.
     * @throws NullPointerException if {@code temperatureUnits} is null
     */
    public void setTemperatureUnits(@NonNull String temperatureUnits) throws NullPointerException {
        //noinspection ConstantConditions
        if (temperatureUnits == null)
            throw new NullPointerException("temperatureUnits is null");

        this.temperatureUnits = temperatureUnits;
    }

    /**
     * Set wind speed units.
     * @param windSpeedUnits wind speed units.
     * @throws NullPointerException if {@code windSpeedUnits} is null
     */
    public void setWindSpeedUnits(@NonNull String windSpeedUnits) throws NullPointerException {
        //noinspection ConstantConditions
        if (windSpeedUnits == null)
            throw new NullPointerException("windSpeedUnits is null");

        this.windSpeedUnits = windSpeedUnits;
    }

    /**
     * Set wind direction format.
     * @param windDirectionFormat wind direction format.
     * @throws NullPointerException if {@code windDirectionFormat} is null
     */
    public void setWindDirectionFormat(@NonNull String windDirectionFormat)
            throws NullPointerException {
        //noinspection ConstantConditions
        if (windDirectionFormat == null)
            throw new NullPointerException("windDirectionFormat is null");

        this.windDirectionFormat = windDirectionFormat;
    }

    /**
     * Set pressure units.
     * @param pressureUnits pressure units.
     * @throws NullPointerException if {@code pressureUnits} is null
     */
    public void setPressureUnits(@NonNull String pressureUnits) throws NullPointerException {
        //noinspection ConstantConditions
        if (pressureUnits == null)
            throw new NullPointerException("pressureUnits is null");

        this.pressureUnits = pressureUnits;
    }

    /**
     * Set weather information.
     * @param weather weather information.
     * @throws NullPointerException if {@code pressureUnits} is null
     */
    @CallSuper
    public void setWeather(@NonNull ImmutableWeather weather) throws NullPointerException {
        //noinspection ConstantConditions
        if (weather == null)
            throw new NullPointerException("weather is null");

        this.weather = weather;
    }

    /**
     * Copy properties from another content updater.
     * @param contentUpdater content updater
     */
    void copyPreviousValues(@NonNull NotificationContentUpdater contentUpdater) {
        roundedTemperature = contentUpdater.roundedTemperature;
        temperatureUnits = contentUpdater.temperatureUnits;
        windSpeedUnits = contentUpdater.windSpeedUnits;
        windDirectionFormat = contentUpdater.windDirectionFormat;
        pressureUnits = contentUpdater.pressureUnits;
        weather = contentUpdater.weather;
    }

    /**
     * Update notification with saved data and default view.
     * @param notification notification to update.
     * @param context android context.
     * @throws NullPointerException if any of parameters are null
     */
    public void updateNotification(@NonNull NotificationCompat.Builder notification,
                            @NonNull Context context
    ) throws NullPointerException {
        throw new UnsupportedOperationException("updateNotification hasn't been implemented");
    }

    public RemoteViews prepareRemoteView(@NonNull Context context) throws NullPointerException {
        throw new UnsupportedOperationException("updateNotification hasn't been implemented");
    }

    /**
     * Update notification with saved data and custom view returned by
     * {@link #prepareRemoteView(Context)}.
     * @param notification notification to update.
     * @param notificationLayout custom notification layout.
     * @param context android context.
     * @throws NullPointerException if any of parameters are null
     */
    public void updateNotification(@NonNull NotificationCompat.Builder notification,
                            @NonNull RemoteViews notificationLayout,
                            @NonNull Context context)
            throws NullPointerException {
        throw new UnsupportedOperationException("updateNotification hasn't been implemented");
    }
}
