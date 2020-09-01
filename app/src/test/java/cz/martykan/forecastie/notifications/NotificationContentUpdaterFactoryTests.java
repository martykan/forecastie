package cz.martykan.forecastie.notifications;

import junit.framework.Assert;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import cz.martykan.forecastie.utils.formatters.WeatherFormatterType;

public class NotificationContentUpdaterFactoryTests {
    @Test
    public void createNotificationContentUpdaterCreatesAppropriateContentUpdater() {
        NotificationContentUpdater actual;
        Map<WeatherFormatterType, Class<? extends NotificationContentUpdater>> typeToExpectedMap =
                new HashMap<>(WeatherFormatterType.values().length);
        typeToExpectedMap.put(WeatherFormatterType.NOTIFICATION_DEFAULT,
                DefaultNotificationContentUpdater.class);
        typeToExpectedMap.put(WeatherFormatterType.NOTIFICATION_SIMPLE,
                SimpleNotificationContentUpdater.class);

        for (Map.Entry<WeatherFormatterType, Class<? extends NotificationContentUpdater>> entry : typeToExpectedMap.entrySet()) {
            actual = NotificationContentUpdaterFactory.createNotificationContentUpdater(entry.getKey());

            Assert.assertEquals("wrong content updater for " + entry.getKey(),
                    entry.getValue(), actual.getClass());
        }
    }
}
