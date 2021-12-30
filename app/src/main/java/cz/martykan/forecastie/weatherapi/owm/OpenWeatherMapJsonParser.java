package cz.martykan.forecastie.weatherapi.owm;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.martykan.forecastie.models.Weather;

public class OpenWeatherMapJsonParser {

    @NonNull
    public static List<Weather> convertJsonToWeatherList(String citiesString) throws JSONException {
        List<Weather> citiesWithWeather = new ArrayList<>();

        JSONObject weatherObject = new JSONObject(citiesString);
        JSONArray weatherArray = weatherObject.getJSONArray("list");

        JSONObject cityObject = weatherObject.getJSONObject("city");
        int cityId = cityObject.getInt("id");
        String cityName = cityObject.getString("name");
        String country = cityObject.getString("country");

        for (int i = 0; i < weatherArray.length(); i++) {
            JSONObject currentWeatherObject = weatherArray.getJSONObject(i);
            Weather weather = getWeatherFromJsonObject(currentWeatherObject);

            setSunsetAndSunrise(weather, cityObject);
            setCityAndCountry(weather, cityId, cityName, country);

            JSONObject coordinatesObject = cityObject.getJSONObject("coord");
            setCoordinates(weather, coordinatesObject);

            weather.setChanceOfPrecipitation(currentWeatherObject.optDouble("pop", 0));

            citiesWithWeather.add(weather);
        }

        return citiesWithWeather;
    }

    @NonNull
    public static Weather convertJsonToWeather(String weatherString) throws JSONException {
        JSONObject weatherObject = new JSONObject(weatherString);
        Weather weather = getWeatherFromJsonObject(weatherObject);

        JSONObject systemObject = weatherObject.getJSONObject("sys");
        setSunsetAndSunrise(weather, systemObject);

        int cityId = weatherObject.getInt("id");
        String cityName = weatherObject.getString("name");
        String country = systemObject.getString("country");

        setCityAndCountry(weather, cityId, cityName, country);

        JSONObject coordinatesObject = weatherObject.getJSONObject("coord");
        setCoordinates(weather, coordinatesObject);

        return weather;
    }

    public static double convertJsonToUVIndex(String uviString) throws JSONException {
        JSONObject jsonObject = new JSONObject(uviString);

        return jsonObject.getDouble("value");
    }

    private static Weather getWeatherFromJsonObject(JSONObject weatherObject) throws JSONException {
        Weather weather = new Weather();

        weather.setDate(new Date(weatherObject.getLong("dt") * 1000));

        JSONObject main = weatherObject.getJSONObject("main");
        weather.setTemperature(main.getDouble("temp"));
        weather.setPressure(main.getInt("pressure"));
        weather.setHumidity(main.getInt("humidity"));

        JSONObject weatherJson = weatherObject.getJSONArray("weather").getJSONObject(0);

        String capitalizedDescription = capitalize(weatherJson.getString("description"));
        weather.setDescription(capitalizedDescription);
        weather.setWeatherId(weatherJson.getInt("id"));

        setWind(weather, weatherObject.optJSONObject("wind"));
        setRain(weather, weatherObject.optJSONObject("rain"), weatherObject.optJSONObject("snow"));

        weather.setLastUpdated(Calendar.getInstance().getTimeInMillis());

        return weather;
    }

    private static void setWind(Weather weather, @Nullable JSONObject windObject) throws JSONException {
        if (windObject == null) {
            weather.setWind(0);
            weather.setWindDirectionDegree(null);
            return;
        }

        weather.setWind(windObject.getDouble("speed"));

        if (windObject.has("deg")) {
            weather.setWindDirectionDegree(windObject.getDouble("deg"));
        } else {
            Log.e("parseTodayJson", "No wind direction available");
            weather.setWindDirectionDegree(null);
        }
    }

    private static void setRain(Weather weather, @Nullable JSONObject rainObject, @Nullable JSONObject snowObject) throws JSONException {
        if (rainObject != null) {
            weather.setRain(getRain(rainObject));
            return;
        }

        if (snowObject != null) {
            weather.setRain(getRain(snowObject));
            return;
        }

        weather.setRain(0);
    }

    private static double getRain(@NonNull JSONObject rainObject) throws JSONException {
        if (rainObject.has("3h")) {
            return rainObject.getDouble("3h");
        }

        if (rainObject.has("1h")) {
            return rainObject.getDouble("1h");
        }

        return 0;
    }

    private static void setSunsetAndSunrise(Weather weather, @NonNull JSONObject systemObject) throws JSONException {
        if (systemObject.has("sunrise") && systemObject.has("sunset")) {
            weather.setSunrise(systemObject.getString("sunrise"));
            weather.setSunset(systemObject.getString("sunset"));
        }
    }

    private static void setCityAndCountry(Weather weather, int cityId, @NonNull String cityName, @NonNull String country) {
        weather.setCityId(cityId);
        weather.setCity(cityName);
        weather.setCountry(country);
    }

    private static void setCoordinates(Weather weather, @NonNull JSONObject coordinatesObject) throws JSONException {
        weather.setLat(coordinatesObject.getDouble("lat"));
        weather.setLon(coordinatesObject.getDouble("lon"));
    }

    private static String capitalize(String string) {
        if (string.isEmpty()) {
            return string;
        }

        return string.substring(0,1).toUpperCase() + string.substring(1);
    }
}
