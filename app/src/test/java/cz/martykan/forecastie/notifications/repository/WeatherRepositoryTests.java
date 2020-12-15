package cz.martykan.forecastie.notifications.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.android.util.concurrent.PausedExecutorService;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.models.ImmutableWeather;
import cz.martykan.forecastie.models.WeatherPresentation;
import cz.martykan.forecastie.utils.formatters.WeatherFormatterType;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

@SuppressWarnings("UnstableApiUsage")
@RunWith(AndroidJUnit4.class)
@Config(sdk = 27)
@LooperMode(LooperMode.Mode.PAUSED)
public class WeatherRepositoryTests {
    private Context context;
    private SharedPreferences prefs;
    private PausedExecutorService executor;

    @Before
    public void setUp() {
        context = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        executor = new PausedExecutorService();
    }

    @Test
    public void observeWeatherEmitsWeatherPresentationWithDefaultValuesIfThereAreNoValues() {
        final WeatherPresentation[] actual = new WeatherPresentation[1];
        WeatherRepository repository = new WeatherRepository(context, executor);

        repository.observeWeather(new WeatherRepository.RepositoryListener() {
            @Override
            public void onChange(@NonNull WeatherPresentation newData) {
                actual[0] = newData;
            }
        });
        executor.runAll();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        Assert.assertEquals("weather data isn't default",
                ImmutableWeather.EMPTY, actual[0].getWeather());
        Assert.assertEquals("weather formatter type isn't default",
                WeatherFormatterType.NOTIFICATION_SIMPLE, actual[0].getType());
        Assert.assertEquals("rounded temperature value isn't default",
                WeatherPresentation.DEFAULT_DO_ROUND_TEMPERATURE, actual[0].isRoundedTemperature());
        Assert.assertEquals("temperature unit isn't default",
                WeatherPresentation.DEFAULT_TEMPERATURE_UNITS, actual[0].getTemperatureUnits());
        Assert.assertEquals("wind speed unit isn't default",
                WeatherPresentation.DEFAULT_WIND_SPEED_UNITS, actual[0].getWindSpeedUnits());
        Assert.assertEquals("wind direction format isn't default",
                WeatherPresentation.DEFAULT_WIND_DIRECTION_FORMAT,
                actual[0].getWindDirectionFormat());
        Assert.assertEquals("pressure unit isn't default",
                WeatherPresentation.DEFAULT_PRESSURE_UNITS, actual[0].getPressureUnits());
    }

    @Test
    public void observeWeatherEmitsWeatherPresentationWithCurrentValuesFromSharedPreference() {
        String type = context.getString(R.string.settings_notification_type_key_default);
        String temperatureUnit = "°F";
        String windSpeedUnit = "kn";
        String windDirectionFormat = "none";
        String pressureUnit = "in Hg";
        putValuesIntoPrefs(type, temperatureUnit, windSpeedUnit, windDirectionFormat, pressureUnit);
        final WeatherPresentation[] actual = new WeatherPresentation[1];
        WeatherRepository repository = new WeatherRepository(context, executor);

        repository.observeWeather(new WeatherRepository.RepositoryListener() {
            @Override
            public void onChange(@NonNull WeatherPresentation newData) {
                actual[0] = newData;
            }
        });
        executor.runAll();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        Assert.assertNotEquals("weather data is default",
                ImmutableWeather.EMPTY, actual[0].getWeather());
        Assert.assertEquals("weather formatter type is default",
                WeatherFormatterType.NOTIFICATION_DEFAULT, actual[0].getType());
        Assert.assertEquals("rounded temperature value is default",
                !WeatherPresentation.DEFAULT_DO_ROUND_TEMPERATURE, actual[0].isRoundedTemperature());
        Assert.assertEquals("temperature unit is default",
                temperatureUnit, actual[0].getTemperatureUnits());
        Assert.assertEquals("wind speed unit is default",
                windSpeedUnit, actual[0].getWindSpeedUnits());
        Assert.assertEquals("wind direction format isn default",
                windDirectionFormat, actual[0].getWindDirectionFormat());
        Assert.assertEquals("pressure unit is default",
                pressureUnit, actual[0].getPressureUnits());
    }

    @Test
    public void observeWeatherEmitsNewValuesWhenTheyAreUpdatedInSharedPreference() throws InterruptedException {
        String type = context.getString(R.string.settings_notification_type_key_default);
        String temperatureUnit = "°F";
        String windSpeedUnit = "kn";
        String windDirectionFormat = "none";
        String pressureUnit = "in Hg";
        final WeatherPresentation[] actual = new WeatherPresentation[1];
        WeatherRepository repository = new WeatherRepository(context, executor);
        repository.observeWeather(new WeatherRepository.RepositoryListener() {
            @Override
            public void onChange(@NonNull WeatherPresentation newData) {
                actual[0] = newData;
            }
        });

        prefs.edit()
                .putString("lastToday", "{\"main\": {\"temp\": 315.25}}")
                .putLong("lastUpdate", 100L)
                .commit();
        executor.runAll();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        Assert.assertNotEquals("weather data is default",
                ImmutableWeather.EMPTY, actual[0].getWeather());

        prefs.edit()
                .putString(context.getString(R.string.settings_notification_type_key), type)
                .commit();
        executor.runAll();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        Assert.assertEquals("weather formatter type is default",
                WeatherFormatterType.NOTIFICATION_DEFAULT, actual[0].getType());

        prefs.edit()
                .putBoolean("temperatureInteger", !WeatherPresentation.DEFAULT_DO_ROUND_TEMPERATURE)
                .commit();
        executor.runAll();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        Assert.assertEquals("rounded temperature value is default",
                !WeatherPresentation.DEFAULT_DO_ROUND_TEMPERATURE, actual[0].isRoundedTemperature());

        prefs.edit().putString("unit", temperatureUnit).commit();
        executor.runAll();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        Assert.assertEquals("temperature unit is default",
                temperatureUnit, actual[0].getTemperatureUnits());

        prefs.edit().putString("speedUnit", windSpeedUnit).commit();
        executor.runAll();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        Assert.assertEquals("wind speed unit is default",
                windSpeedUnit, actual[0].getWindSpeedUnits());

        prefs.edit().putString("windDirectionFormat", windDirectionFormat).commit();
        executor.runAll();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        Assert.assertEquals("wind direction format isn default",
                windDirectionFormat, actual[0].getWindDirectionFormat());

        prefs.edit().putString("pressureUnit", pressureUnit).commit();
        executor.runAll();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        Assert.assertEquals("pressure unit is default",
                pressureUnit, actual[0].getPressureUnits());
    }

    @Test
    public void observeWeatherEmitsNewValuesInEveryListener() throws InterruptedException {
        final WeatherPresentation[] actual = new WeatherPresentation[2];
        WeatherRepository repository = new WeatherRepository(context, executor);
        repository.observeWeather(new WeatherRepository.RepositoryListener() {
            @Override
            public void onChange(@NonNull WeatherPresentation newData) {
                actual[0] = newData;
            }
        });
        repository.observeWeather(new WeatherRepository.RepositoryListener() {
            @Override
            public void onChange(@NonNull WeatherPresentation newData) {
                actual[1] = newData;
            }
        });

        prefs.edit()
                .putString("lastToday", "{\"main\": {\"temp\": 315.25}}")
                .putLong("lastUpdate", 100L)
                .commit();
        executor.runAll();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        Assert.assertNotEquals("weather data of first listener is default",
                ImmutableWeather.EMPTY, actual[0].getWeather());
        Assert.assertNotEquals("weather data of second listener is default",
                ImmutableWeather.EMPTY, actual[1].getWeather());
    }

    @Test
    public void getWeatherReturnsCurrentValues() {
        String pressureUnit = "in Hg";
        WeatherRepository repository = new WeatherRepository(context, executor);

        WeatherPresentation actual = repository.getWeather();
        Assert.assertEquals("weather data isn't default",
                ImmutableWeather.EMPTY, actual.getWeather());
        Assert.assertEquals("pressure unit isn't default",
                WeatherPresentation.DEFAULT_PRESSURE_UNITS, actual.getPressureUnits());

        prefs.edit()
                .putString("lastToday", "{\"main\": {\"temp\": 315.25}}")
                .putLong("lastUpdate", 100L)
                .putString("pressureUnit", pressureUnit)
                .commit();
        executor.runAll();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        actual = repository.getWeather();

        Assert.assertNotEquals("weather data of first listener is default",
                ImmutableWeather.EMPTY, actual.getWeather());
        Assert.assertNotEquals("weather data of second listener is default",
                ImmutableWeather.EMPTY, actual.getWeather());
    }

    // TODO add tests for clear method

    private void putValuesIntoPrefs(@NonNull String type, @NonNull String temperatureUnit,
                                    @NonNull String windSpeedUnit,
                                    @NonNull String windDirectionFormat,
                                    @NonNull String pressureUnit) {
        prefs.edit()
                .putString("lastToday", "{\"main\": {\"temp\": 315.25}}")
                .putLong("lastUpdate", 100L)
                .putString(context.getString(R.string.settings_notification_type_key), type)
                .putBoolean("temperatureInteger", !WeatherPresentation.DEFAULT_DO_ROUND_TEMPERATURE)
                .putString("unit", temperatureUnit)
                .putString("speedUnit", windSpeedUnit)
                .putString("windDirectionFormat", windDirectionFormat)
                .putString("pressureUnit", pressureUnit)
                .commit();
    }
}