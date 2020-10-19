package cz.martykan.forecastie.utils.formatters;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;

import cz.martykan.forecastie.models.ImmutableWeather;

/**
 * Formatter for temperature.
 * <br/>
 * Format temperature with units like: 15.3K, 12°C
 */
// TODO rid off static and use DI
public abstract class TemperatureFormatter {
    /**
     * Returns temperature with units.
     * @param weather weather info
     * @param temperatureUnit temperature units
     * @param roundedTemperature if {@code true} round temperature and show as integer
     * @return temperature with units
     * @throws NullPointerException if any of parameters is null
     */
    @NonNull
    public static String getTemperature(@NonNull ImmutableWeather weather,
                                        @NonNull String temperatureUnit,
                                        boolean roundedTemperature
    ) throws NullPointerException {
        //noinspection ConstantConditions
        if (weather == null)
            throw new NullPointerException("weather should not be null");
        //noinspection ConstantConditions
        if (temperatureUnit == null)
            throw new NullPointerException("temperatureUnit should not be null");

        String temperature = roundedTemperature
                ? String.valueOf(weather.getRoundedTemperature(temperatureUnit))
                : new DecimalFormat("0.#").format(weather.getTemperature(temperatureUnit));
        return temperature + temperatureUnit;
    }
}
