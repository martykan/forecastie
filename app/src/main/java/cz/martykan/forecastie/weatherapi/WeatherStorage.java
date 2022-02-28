package cz.martykan.forecastie.weatherapi;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONException;

import java.util.List;

import cz.martykan.forecastie.Constants;
import cz.martykan.forecastie.models.Weather;
import cz.martykan.forecastie.weatherapi.owm.OpenWeatherMapJsonParser;

public class WeatherStorage {
    protected SharedPreferences sharedPreferences;

    public WeatherStorage(Context context) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public int getCityId() {
        String cityIdString = sharedPreferences.getString("cityId", Constants.DEFAULT_CITY_ID);
        return Integer.parseInt(cityIdString);
    }

    public void setCityId(int cityId) {
        String cityIdString = Integer.toString(cityId);
        sharedPreferences.edit().putString("cityId", cityIdString).apply();
    }

    @Nullable
    public Weather getLastToday() {
        String lastToday = this.sharedPreferences.getString("lastToday", null);
        if (lastToday == null) {
            return null;
        }

        try {
            return OpenWeatherMapJsonParser.convertJsonToWeather(lastToday);
        } catch (JSONException e) {
            Log.e("WeatherStorage", "Could not parse today JSON", e);
            e.printStackTrace();
            return null;
        }
    }

    public void setLastToday(String lastToday) {
        this.sharedPreferences.edit().putString("lastToday", lastToday).apply();
    }

    @Nullable
    public List<Weather> getLastLongTerm() {
        String lastLongTerm = this.sharedPreferences.getString("lastLongterm", null);
        if (null == lastLongTerm) {
            return null;
        }

        try {
            return OpenWeatherMapJsonParser.convertJsonToWeatherList(lastLongTerm);
        } catch (JSONException e) {
            Log.e("WeatherStorage", "Could not parse long term JSON", e);
            e.printStackTrace();
            return null;
        }
    }


    public void setLastLongTerm(String lastLongTerm) {
        this.sharedPreferences.edit().putString("lastLongterm", lastLongTerm).apply();
    }

    @Nullable
    public Double getLastUviToday() {
        String lastUviToday = this.sharedPreferences.getString("lastUVIToday", null);
        if (null == lastUviToday) {
            return null;
        }

        try {
            return OpenWeatherMapJsonParser.convertJsonToUVIndex(lastUviToday);
        } catch (JSONException e) {
            Log.e("WeatherStorage", "Could not parse UV index JSON", e);
            e.printStackTrace();
            return null;
        }
    }

    public void setLastUviToday(String lastUviToday) {
        this.sharedPreferences.edit().putString("lastUVIToday", lastUviToday).apply();
    }

    @Nullable
    public Double getLatitude() {
        if (this.sharedPreferences.contains("latitude")) {
            return (double) this.sharedPreferences.getFloat("latitude", 0);
        } else {
            return null;
        }
    }

    public double getLatitude(double defaultValue) {
        Double latitude = this.getLatitude();
        return latitude != null ? latitude : defaultValue;
    }

    public void setLatitude(double latitude) {
        this.sharedPreferences.edit().putFloat("latitude", (float) latitude).apply();
    }

    @Nullable
    public Double getLongitude() {
        if (this.sharedPreferences.contains("longitude")) {
            return (double) this.sharedPreferences.getFloat("longitude", 0);
        } else {
            return null;
        }
    }

    public double getLongitude(double defaultValue) {
        Double longitude = this.getLongitude();
        return longitude != null ? longitude : defaultValue;
    }

    public void setLongitude(double longitude) {
        this.sharedPreferences.edit().putFloat("longitude", (float) longitude).apply();

    }
}
