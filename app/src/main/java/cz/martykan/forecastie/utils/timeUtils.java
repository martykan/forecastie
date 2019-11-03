package cz.martykan.forecastie.utils;

import java.util.Calendar;
import java.util.Date;

import cz.martykan.forecastie.models.Weather;

public class timeUtils {

    public static boolean isDayTime(Weather w){
        Calendar now = Calendar.getInstance();
        Date sunrise = w.getSunrise();
        Date sunset = w.getSunset();
        boolean day;
        if((sunrise != null) && (sunset != null))
            day = now.after(w.getSunrise()) && now.before(w.getSunset());
        else{
            // fallback
            int hourOfDay = now.get(Calendar.HOUR_OF_DAY);
            day = (hourOfDay >= 7 && hourOfDay < 20);
        }
        return day;
    }
}
