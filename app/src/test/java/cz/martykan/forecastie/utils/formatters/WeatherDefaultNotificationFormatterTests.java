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
public class WeatherDefaultNotificationFormatterTests {
    private WeatherDefaultNotificationFormatter formatter = new WeatherDefaultNotificationFormatter();

    @Test
    public void isEnoughValidDataChecksEnoughFields() {
        String[] nonValidJsons = {
                "{}",
                "{\"main\": {\"temp\": 315.25}}",
                "{\"weather\": [{\"description\": \"clear sky\"}]}",
                "{\"weather\": [{\"id\": 210}]}",
                "{\"weather\": [{\"description\": \"clear sky\", \"id\": 210}]}",
                "{\"main\": {\"temp\": 315.25}, \"weather\": [{\"description\": \"clear sky\"}]}",
                "{\"main\": {\"temp\": 315.25}, \"weather\": [{\"id\": 210}]}",
                "{\"main\": {\"temp\": \"Parse this\"}, \"weather\": [{\"description\": \"clear sky\", \"id\": 210}]}",
                "{\"main\": {\"temp\": 315.25}, \"weather\": [{\"description\": \"clear sky\", \"id\": 8589934591}]}"
        };
        String[] validJsons = {
                "{\"main\": {\"temp\": 315.25}, \"weather\": [{\"description\": \"clear sky\", \"id\": 210}]}",
                "{\"main\": {\"temp\": 315.25}, \"weather\": [{\"description\": \"clear sky\", \"id\": 210}], \"sys\": {\"sunrise\": 1}}",
                "{\"main\": {\"temp\": 315.25}, \"weather\": [{\"description\": \"clear sky\", \"id\": 210}], \"sys\": {\"sunset\": 3}}",
                "{\"main\": {\"temp\": 315.25}, \"weather\": [{\"description\": \"clear sky\", \"id\": 210}], \"sys\": {\"sunrise\": 1, \"sunset\": 3}}"
        };
        boolean nonValidActual = false;
        boolean validActual = true;
        ImmutableWeather weather;

        for (String json: nonValidJsons) {
            weather = ImmutableWeather.fromJson(json, 1);
            nonValidActual = nonValidActual || formatter.isEnoughValidData(weather);
        }

        for (String json: validJsons) {
            weather = ImmutableWeather.fromJson(json, 1);
            validActual = validActual && formatter.isEnoughValidData(weather);
        }

        Assert.assertFalse("some of non valid json has been defined as valid",
                nonValidActual);
        Assert.assertTrue("some of valid json has been defined as non valid", validActual);
        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                //noinspection ConstantConditions
                formatter.isEnoughValidData(null);
            }
        });
    }

    @Test
    public void getTemperatureConvertsAndRoundsTemperature() {
        String json = "{\"main\": {\"temp\": 315.25}}";
        final ImmutableWeather weather = ImmutableWeather.fromJson(json, -1);

        Assert.assertEquals("temperature in kelvins is wrong", "315.2K",
                formatter.getTemperature(weather, "K", false));
        Assert.assertEquals("temperature in celsius is wrong", "42.1°C",
                formatter.getTemperature(weather, "°C", false));
        Assert.assertEquals("temperature in fahrenheit is wrong", "107.8°F",
                formatter.getTemperature(weather, "°F", false));
        Assert.assertEquals("rounded temperature in kelvins is wrong", "315K",
                formatter.getTemperature(weather, "K", true));
        Assert.assertEquals("rounded temperature in celsius is wrong", "42°C",
                formatter.getTemperature(weather, "°C", true));
        Assert.assertEquals("rounded temperature in fahrenheit is wrong", "108°F",
                formatter.getTemperature(weather, "°F", true));

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                //noinspection ConstantConditions
                formatter.getTemperature(null, "K", false);
            }
        });
        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                //noinspection ConstantConditions
                formatter.getTemperature(weather, null, false);
            }
        });
    }

    @Test
    public void getDescriptionReturnsDescriptionWithFirstUpperLetter() {
        String json = "{\"weather\": [{\"description\": \"clear sky\"}]}";
        ImmutableWeather weather = ImmutableWeather.fromJson(json, -1);

        Assert.assertEquals("description is wrong", "Clear sky",
                formatter.getDescription(weather));

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                //noinspection ConstantConditions
                formatter.getDescription(null);
            }
        });
    }

    // TODO add test for getWeatherIcon
}
