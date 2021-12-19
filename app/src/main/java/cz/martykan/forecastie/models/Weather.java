package cz.martykan.forecastie.models;

import android.content.Context;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cz.martykan.forecastie.R;

public class Weather {

    private String city;
    private int cityId;
    private String country;
    private Date date;
    private double temperature;
    private String description;
    private double wind;
    private Double windDirectionDegree;
    private int pressure;
    private int humidity;
    private double rain;
    private int weatherId;
    private long lastUpdated;
    private Date sunrise;
    private Date sunset;
    private double lat;
    private double lon;
    private double uvIndex;
    private double chanceOfPrecipitation;

    public enum WindDirection {
        // don't change order
        NORTH, NORTH_NORTH_EAST, NORTH_EAST, EAST_NORTH_EAST,
        EAST, EAST_SOUTH_EAST, SOUTH_EAST, SOUTH_SOUTH_EAST,
        SOUTH, SOUTH_SOUTH_WEST, SOUTH_WEST, WEST_SOUTH_WEST,
        WEST, WEST_NORTH_WEST, NORTH_WEST, NORTH_NORTH_WEST;

        public static WindDirection byDegree(double degree) {
            return byDegree(degree, WindDirection.values().length);
        }

        public static WindDirection byDegree(double degree, int numberOfDirections) {
            WindDirection[] directions = WindDirection.values();
            int availableNumberOfDirections = directions.length;

            int direction = windDirectionDegreeToIndex(degree, numberOfDirections)
                    * availableNumberOfDirections / numberOfDirections;

            return directions[direction];
        }

        public String getLocalizedString(Context context) {
            // usage of enum.ordinal() is not recommended, but whatever
            return context.getResources().getStringArray(R.array.windDirections)[ordinal()];
        }

        public String getArrow(Context context) {
            // usage of enum.ordinal() is not recommended, but whatever
            return context.getResources().getStringArray(R.array.windDirectionArrows)[ordinal() / 2];
        }
    }

    // you may use values like 4, 8, etc. for numberOfDirections
    public static int windDirectionDegreeToIndex(double degree, int numberOfDirections) {
        // to be on the safe side
        degree %= 360;
        if (degree < 0) degree += 360;

        degree += 180.0 / numberOfDirections; // add offset to make North start from 0

        int direction = (int) Math.floor(degree * numberOfDirections / 360);

        return direction % numberOfDirections;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public double getWind() {
        return wind;
    }

    public void setWind(double wind) {
        this.wind = wind;
    }

    public Double getWindDirectionDegree() {
        return windDirectionDegree;
    }

    public void setWindDirectionDegree(Double windDirectionDegree) {
        this.windDirectionDegree = windDirectionDegree;
    }

    public WindDirection getWindDirection() {
        return WindDirection.byDegree(windDirectionDegree);
    }

    public WindDirection getWindDirection(int numberOfDirections) {
        return WindDirection.byDegree(windDirectionDegree, numberOfDirections);
    }

    public boolean isWindDirectionAvailable() {
        return windDirectionDegree != null;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public Date getSunrise(){
        return this.sunrise;
    }

    public void setSunrise(String dateString) {
        try {
            setSunrise(new Date(Long.parseLong(dateString) * 1000));
        }
        catch (Exception e) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            try {
                setSunrise(inputFormat.parse(dateString));
            }
            catch (ParseException e2) {
                setSunrise(new Date()); // make the error somewhat obvious
                e2.printStackTrace();
            }
        }
    }

    public void setSunrise(Date date) {
        this.sunrise = date;
    }

    public Date getSunset(){
        return this.sunset;
    }

    public void setSunset(String dateString) {
        try {
            setSunset(new Date(Long.parseLong(dateString) * 1000));
        }
        catch (Exception e) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            try {
                setSunrise(inputFormat.parse(dateString));
            }
            catch (ParseException e2) {
                setSunset(new Date()); // make the error somewhat obvious
                e2.printStackTrace();
            }
        }
    }

    public void setSunset(Date date) {
        this.sunset = date;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLat() {
        return this.lat;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLon() {
        return this.lon;
    }

    public double getUvIndex() {
        return this.uvIndex;
    }

    public void setUvIndex(double uvIndex) {
        this.uvIndex = uvIndex;
    }

    public Date getDate(){
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getNumDaysFrom(Date initialDate) {
        Calendar initial = Calendar.getInstance();
        initial.setTime(initialDate);
        initial.set(Calendar.MILLISECOND, 0);
        initial.set(Calendar.SECOND, 0);
        initial.set(Calendar.MINUTE, 0);
        initial.set(Calendar.HOUR_OF_DAY, 0);

        Calendar me = Calendar.getInstance();
        me.setTime(this.date);
        me.set(Calendar.MILLISECOND, 0);
        me.set(Calendar.SECOND, 0);
        me.set(Calendar.MINUTE, 0);
        me.set(Calendar.HOUR_OF_DAY, 0);

        return Math.round((me.getTimeInMillis() - initial.getTimeInMillis()) / 86400000.0);
    }

    public int getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(int weatherId) {
        this.weatherId = weatherId;
    }

    public double getRain() {
        return rain;
    }

    public void setRain(double rain) {
        this.rain = rain;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public double getChanceOfPrecipitation() {
        return chanceOfPrecipitation;
    }

    public void setChanceOfPrecipitation(double chanceOfPrecipitation) {
        this.chanceOfPrecipitation = chanceOfPrecipitation;
    }
}
