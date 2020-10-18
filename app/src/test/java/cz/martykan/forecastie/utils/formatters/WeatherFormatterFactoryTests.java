package cz.martykan.forecastie.utils.formatters;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class WeatherFormatterFactoryTests {
    @Test
    public void createFormatterCreatesAppropriateFormatters() {
        WeatherFormatter actual;
        Map<WeatherFormatterType, Class<? extends WeatherFormatter>> typeToExpectedMap =
                new HashMap<>(WeatherFormatterType.values().length);
        typeToExpectedMap.put(WeatherFormatterType.NOTIFICATION_DEFAULT,
                WeatherDefaultNotificationFormatter.class);
        typeToExpectedMap.put(WeatherFormatterType.NOTIFICATION_SIMPLE,
                WeatherSimpleNotificationFormatter.class);

        for (Map.Entry<WeatherFormatterType, Class<? extends WeatherFormatter>> entry : typeToExpectedMap.entrySet()) {
            actual = WeatherFormatterFactory.createFormatter(entry.getKey());

            Assert.assertEquals("wrong formatter for " + entry.getKey(),
                    entry.getValue(), actual.getClass());
        }
    }
}
