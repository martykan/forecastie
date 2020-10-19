package cz.martykan.forecastie.utils.formatters;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.text.DecimalFormat;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.models.ImmutableWeather;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 27)
public class WeatherSimpleNotificationFormatterTests {
    private WeatherSimpleNotificationFormatter formatter = new WeatherSimpleNotificationFormatter();

    @Test
    public void isEnoughValidDataChecksEnoughFields() {
        String[] nonValidJsons = {
                "{}",
                "{\"main\": {\"temp\": 315.25}}",
                "{\"main\": {\"pressure\": 1016}}",
                "{\"main\": {\"humidity\": 100}}",
                "{\"main\": {\"temp\": 315.25, \"pressure\": 1016}}",
                "{\"main\": {\"temp\": 315.25, \"humidity\": 100}}",
                "{\"main\": {\"pressure\": 1016, \"humidity\": 100}}",
                "{\"main\": {\"temp\": 315.25, \"pressure\": 1016, \"humidity\": 100}}",
                "{\"weather\": [{\"description\": \"clear sky\"}]}",
                "{\"weather\": [{\"id\": 210}]}",
                "{\"weather\": [{\"description\": \"clear sky\", \"id\": 210}]}",
                "{\"wind\": {\"speed\": 1.5}}",
                "{\"wind\": {\"deg\": 350}}",
                "{\"wind\": {\"speed\": 1.5, \"deg\": 350}}",
                "{\"main\": {\"temp\": 315.25}, \"weather\": [{\"description\": \"clear sky\"}]}",
                "{\"main\": {\"temp\": 315.25}, \"weather\": [{\"id\": 210}]}",
                "{\"main\": {\"temp\": \"Parse this\"}, \"weather\": [{\"description\": \"clear sky\", \"id\": 210}]}",
                "{\"main\": {\"temp\": 315.25}, \"weather\": [{\"description\": \"clear sky\", \"id\": 8589934591}]}",
                "{\"main\": {\"temp\": 315.25, \"pressure\": 1016, \"humidity\": 100}, \"wind\": {\"speed\": 1.5}}",
                "{\"main\": {\"temp\": 315.25, \"pressure\": 1016, \"humidity\": 100}, \"wind\": {\"deg\": 350}}",
                "{\"main\": {\"temp\": 315.25, \"pressure\": 1016, \"humidity\": 100}, \"wind\": {\"speed\": 1.5, \"deg\": 350}}",
                "{\"weather\": [{\"description\": \"clear sky\", \"id\": 210}], \"wind\": {\"speed\": 1.5, \"deg\": 350}}"
        };
        String[] validJsons = {
                "{\"main\": {\"temp\": 315.25, \"pressure\": 1016, \"humidity\": 100}, \"weather\": [{\"description\": \"clear sky\", \"id\": 210}], \"wind\": {\"speed\": 1.5, \"deg\": 350}}",
                "{\"main\": {\"temp\": 315.25,\"pressure\": 1016, \"humidity\": 100}, \"weather\": [{\"description\": \"clear sky\", \"id\": 210}], \"wind\": {\"speed\": 1.5, \"deg\": 350}, \"sys\": {\"sunrise\": 1}}",
                "{\"main\": {\"temp\": 315.25, \"pressure\": 1016, \"humidity\": 100}, \"weather\": [{\"description\": \"clear sky\", \"id\": 210}], \"wind\": {\"speed\": 1.5, \"deg\": 350}, \"sys\": {\"sunset\": 3}}",
                "{\"main\": {\"temp\": 315.25, \"pressure\": 1016, \"humidity\": 100}, \"weather\": [{\"description\": \"clear sky\", \"id\": 210}], \"wind\": {\"speed\": 1.5, \"deg\": 350}, \"sys\": {\"sunrise\": 1, \"sunset\": 3}}"
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
    public void isEnoughValidMainDataChecksTemperature() {
        String[] nonValidJsons = {
                "{}",
                "{\"main\": {\"temp\": \"Parse this\"}}",
                "{\"main\": {\"pressure\": 1016}}",
                "{\"main\": {\"pressure\": 1016, \"humidity\": 100}}",
                "{\"weather\": [{\"description\": \"clear sky\", \"id\": 210}]}",
                "{\"wind\": {\"speed\": 1.5, \"deg\": 350}}"
        };
        String[] validJsons = {
                "{\"main\": {\"temp\": 315.25, \"pressure\": 1016, \"humidity\": 100}, \"weather\": [{\"description\": \"clear sky\", \"id\": 210}], \"wind\": {\"speed\": 1.5, \"deg\": 350}}",
                "{\"main\": {\"temp\": 315.25}}"
        };
        boolean nonValidActual = false;
        boolean validActual = true;
        ImmutableWeather weather;

        for (String json: nonValidJsons) {
            weather = ImmutableWeather.fromJson(json, 1);
            nonValidActual = nonValidActual || formatter.isEnoughValidMainData(weather);
        }

        for (String json: validJsons) {
            weather = ImmutableWeather.fromJson(json, 1);
            validActual = validActual && formatter.isEnoughValidMainData(weather);
        }

        Assert.assertFalse("some of non valid json has been defined as valid",
                nonValidActual);
        Assert.assertTrue("some of valid json has been defined as non valid", validActual);
        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                //noinspection ConstantConditions
                formatter.isEnoughValidMainData(null);
            }
        });
    }

    @Test
    public void getTemperatureConvertsAndRoundsTemperature() {
        String json = "{\"main\": {\"temp\": 315.25}}";
        ImmutableWeather weather = ImmutableWeather.fromJson(json, -1);

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
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void getTemperatureChecksParametersForNull() {
        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                formatter.getTemperature(null, "K", false);
            }
        });

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                formatter.getTemperature(ImmutableWeather.EMPTY, null,
                        false);
            }
        });
    }

    @Test
    public void getDescriptionReturnsDescriptionWithFirstUpperLetter() {
        String json = "{\"weather\": [{\"description\": \"clear sky\"}]}";
        ImmutableWeather weather = ImmutableWeather.fromJson(json, -1);

        Assert.assertEquals("description is wrong", "Clear sky",
                formatter.getDescription(weather));
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void getDescriptionChecksParametersForNull() {
        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                formatter.getDescription(null);
            }
        });
    }

    @Test
    public void getWindCombinesWindTitleAndWindSpeedAndWindDirectionWithSpecifiedUnitsAndDirectionStyle() {
        String json = "{\"wind\": {\"speed\": 1.5, \"deg\": 340}}";
        ImmutableWeather weather = ImmutableWeather.fromJson(json, -1);
        Context context = getApplicationContext();
        String title = context.getString(R.string.wind);
        String nnw = context.getString(R.string.wind_direction_north_north_west);
        String bftValue = context.getString(R.string.beaufort_light_breeze);

        Assert.assertEquals("wind in meters per second (m/s) is wrong",
                title + ": " + new DecimalFormat("0.0").format(1.5) + " m/s " + nnw,
                formatter.getWind(weather, "m/s", "abbr", context));
        Assert.assertEquals("wind in kilometers per hour (kph) is wrong",
                title + ": " + new DecimalFormat("0.0").format(5.4) + " kph ↘",
                formatter.getWind(weather, "kph", "arrow", context));
        Assert.assertEquals("wind in miles per hour (mph) is wrong",
                title + ": " + new DecimalFormat("0.0").format(3.4) + " mph",
                formatter.getWind(weather, "mph", "none", context));
        Assert.assertEquals("wind in Beaufort wind scale (bft) is wrong",
                title + ": " + bftValue + " " + nnw,
                formatter.getWind(weather, "bft", "abbr", context));
    }

    @Test
    @Config(qualifiers = "ru")
    public void getWindHandlesInvalidCases() {
        String json = "{\"wind\": {\"speed\": 1.5, \"deg\": 340}}";
        String jsonWithoutDirection = "{\"wind\": {\"speed\": 1.5}}";
        ImmutableWeather weather = ImmutableWeather.fromJson(json, -1);
        ImmutableWeather weatherWithoutDirection =
                ImmutableWeather.fromJson(jsonWithoutDirection, -1);
        Context context = getApplicationContext();
        String title = context.getString(R.string.wind);

        Assert.assertEquals("wind direction should be omitted if it's unknown",
                title + ": " + new DecimalFormat("0.0").format(1.5) + " м/с",
                formatter.getWind(weatherWithoutDirection, "m/s", "abbr",
                        context));
        Assert.assertEquals("wind direction should be omitted if direction format is unknown",
                title + ": " + new DecimalFormat("0.0").format(5.4) + " км/ч",
                formatter.getWind(weather, "kph", "абв",
                        context));
        Assert.assertEquals("should be empty string if wind speed units is unknown",
                "",
                formatter.getWind(weather, "абв", "arrow",
                        context));
        Assert.assertEquals("should be empty string if there is no wind data",
                "",
                formatter.getWind(ImmutableWeather.EMPTY, "m/s", "abbr",
                        context));
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void getWindChecksParametersForNull() {
        final Context context = getApplicationContext();

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                formatter.getWind(null, "m/s", "abbr", context);
            }
        });

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                formatter.getWind(ImmutableWeather.EMPTY, null, "abbr", context);
            }
        });

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                formatter.getWind(ImmutableWeather.EMPTY, "m/s", null, context);
            }
        });

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                formatter.getWind(ImmutableWeather.EMPTY, "m/s", "abbr",
                        null);
            }
        });
    }

    @Test
    public void getPressureCombinesPressureTitleAndPressureValueAndPressureUnits() {
        String json = "{\"main\": {\"pressure\": 1016}}";
        ImmutableWeather weather = ImmutableWeather.fromJson(json, -1);
        Context context = getApplicationContext();
        String title = context.getString(R.string.pressure);
        String expectedHPA = title + ": "
                + new DecimalFormat("0.0").format(1016.0) + " hPa/mBar";
        String expectedKPA = title + ": "
                + new DecimalFormat("0.0").format(101.6) + " kPa";
        String expectedMMHG = title + ": "
                + new DecimalFormat("0.0").format(1016.0 * 0.750061561303)
                + " mm Hg";
        String expectedINHG = title + ": "
                + new DecimalFormat("0.0").format(1016.0 * 0.0295299830714)
                + " in Hg";

        Assert.assertEquals("pressure in hPa/mBar (\"hPa/mBar\") is wrong", expectedHPA,
                formatter.getPressure(weather, "hPa/mBar", context));
        Assert.assertEquals("pressure in hPa/mBar (\"hPa\") is wrong", expectedHPA,
                formatter.getPressure(weather, "hPa", context));
        Assert.assertEquals("pressure in kPa is wrong", expectedKPA,
                formatter.getPressure(weather, "kPa", context));
        Assert.assertEquals("pressure in mm Hg is wrong", expectedMMHG,
                formatter.getPressure(weather, "mm Hg", context));
        Assert.assertEquals("pressure in in Hg is wrong", expectedINHG,
                formatter.getPressure(weather, "in Hg", context));
    }

    @Test
    public void getPressureHandlesInvalidCases() {
        String json = "{\"main\": {\"pressure\": 1016}}";
        ImmutableWeather weather = ImmutableWeather.fromJson(json, -1);
        Context context = getApplicationContext();

        Assert.assertEquals("should be empty string if pressure units is unknown",
                "",
                formatter.getPressure(weather, "qwerty", context));
        Assert.assertEquals("should be empty string if there is no pressure data",
                "",
                formatter.getPressure(ImmutableWeather.EMPTY, "kPa", context));
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void getPressureChecksParametersForNull() {
        final Context context = getApplicationContext();

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                formatter.getPressure(null, "hPa", context);
            }
        });

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                formatter.getPressure(ImmutableWeather.EMPTY, null,  context);
            }
        });

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                formatter.getPressure(ImmutableWeather.EMPTY, "hPa", null);
            }
        });
    }

    @Test
    public void getHumidityCombinesHumidityTitleAndHumidityValueAndPerCentSymbol() {
        int expectedHumidity = 99;
        String json = String.format("{\"main\": {\"humidity\": %d}}", expectedHumidity);
        ImmutableWeather weather = ImmutableWeather.fromJson(json, -1);
        Context context = getApplicationContext();
        String expected = String.format("%s: %d %%", context.getString(R.string.humidity),
                expectedHumidity);

        String actual = formatter.getHumidity(weather, context);

        Assert.assertEquals("humidity is wrong", expected, actual);
    }

    @Test
    public void getHumidityHandlesInvalidCases() {
        String json = "{\"main\": {\"humidity\": \"Parse This\"}}";
        ImmutableWeather weather = ImmutableWeather.fromJson(json, -1);
        Context context = getApplicationContext();

        Assert.assertEquals("should be empty string if humidity is unknown",
                "",
                formatter.getHumidity(weather, context));
        Assert.assertEquals("should be empty string if there is no humidity data",
                "",
                formatter.getHumidity(ImmutableWeather.EMPTY, context));
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void getHumidityChecksParametersForNull() {
        final Context context = getApplicationContext();

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                formatter.getHumidity(null, context);
            }
        });

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                formatter.getHumidity(ImmutableWeather.EMPTY, null);
            }
        });
    }
}