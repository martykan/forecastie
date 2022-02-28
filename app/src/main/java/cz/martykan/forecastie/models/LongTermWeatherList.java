package cz.martykan.forecastie.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LongTermWeatherList {
    private final List<Weather> longTermWeatherList = new ArrayList<>();

    public List<Weather> getToday() {
        List<Weather> todayList = new ArrayList<>();
        Calendar tomorrowCalendar = getTomorrowCalendar();

        for (Weather weather : this.longTermWeatherList) {
            Calendar weatherCalendar = getWeatherCalendar(weather);
            if (weatherCalendar.compareTo(tomorrowCalendar) < 0) {
                todayList.add(weather);
            }
        }

        return todayList;
    }

    public List<Weather> getTomorrow() {
        List<Weather> tomorrowList = new ArrayList<>();
        Calendar tomorrowCalendar = getTomorrowCalendar();
        Calendar laterCalendar = getLaterCalendar();

        for (Weather weather : this.longTermWeatherList) {
            Calendar weatherCalendar = getWeatherCalendar(weather);
            if (weatherCalendar.compareTo(tomorrowCalendar) >= 0 && weatherCalendar.compareTo(laterCalendar) < 0) {
                tomorrowList.add(weather);
            }
        }

        return tomorrowList;
    }

    public List<Weather> getLater() {
        List<Weather> laterList = new ArrayList<>();
        Calendar laterCalendar = getLaterCalendar();

        for (Weather weather : this.longTermWeatherList) {
            Calendar weatherCalendar = getWeatherCalendar(weather);
            if (weatherCalendar.compareTo(laterCalendar) >= 0) {
                laterList.add(weather);
            }
        }

        return laterList;
    }

    public void addAll(List<Weather> longTermWeather) {
        this.longTermWeatherList.addAll(longTermWeather);
    }

    public void clear() {
        this.longTermWeatherList.clear();
    }

    private Calendar getTodayCalendar() {
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);

        return todayCalendar;
    }

    private Calendar getTomorrowCalendar() {
        Calendar tomorrowCalendar = getTodayCalendar();
        tomorrowCalendar.add(Calendar.DAY_OF_YEAR, 1);

        return tomorrowCalendar;
    }

    private Calendar getLaterCalendar() {
        Calendar laterCalendar = getTodayCalendar();
        laterCalendar.add(Calendar.DAY_OF_YEAR, 2);

        return laterCalendar;
    }

    private Calendar getWeatherCalendar(Weather weather) {
        Calendar weatherCalendar = Calendar.getInstance();
        weatherCalendar.setTimeInMillis(weather.getDate().getTime());

        return weatherCalendar;
    }
}
