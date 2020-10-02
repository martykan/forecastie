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
 * <br/>
 * NOTE: default implementation for all methods except {@link #setWeather(ImmutableWeather)} is
 * to do nothing.
 */
@SuppressWarnings("SameParameterValue")
public abstract class NotificationContentUpdater {
    /** Weather information. */
    protected ImmutableWeather weather = ImmutableWeather.EMPTY;

    /**
     * Set is temperature should be rounded to integer.
     * @param isRoundedTemperature {@code true} for rounding and {@code false} for not.
     */
    public void setRoundedTemperature(boolean isRoundedTemperature) {

    }

    /**
     * Set temperature units.
     * @param temperatureUnits temperature units.
     * @throws NullPointerException if {@code temperatureUnits} is null
     */
    public void setTemperatureUnits(@NonNull String temperatureUnits) throws NullPointerException {

    }

    /**
     * Set wind speed units.
     * @param windSpeedUnits wind speed units.
     * @throws NullPointerException if {@code windSpeedUnits} is null
     */
    public void setWindSpeedUnits(@NonNull String windSpeedUnits) throws NullPointerException {

    }

    /**
     * Set wind direction format.
     * @param windDirectionFormat wind direction format.
     * @throws NullPointerException if {@code windDirectionFormat} is null
     */
    public void setWindDirectionFormat(@NonNull String windDirectionFormat) throws NullPointerException {

    }

    /**
     * Set pressure units.
     * @param pressureUnits pressure units.
     * @throws NullPointerException if {@code pressureUnits} is null
     */
    public void setPressureUnits(@NonNull String pressureUnits) throws NullPointerException {

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
