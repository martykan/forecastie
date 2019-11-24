package cz.martykan.forecastie.utils;

import java.util.Calendar;
import java.util.Date;

import cz.martykan.forecastie.models.Weather;

public class TimeUtils {

    public static boolean isDayTime(Weather W, Calendar Cal){
        Date Sunrise = W.getSunrise();
        Date Sunset = W.getSunset();
        boolean day;
        if((Sunrise != null) && (Sunset != null)){
            Date currentTime = Calendar.getInstance().getTime();    // Cal is always set to midnight
                                                                    // then get real time
            day = currentTime.after(W.getSunrise()) && currentTime.before(W.getSunset());
        }
        else{
            // fallback
            int hourOfDay = Cal.get(Calendar.HOUR_OF_DAY);
            day = (hourOfDay >= 7 && hourOfDay < 20);
        }
        return day;
    }
}
