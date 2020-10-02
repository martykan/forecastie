package cz.martykan.forecastie.utils.formatters;

import androidx.annotation.NonNull;

import cz.martykan.forecastie.models.ImmutableWeather;

/**
 * Formatter for weather description.
 */
// TODO rid off static and use DI
public abstract class DescriptionFormatter {
    /**
     * Returns weather description with first uppercase letter.
     * @param weather weather info
     * @return weather description with first uppercase letter
     * @throws NullPointerException if {@code weather} is null
     */
    @NonNull
    public static String getDescription(@NonNull ImmutableWeather weather)
            throws NullPointerException {
        //noinspection ConstantConditions
        if (weather == null)
            throw new NullPointerException("weather should not be null");

        String description = weather.getDescription();
        String result;
        if (description.isEmpty())
            result = description;
        else
            result = description.substring(0,1).toUpperCase() + description.substring(1);
        return result;
    }
}
