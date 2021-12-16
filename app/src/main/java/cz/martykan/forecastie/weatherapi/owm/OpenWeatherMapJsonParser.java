package cz.martykan.forecastie.weatherapi.owm;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cz.martykan.forecastie.models.Weather;

public class OpenWeatherMapJsonParser {

    @NonNull
    public static List<Weather> convertJsonToWeatherList(String citiesString) throws JSONException {
        List<Weather> citiesWithWeather = new ArrayList<>();

        JSONObject weatherObject = new JSONObject(citiesString);
        JSONArray jsonArray = weatherObject.getJSONArray("list");
        JSONObject cityObject = weatherObject.getJSONObject("city");

        for (int i = 0; i < jsonArray.length(); i++) {
            Weather weather = convertJsonToWeather(jsonArray.getJSONObject(i).toString());
            weather.setCity(cityObject.getString("name"));
            weather.setCountry(cityObject.getString("country"));

            citiesWithWeather.add(weather);
        }

        return citiesWithWeather;
    }

    @NonNull
    public static Weather convertJsonToWeather(String weatherString) throws JSONException {
        Weather weather = new Weather();

        JSONObject weatherObject = new JSONObject(weatherString);
        JSONObject main = weatherObject.getJSONObject("main");

        weather.setDate(weatherObject.getString("dt"));
        weather.setTemperature(main.getDouble("temp"));

        String capitalizedDescription = capitalize(weatherObject.getJSONArray("weather").getJSONObject(0).getString("description"));
        weather.setDescription(capitalizedDescription);

        JSONObject windObj = weatherObject.optJSONObject("wind");
        if (windObj != null) {
            weather.setWind(windObj.getDouble("speed"));

            if (windObj.has("deg")) {
                weather.setWindDirectionDegree(windObj.getDouble("deg"));
            } else {
                Log.e("parseTodayJson", "No wind direction available");
                weather.setWindDirectionDegree(null);
            }
        }
        weather.setPressure(main.getInt("pressure"));
        weather.setHumidity(main.getInt("humidity"));

        JSONObject rainObj = weatherObject.optJSONObject("rain");
        double rain;
        if (rainObj != null) {
            rain = getRain(rainObj);
        } else {
            JSONObject snowObj = weatherObject.optJSONObject("snow");
            if (snowObj != null) {
                rain = getRain(snowObj);
            } else {
                rain = 0;
            }
        }
        weather.setRain(rain);

        int weatherId = weatherObject.getJSONArray("weather").getJSONObject(0).getInt("id");
        weather.setWeatherId(weatherId);

        // Only available on weather, but not on forecast
        JSONObject sysObj = weatherObject.optJSONObject("sys");
        if (sysObj != null && sysObj.has("sunrise") && sysObj.has("sunset")) {
            weather.setSunrise(sysObj.getString("sunrise"));
            weather.setSunset(sysObj.getString("sunset"));
        }

        // Only available on weather, but not on forecast
        if (sysObj != null && sysObj.has("country") && weatherObject.has("name") && sysObj.has("id")) {
            weather.setCountry(sysObj.getString("country"));
            weather.setCity(weatherObject.getString("name"));
            weather.setCityId(sysObj.getInt("id"));
        }

        // Only available on weather, but not on forecast
        JSONObject coordObj = weatherObject.optJSONObject("coord");
        if (coordObj != null) {
            weather.setLat(coordObj.getDouble("lat"));
            weather.setLon(coordObj.getDouble("lon"));
        }

        weather.setLastUpdated(Calendar.getInstance().getTimeInMillis());

        return weather;
    }

    public static double convertJsonToUVIndex(String uviString) throws JSONException {
        JSONObject jsonObject = new JSONObject(uviString);

        return jsonObject.getDouble("value");
    }

    private static double getRain(JSONObject rainObj) {
        double rain = 0;
        if (rainObj != null) {
            rain = rainObj.optDouble("3h");
            if (Double.isNaN(rain)) {
                rain = rainObj.optDouble("1h", 0);
            }
        }
        return rain;
    }

    private static String capitalize(String string)
    {
        if (string.length() < 1) {
            return string;
        }

        return string.substring(0,1).toUpperCase() + string.substring(1);
    }
}
