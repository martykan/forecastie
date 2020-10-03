package cz.martykan.forecastie.notifications;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
        return createNotificationContentUpdater(type, null);
    }

    /**
     * Create notification content updater for specified type.
     * @param type type of weather formatter
     * @param previousContentUpdater previous content updater to copy current values
     * @return notification content updater for {@code type}
     */
    @NonNull
    public static NotificationContentUpdater createNotificationContentUpdater(
            @NonNull WeatherFormatterType type,
            @Nullable  NotificationContentUpdater previousContentUpdater
    ) {
        WeatherFormatter formatter = WeatherFormatterFactory.createFormatter(type);
        NotificationContentUpdater contentUpdater;
        switch (type) {
            case NOTIFICATION_DEFAULT:
                contentUpdater = new DefaultNotificationContentUpdater(formatter);
                break;
            case NOTIFICATION_SIMPLE:
                contentUpdater = new SimpleNotificationContentUpdater(formatter);
                break;
            default:
                throw new IllegalArgumentException("Unknown type" + type);
        }

        if (previousContentUpdater != null) {
            contentUpdater.copyPreviousValues(previousContentUpdater);
        }

        return contentUpdater;
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
