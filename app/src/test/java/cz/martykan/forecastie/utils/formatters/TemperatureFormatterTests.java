package cz.martykan.forecastie.utils.formatters;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import cz.martykan.forecastie.models.ImmutableWeather;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 27)
public class TemperatureFormatterTests {
    @Test
    public void getTemperatureConvertsAndRoundsTemperature() {
        String json = "{\"main\": {\"temp\": 315.25}}";
        ImmutableWeather weather = ImmutableWeather.fromJson(json, -1);

        Assert.assertEquals("temperature in kelvins is wrong", "315.2K",
                TemperatureFormatter.getTemperature(weather, "K",
                        false));
        Assert.assertEquals("temperature in celsius is wrong", "42.1°C",
                TemperatureFormatter.getTemperature(weather, "°C",
                        false));
        Assert.assertEquals("temperature in fahrenheit is wrong", "107.8°F",
                TemperatureFormatter.getTemperature(weather, "°F",
                        false));
        Assert.assertEquals("rounded temperature in kelvins is wrong", "315K",
                TemperatureFormatter.getTemperature(weather, "K",
                        true));
        Assert.assertEquals("rounded temperature in celsius is wrong", "42°C",
                TemperatureFormatter.getTemperature(weather, "°C",
                        true));
        Assert.assertEquals("rounded temperature in fahrenheit is wrong", "108°F",
                TemperatureFormatter.getTemperature(weather, "°F",
                        true));
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void getTemperatureChecksParametersForNull() {
        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                TemperatureFormatter.getTemperature(null, "K", false);
            }
        });

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                TemperatureFormatter.getTemperature(ImmutableWeather.EMPTY, null, false);
            }
        });
    }
}
