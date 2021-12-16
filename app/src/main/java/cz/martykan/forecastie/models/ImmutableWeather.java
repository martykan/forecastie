package cz.martykan.forecastie.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.martykan.forecastie.utils.Formatting;
import cz.martykan.forecastie.utils.UnitConvertor;

/**
 * Weather information.
 * <br/>
 * To create pass json into {@link #fromJson(String, long)}. For default value use
 * {@link ImmutableWeather#EMPTY}.
 */
// TODO add rain
public class ImmutableWeather implements Parcelable {
    /**
     * Value object for unknown weather (like there is no information to parse).
     */
    public static final ImmutableWeather EMPTY = new ImmutableWeather();

    /**
     * Valid numbers of wind directions:
     * <ul>
     * <li>{@link #WIND_DIRECTIONS_SIMPLE} - base four directions: north, east, south and west.</li>
     * <li>{@link #WIND_DIRECTIONS_ARROWS} - eight directions for arrows.</li>
     * <li>{@link #WIND_DIRECTIONS_MAX} - all sixteen directions.</li>
     * </ul>
     */
    @IntDef({WIND_DIRECTIONS_SIMPLE, WIND_DIRECTIONS_ARROWS, WIND_DIRECTIONS_MAX})
    public @interface NumberOfWindDirections {}
    /** Base four directions: north, east, south and west. */
    public static final int WIND_DIRECTIONS_SIMPLE = 4;
    /** Eight directions for arrows. */
    public static final int WIND_DIRECTIONS_ARROWS = 8;
    /** All sixteen directions. */
    public static final int WIND_DIRECTIONS_MAX = 16;

    private float temperature = Float.MIN_VALUE;
    private double pressure = Double.MIN_VALUE;
    private int humidity = -1;
    private double windSpeed = Double.MIN_VALUE;
    private Weather.WindDirection windDirection = null;
    private long sunrise = -1L;
    private long sunset = -1L;
    private String city = "";
    private String country = "";
    private String description = "";
    private int weatherIcon = -1;
    private long lastUpdate = -1L;

    private ImmutableWeather() {}

    /**
     * Parse OpenWeatherMap response json and initialize object with weather information.
     * <br/>
     * If {@code json} is empty or has empty object (i.e. {@code "{}"}), {@link #EMPTY} will be
     * returned.
     *
     * @param json json with weather information from OWM.
     * @param lastUpdate time of retrieving response in milliseconds.
     * @return parsed OWM response
     * @throws NullPointerException if {@code json} is null.
     */
    @NonNull
    public static ImmutableWeather fromJson(@NonNull String json, long lastUpdate)
            throws NullPointerException {
        //noinspection ConstantConditions
        if (json == null)
            throw new NullPointerException("json should not be null");

        try {
            final JSONObject reader = new JSONObject(json);
            if (reader.length() == 0)
                return EMPTY;
            else {
                final ImmutableWeather result = new ImmutableWeather();
                result.lastUpdate = lastUpdate;

                final JSONObject main = reader.optJSONObject("main");
                // temperature
                result.temperature = getFloat("temp", Float.MIN_VALUE, main);
                // pressure
                result.pressure = getDouble("pressure", Double.MIN_VALUE, main);
                // humidity
                result.humidity = getInt("humidity", -1, main);

                final JSONObject wind = reader.optJSONObject("wind");
                // wind speed
                result.windSpeed = getDouble("speed", Double.MIN_VALUE, wind);
                // wind direction
                int degree = getInt("deg", Integer.MIN_VALUE, wind);
                result.windDirection = degree == Integer.MIN_VALUE
                        ? null
                        : Weather.WindDirection.byDegree(degree);

                final JSONArray weather = reader.optJSONArray("weather");
                final JSONObject todayWeather = weather != null ? weather.optJSONObject(0) : null;
                // description
                if (todayWeather != null)
                    result.description = todayWeather.optString("description", "");
                result.weatherIcon = getInt("id", -1, todayWeather);
                if (result.weatherIcon < -1)
                    result.weatherIcon = -1;

                final JSONObject sys = reader.optJSONObject("sys");
                // country
                if (sys != null)
                    result.country = sys.optString("country", "");
                // sunrise
                result.sunrise = getTimestamp("sunrise", -1L, sys);
                // sunset
                result.sunset = getTimestamp("sunset", -1L, sys);

                // city
                result.city = reader.optString("name", "");

                return result;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return EMPTY;
        }
    }

    /**
     * Returns temperature in kelvins.
     * <br/>
     * Default value for invalid data: {@link Float#MIN_VALUE}.
     * @return temperature in kelvins
     * @see #getTemperature(String)
     */
    public float getTemperature() {
        return temperature;
    }

    /**
     * Returns temperature in specified [unit].
     * <br/>
     * Default value for invalid data: {@link Float#MIN_VALUE}.
     * @param unit resulted unit
     * @return temperature in specified unit
     * @throws NullPointerException if {@code unit} is null
     */
    // TODO rewrite units as enum
    public float getTemperature(@NonNull String unit) throws NullPointerException {
        //noinspection ConstantConditions
        if (unit == null)
            throw new NullPointerException("unit should not be null");

        float result;
        if (temperature == Float.MIN_VALUE)
            result = temperature;
        else
            result = UnitConvertor.convertTemperature(temperature, unit);
        return result;
    }

    /**
     * Returns <b>rounded</b> temperature in specified [unit].
     * <br/>
     * Default value for invalid data: {@link Integer#MIN_VALUE}.
     * @param unit resulted unit
     * @return rounded temperature in specified unit
     * @throws NullPointerException if {@code unit} is null
     */
    // TODO rewrite units as enum
    public int getRoundedTemperature(@NonNull String unit) throws NullPointerException {
        //noinspection ConstantConditions
        if (unit == null)
            throw new NullPointerException("unit should not be null");

        int result;
        if (temperature == Float.MIN_VALUE)
            result = Integer.MIN_VALUE;
        else {
            float convertedTemperature = UnitConvertor.convertTemperature(temperature, unit);
            result = (int) Math.round(convertedTemperature);
        }
        return result;
    }

    /**
     * Returns pressure in default unit (hPa/mBar).
     * <br/>
     * Default value for invalid data: {@link Double#MIN_VALUE}.
     * @return pressure in hPa/mBar
     * @see #getPressure(String)
     */
    public double getPressure() {
        return pressure;
    }

    /**
     * Returns pressure in specified [unit].
     * <br/>
     * Default value for invalid data: {@link Double#MIN_VALUE}.
     * @param unit resulted unit
     * @return pressure in specified unit
     * @throws NullPointerException if {@code unit} is null
     */
    // TODO rewrite units as enum
    public double getPressure(@NonNull String unit) throws NullPointerException {
        //noinspection ConstantConditions
        if (unit == null)
            throw new NullPointerException("unit should not be null");

        double result;
        if (pressure == Double.MIN_VALUE)
            result = pressure;
        else
            result = UnitConvertor.convertPressure(pressure, unit);
        return result;
    }

    /**
     * Returns humidity in per cents.
     * <br/>
     * Default value for invalid data: -1.
     * @return humidity in per cents
     */
    public int getHumidity() {
        return humidity;
    }

    /**
     * Returns wind speed in meter/sec.
     * <br/>
     * Default value for invalid data: {@link Double#MIN_VALUE}.
     * @return wind speed in meter/sec
     * @see #getWindSpeed(String)
     */
    public double getWindSpeed() {
        return windSpeed;
    }

    /**
     * Returns wind speed in specified {@code unit}.
     * <br/>
     * Default value for invalid data: {@link Double#MIN_VALUE}.
     * @param unit resulted unit
     * @return wind speed in specified unit
     * @throws NullPointerException if {@code unit} is null
     */
    public double getWindSpeed(@NonNull String unit) throws NullPointerException {
        //noinspection ConstantConditions
        if (unit == null)
            throw new NullPointerException("unit should not be null");

        double result;
        if (windSpeed == Double.MIN_VALUE)
            result = windSpeed;
        else
            result = UnitConvertor.convertWind(windSpeed, unit);
        return result;
    }

    /**
     * Returns wind direction.
     * <br/>
     * Default value for invalid data: {@code null}.
     * @return wind direction
     * @see Weather.WindDirection
     */
    @Nullable
    public Weather.WindDirection getWindDirection() {
        return windDirection;
    }

    /**
     * Returns wind direction scaled by specified maximum possible directions.
     * <br/>
     * Default value for invalid data: {@code null}.
     * @param maxDirections maximum possible directions
     * @return wind direction scaled by {@code maxDirections}
     * @see NumberOfWindDirections
     */
    @Nullable
    public Weather.WindDirection getWindDirection(@NumberOfWindDirections int maxDirections) {
        Weather.WindDirection result;
        if (windDirection == null)
            result = null;
        else {
            int diff = Weather.WindDirection.values().length / maxDirections - 1;
            result = Weather.WindDirection.values()[windDirection.ordinal() - diff];
        }
        return result;
    }

    /**
     * Returns sunrise time as UNIX timestamp.
     * <br/>
     * Default value for invalid data: -1.
     * @return sunrise time as UNIX timestamp
     */
    public long getSunrise() {
        return sunrise;
    }

    /**
     * Returns sunset time as UNIX timestamp.
     * <br/>
     * Default value for invalid data: -1.
     * @return sunset time as UNIX timestamp
     */
    public long getSunset() {
        return sunset;
    }

    /**
     * Returns city name.
     * <br/>
     * Default value for invalid data: empty string.
     * @return city name
     */
    @NonNull
    public String getCity() {
        return city;
    }

    /**
     * Returns country code.
     * <br/>
     * Default value for invalid data: empty string.
     * @return country code
     */
    @NonNull
    public String getCountry() {
        return country;
    }

    /**
     * Returns weather description.
     * <br/>
     * Default value for invalid data: empty string.
     * @return weather description.
     */
    @NonNull
    public String getDescription() {
        return description;
    }

    /**
     * Returns weather id for formatting weather icon.
     * <br/>
     * Default value for invalid data: -1.
     * @return weather id
     * @see Formatting#getWeatherIcon(int, boolean)
     */
    public int getWeatherIcon() {
        return weatherIcon;
    }

    /**
     * Returns time when this data has been created as timestamp in milliseconds.
     * <br/>
     * Default value for invalid data: -1.
     * @return data creation timestamp in milliseconds
     */
    public long getLastUpdate() {
        return lastUpdate;
    }

    @SuppressWarnings("SameParameterValue")
    private static float getFloat(@NonNull String key, float def, @Nullable JSONObject jsonObject) {
        float result;
        if (jsonObject != null && jsonObject.has(key)) {
            try {
                result = Float.parseFloat(jsonObject.getString("temp"));
            } catch (NumberFormatException | JSONException e) {
                e.printStackTrace();
                result = def;
            }
        } else {
            result = def;
        }
        return result;
    }

    @SuppressWarnings("SameParameterValue")
    private static double getDouble(@NonNull String key,
                                    double def,
                                    @Nullable JSONObject jsonObject
    ) {
        double result;
        if (jsonObject != null && jsonObject.has(key)) {
            try {
                result = jsonObject.getDouble(key);
            } catch (JSONException e) {
                e.printStackTrace();
                result = def;
            }
        } else {
            result = def;
        }
        return result;
    }

    private static int getInt(@NonNull String key, int def, @Nullable JSONObject jsonObject) {
        int result;
        if (jsonObject != null && jsonObject.has(key)) {
            try {
                result = jsonObject.getInt(key);
            } catch (JSONException e) {
                result = def;
            }
        } else {
            result = def;
        }
        return result;
    }

    @SuppressWarnings("SameParameterValue")
    private static long getTimestamp(String key, long def, @Nullable JSONObject jsonObject) {
        long result;
        if (jsonObject != null && jsonObject.has(key)) {
            try {
                result = jsonObject.getLong(key);
                if (result < 0)
                    result = def;
            } catch (JSONException e) {
                e.printStackTrace();
                result = def;
            }
        } else {
            result = def;
        }
        return result;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImmutableWeather that = (ImmutableWeather) o;

        if (Float.compare(that.temperature, temperature) != 0) return false;
        if (Double.compare(that.pressure, pressure) != 0) return false;
        if (humidity != that.humidity) return false;
        if (Double.compare(that.windSpeed, windSpeed) != 0) return false;
        if (sunrise != that.sunrise) return false;
        if (sunset != that.sunset) return false;
        if (weatherIcon != that.weatherIcon) return false;
        if (lastUpdate != that.lastUpdate) return false;
        if (windDirection != that.windDirection) return false;
        if (!city.equals(that.city)) return false;
        if (!country.equals(that.country)) return false;
        return description.equals(that.description);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (temperature != +0.0f ? Float.floatToIntBits(temperature) : 0);
        temp = Double.doubleToLongBits(pressure);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + humidity;
        temp = Double.doubleToLongBits(windSpeed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (windDirection != null ? windDirection.hashCode() : 0);
        result = 31 * result + (int) (sunrise ^ (sunrise >>> 32));
        result = 31 * result + (int) (sunset ^ (sunset >>> 32));
        result = 31 * result + city.hashCode();
        result = 31 * result + country.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + weatherIcon;
        result = 31 * result + (int) (lastUpdate ^ (lastUpdate >>> 32));
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "ImmutableWeather{" +
                "temperature=" + temperature +
                ", pressure=" + pressure +
                ", humidity=" + humidity +
                ", windSpeed=" + windSpeed +
                ", windDirection=" + windDirection +
                ", sunrise=" + sunrise +
                ", sunset=" + sunset +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", description='" + description + '\'' +
                ", weatherIcon=" + weatherIcon +
                ", lastUpdate=" + lastUpdate +
                '}';
    }

    // Parcelable implementation

    protected ImmutableWeather(Parcel in) {
        temperature = in.readFloat();
        pressure = in.readDouble();
        humidity = in.readInt();
        windSpeed = in.readDouble();
        int direction = in.readInt();
        if (direction < 0 || direction >= Weather.WindDirection.values().length)
            windDirection = null;
        else
            windDirection = Weather.WindDirection.values()[direction];
        sunrise = in.readLong();
        sunset = in.readLong();
        city = in.readString();
        country = in.readString();
        description = in.readString();
        weatherIcon = in.readInt();
        lastUpdate = in.readLong();
    }

    public static final Creator<ImmutableWeather> CREATOR = new Creator<ImmutableWeather>() {
        @Override
        public ImmutableWeather createFromParcel(Parcel in) {
            return new ImmutableWeather(in);
        }

        @Override
        public ImmutableWeather[] newArray(int size) {
            return new ImmutableWeather[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(temperature);
        dest.writeDouble(pressure);
        dest.writeInt(humidity);
        dest.writeDouble(windSpeed);
        if (windDirection == null)
            dest.writeInt(Integer.MIN_VALUE);
        else
            dest.writeInt(windDirection.ordinal());
        dest.writeLong(sunrise);
        dest.writeLong(sunset);
        dest.writeString(city);
        dest.writeString(country);
        dest.writeString(description);
        dest.writeInt(weatherIcon);
        dest.writeLong(lastUpdate);
    }
}