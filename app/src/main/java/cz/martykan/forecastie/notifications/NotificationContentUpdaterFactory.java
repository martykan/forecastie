package cz.martykan.forecastie.notifications;

import androidx.annotation.NonNull;

import cz.martykan.forecastie.utils.formatters.WeatherFormatter;
import cz.martykan.forecastie.utils.formatters.WeatherFormatterFactory;
import cz.martykan.forecastie.utils.formatters.WeatherFormatterType;

/**
 * Factory for creation notification fontent updaters by type.
 */
public abstract class NotificationContentUpdaterFactory {
    /**
     * Create notification content updater for specified type.
     * @param type type of weather formatter
     * @return notification content updater for {@code type}
     */
    @NonNull
    public static NotificationContentUpdater createNotificationContentUpdater(
            @NonNull WeatherFormatterType type
    ) {
        WeatherFormatter formatter = WeatherFormatterFactory.createFormatter(type);
        switch (type) {
            case NOTIFICATION_DEFAULT:
                return new DefaultNotificationContentUpdater(formatter);
            case NOTIFICATION_SIMPLE:
                return new SimpleNotificationContentUpdater(formatter);
            default:
                throw new IllegalArgumentException("Unknown type" + type);
        }
    }
}
