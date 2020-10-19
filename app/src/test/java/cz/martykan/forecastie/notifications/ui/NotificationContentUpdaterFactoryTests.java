package cz.martykan.forecastie.notifications.ui;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import cz.martykan.forecastie.notifications.ui.DefaultNotificationContentUpdater;
import cz.martykan.forecastie.notifications.ui.NotificationContentUpdater;
import cz.martykan.forecastie.notifications.ui.NotificationContentUpdaterFactory;
import cz.martykan.forecastie.notifications.ui.SimpleNotificationContentUpdater;
import cz.martykan.forecastie.utils.formatters.WeatherFormatter;
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

    @Test
    public void doesContentUpdaterMatchTypeReturnsTrueIfContentUpdaterMatchesTypeAndFalseOtherwise() {
        boolean actual;
        WeatherFormatter weatherFormatter = Mockito.mock(WeatherFormatter.class);
        DefaultNotificationContentUpdater defaultNotificationContentUpdater =
                new DefaultNotificationContentUpdater(weatherFormatter);
        SimpleNotificationContentUpdater simpleNotificationContentUpdater =
                new SimpleNotificationContentUpdater(weatherFormatter);
        WeatherFormatterType type;

        type = WeatherFormatterType.NOTIFICATION_DEFAULT;
        actual = NotificationContentUpdaterFactory.doesContentUpdaterMatchType(type, defaultNotificationContentUpdater);

        Assert.assertTrue(
                defaultNotificationContentUpdater.getClass().getSimpleName() + " should match " + type,
                actual);

        actual = NotificationContentUpdaterFactory.doesContentUpdaterMatchType(type, simpleNotificationContentUpdater);

        Assert.assertFalse(
                simpleNotificationContentUpdater.getClass().getSimpleName() + " shouldn't match " + type,
                actual);

        type = WeatherFormatterType.NOTIFICATION_SIMPLE;
        actual = NotificationContentUpdaterFactory.doesContentUpdaterMatchType(type, defaultNotificationContentUpdater);

        Assert.assertFalse(
                defaultNotificationContentUpdater.getClass().getSimpleName() + " shouldn't  match " + type,
                actual);

        actual = NotificationContentUpdaterFactory.doesContentUpdaterMatchType(type, simpleNotificationContentUpdater);

        Assert.assertTrue(
                simpleNotificationContentUpdater.getClass().getSimpleName() + " should match " + type,
                actual);
    }
}
