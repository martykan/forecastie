package cz.martykan.forecastie;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cz.martykan.forecastie.models.Weather;
import cz.martykan.forecastie.utils.UnitConvertor;
import cz.martykan.forecastie.weatherapi.WeatherStorage;
import nodomain.freeyourgadget.gadgetbridge.model.WeatherSpec;

public class Broadcaster {
    public static void sendWeather(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean run = sp.getBoolean("sendToGadgetbridge", false);

        if(run) {
            WeatherStorage storage = new WeatherStorage(context);

            Intent intent = new Intent();
            intent.setAction("de.kaffeemitkoffein.broadcast.WEATHERDATA");
            intent.setPackage("nodomain.freeyourgadget.gadgetbridge");
            intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.putExtra("WeatherSpec", generateWeatherSpec(storage));
            context.sendBroadcast(intent);
        }
    }

    private static WeatherSpec generateWeatherSpec(WeatherStorage storage) {
        WeatherSpec spec = new WeatherSpec();
        Weather weather = storage.getLastToday();
        List<Weather> multi = storage.getLastLongTerm();
        List<WeatherSpec.Forecast> forecasts = generateForecasts(weather, multi);

        spec.timestamp = (int) weather.getLastUpdated();
        spec.location = weather.getCity() + ", " +weather.getCountry();
        spec.currentTemp = (int) weather.getTemperature();  //Gadgetbridge does conversion. slight loss in accuracy here.
        spec.currentConditionCode = weather.getWeatherId();
        spec.currentCondition = weather.getDescription();
        spec.currentHumidity = weather.getHumidity();
        spec.todayMaxTemp =  forecasts.get(0).maxTemp;
        spec.todayMinTemp =  forecasts.get(0).minTemp;
        spec.windSpeed = (float) weather.getWind();   //km per hour.
        spec.windDirection = weather.getWindDirectionDegree().intValue();
        for (int i=1;i<forecasts.size();i++)
            spec.forecasts.add(forecasts.get(i));

        return spec;
    }

    private static List<WeatherSpec.Forecast> generateForecasts(Weather weather, List<Weather> manyWeathers) {
       List<WeatherSpec.Forecast> forecasts = new ArrayList<WeatherSpec.Forecast>();
       manyWeathers.add(0,weather);
       List<Weather> allWeathers = manyWeathers;

       //seperate weathers out based on day. assuming already ordered, no further sorting based on day is needed.
       List<List<Integer>> sortedWeathers = new ArrayList<List<Integer>>();
       Calendar currCal;
       Calendar currDate;
       int count=0;

       while(count<allWeathers.size()) {
           currCal = getWeatherCalendar(allWeathers.get(count));
           sortedWeathers.add(new ArrayList<Integer>());

           for(int i=0;i<allWeathers.size();i++) {
               currDate = getWeatherCalendar(allWeathers.get(i));
               if (currCal.get(Calendar.DATE) == currDate.get(Calendar.DATE)) {
                   sortedWeathers.get(sortedWeathers.size() - 1).add(i);
                   count++;
               }
           }
       }

       //2. get the 4 values based on day. min/max temp, average humidity, and weather cond.
       for(List<Integer> dayNumber : sortedWeathers)
       {
           ArrayList<Double> temps = new ArrayList<Double>();
           int totalHumid = 0;

           for (Integer occ : dayNumber) {
               temps.add(allWeathers.get(occ).getTemperature());
               totalHumid += allWeathers.get(occ).getHumidity();
           }

           int humidity = totalHumid / dayNumber.size();
           //TODO also the option to take mode of weather conditions for the day instead of just getting mid value
           int condition = allWeathers.get(dayNumber.get(dayNumber.size() / 2)).getWeatherId();
           double maxTemp = temps.get(0);
           double minTemp = temps.get(0);

           for(double temp : temps)
           {
               if(temp < minTemp)
                   minTemp = temp;
               if(temp > maxTemp)
                   maxTemp = temp;
           }

           WeatherSpec.Forecast forecast = new WeatherSpec.Forecast(
                   (int) minTemp,
                   (int) maxTemp,
                   condition,
                   humidity);
           forecasts.add(forecast);
       }
       return forecasts;
    }
    //method from LongTermWeatherList
    private static Calendar getWeatherCalendar(Weather weather) {
        Calendar weatherCalendar = Calendar.getInstance();
        weatherCalendar.setTimeInMillis(weather.getDate().getTime());

        return weatherCalendar;
    }
}
