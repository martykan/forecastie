package cz.martykan.forecastie.models;

import androidx.annotation.NonNull;

import cz.martykan.forecastie.utils.formatters.WeatherFormatterType;

public class WeatherPresentation {
    /** Do not round temperature by default. */
    public static final boolean DEFAULT_DO_ROUND_TEMPERATURE = false;
    /** Default temperature unit is Celsius. */
    public static final String DEFAULT_TEMPERATURE_UNITS = "°C";
    /** Default wind speed unit is meters per second. */
    public static final String DEFAULT_WIND_SPEED_UNITS = "m/s";
    /** Default wind direction format is arrows. */
    public static final String DEFAULT_WIND_DIRECTION_FORMAT = "arrow";
    /** Default pressure units is hPa/mBar. */
    public static final String DEFAULT_PRESSURE_UNITS = "hPa/mBar";

    private final boolean roundedTemperature;
    private final String temperatureUnits;
    private final String windSpeedUnits;
    private final String windDirectionFormat;
    private final String pressureUnits;
    /** Weather information. */
    private final ImmutableWeather weather;
    private final WeatherFormatterType type;

    public WeatherPresentation() {
        roundedTemperature = DEFAULT_DO_ROUND_TEMPERATURE;
        temperatureUnits = DEFAULT_TEMPERATURE_UNITS;
        windSpeedUnits = DEFAULT_WIND_SPEED_UNITS;
        windDirectionFormat = DEFAULT_WIND_DIRECTION_FORMAT;
        pressureUnits = DEFAULT_PRESSURE_UNITS;
        weather = ImmutableWeather.EMPTY;
        type = WeatherFormatterType.NOTIFICATION_SIMPLE;
    }

    public WeatherPresentation(boolean roundedTemperature, @NonNull String temperatureUnits,
            @NonNull String windSpeedUnits, @NonNull String windDirectionFormat,
            @NonNull String pressureUnits, @NonNull ImmutableWeather weather,
            @NonNull WeatherFormatterType type
    ) {
        this.roundedTemperature = roundedTemperature;
        this.temperatureUnits = temperatureUnits;
        this.windSpeedUnits = windSpeedUnits;
        this.windDirectionFormat = windDirectionFormat;
        this.pressureUnits = pressureUnits;
        this.weather = weather;
        this.type = type;
    }

    /**
     * Returns {@code true} if temperature should be rounded and {@code false} if shouldn't.
     * @return {@code true} if temperature should be rounded and {@code false} if shouldn't
     */
    public boolean isRoundedTemperature() {
        return roundedTemperature;
    }

    /**
     * Returns temperature units as temperature unit key.
     * @return temperature units
     */
    public String getTemperatureUnits() {
        return temperatureUnits;
    }

    /**
     * Returns wind speed units as wind speed unit key.
     * @return wind speed units
     */
    public String getWindSpeedUnits() {
        return windSpeedUnits;
    }

    /**
     * Returns wind direction format.
     * @return wind direction format
     */
    public String getWindDirectionFormat() {
        return windDirectionFormat;
    }

    /**
     * Returns pressure units as pressure unit key.
     * @return pressure units
     */
    public String getPressureUnits() {
        return pressureUnits;
    }

    /**
     * Returns weather information.
     * @return weather information
     */
    public ImmutableWeather getWeather() {
        return weather;
    }

    /**
     * Returns weather formatter type.
     * @return weather formatter type
     */
    public WeatherFormatterType getType() {
        return type;
    }

    public WeatherPresentation copy(boolean roundedTemperature) {
        return new WeatherPresentation(roundedTemperature, temperatureUnits, windSpeedUnits,
                windDirectionFormat, pressureUnits, weather, type);
    }

    public WeatherPresentation copyTemperatureUnits(@NonNull String temperatureUnits) {
        return new WeatherPresentation(roundedTemperature, temperatureUnits, windSpeedUnits,
                windDirectionFormat, pressureUnits, weather, type);
    }

    public WeatherPresentation copyWindSpeedUnits(@NonNull String windSpeedUnits) {
        return new WeatherPresentation(roundedTemperature, temperatureUnits, windSpeedUnits,
                windDirectionFormat, pressureUnits, weather, type);
    }

    public WeatherPresentation copyWindDirectionFormat(@NonNull String windDirectionFormat) {
        return new WeatherPresentation(roundedTemperature, temperatureUnits, windSpeedUnits,
                windDirectionFormat, pressureUnits, weather, type);
    }

    public WeatherPresentation copyPressureUnits(@NonNull String pressureUnits) {
        return new WeatherPresentation(roundedTemperature, temperatureUnits, windSpeedUnits,
                windDirectionFormat, pressureUnits, weather, type);
    }

    public WeatherPresentation copy(@NonNull ImmutableWeather weather) {
        return new WeatherPresentation(roundedTemperature, temperatureUnits, windSpeedUnits,
                windDirectionFormat, pressureUnits, weather, type);
    }

    public WeatherPresentation copy(@NonNull WeatherFormatterType type) {
        return new WeatherPresentation(roundedTemperature, temperatureUnits, windSpeedUnits,
                windDirectionFormat, pressureUnits, weather, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WeatherPresentation)) return false;

        WeatherPresentation that = (WeatherPresentation) o;

        if (roundedTemperature != that.roundedTemperature) return false;
        if (!temperatureUnits.equals(that.temperatureUnits)) return false;
        if (!windSpeedUnits.equals(that.windSpeedUnits)) return false;
        if (!windDirectionFormat.equals(that.windDirectionFormat)) return false;
        if (!pressureUnits.equals(that.pressureUnits)) return false;
        if (!weather.equals(that.weather)) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = (roundedTemperature ? 1 : 0);
        result = 31 * result + temperatureUnits.hashCode();
        result = 31 * result + windSpeedUnits.hashCode();
        result = 31 * result + windDirectionFormat.hashCode();
        result = 31 * result + pressureUnits.hashCode();
        result = 31 * result + weather.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "WeatherPresentation{" +
                "roundedTemperature=" + roundedTemperature +
                ", temperatureUnits='" + temperatureUnits + '\'' +
                ", windSpeedUnits='" + windSpeedUnits + '\'' +
                ", windDirectionFormat='" + windDirectionFormat + '\'' +
                ", pressureUnits='" + pressureUnits + '\'' +
                ", weather=" + weather +
                ", type=" + type +
                '}';
    }
}