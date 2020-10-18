package cz.martykan.forecastie.notifications.ui;

import androidx.annotation.NonNull;

import cz.martykan.forecastie.utils.formatters.WeatherFormatter;
import cz.martykan.forecastie.utils.formatters.WeatherFormatterFactory;
import cz.martykan.forecastie.utils.formatters.WeatherFormatterType;

/**
 * Factory for creation notification content updaters by type.
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

    /**
     * Check is content updater has appropriate class for specified type.
     * @param type type of weather formatter
     * @param contentUpdater content updater to check
     * @return {@code true} if content updater matches type and {@code false} if not.
     */
    public static boolean doesContentUpdaterMatchType(
            @NonNull WeatherFormatterType type,
            @NonNull  NotificationContentUpdater contentUpdater) {
        return (type == WeatherFormatterType.NOTIFICATION_DEFAULT
                && contentUpdater instanceof DefaultNotificationContentUpdater)
                ||
                (type == WeatherFormatterType.NOTIFICATION_SIMPLE
                        && contentUpdater instanceof SimpleNotificationContentUpdater);
    }
}
