package cz.martykan.forecastie.utils.formatters;

import androidx.annotation.NonNull;

/**
 * Factory for creating formatters by type.
 */
public abstract class WeatherFormatterFactory {
    /**
     * Create formatter for specified type.
     * @param type type of formatter
     * @return new formatter
     */
    @NonNull
    public static WeatherFormatter createFormatter(@NonNull WeatherFormatterType type) {
        switch (type) {
            case NOTIFICATION_DEFAULT:
                return new WeatherDefaultNotificationFormatter();
            case NOTIFICATION_SIMPLE:
                return new WeatherSimpleNotificationFormatter();
            default:
                throw new IllegalArgumentException("Unknown type " + type);
        }
    }
}
