package cz.martykan.forecastie;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Weather {
    private String city;
    private String country;
    private Date date;
    private String temperature;
    private String description;
    private String wind;
    private String pressure;
    private String humidity;
    private String rain;
    private String id;
    private String icon;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }


    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Date getDate(){
        return this.date;
    }

    public void setDate(String dateString) {
        try {
            setDate(new Date(Long.parseLong(dateString) * 1000));
        }
        catch (Exception e) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            try {
                setDate(inputFormat.parse(dateString));
            }
            catch (ParseException e2) {
                setDate(new Date()); // make the error somewhat obvious
                e2.printStackTrace();
            }
        }
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
        initial.set(Calendar.HOUR, 0);

        Calendar me = Calendar.getInstance();
        me.setTime(this.date);
        me.set(Calendar.MILLISECOND, 0);
        me.set(Calendar.SECOND, 0);
        me.set(Calendar.MINUTE, 0);
        me.set(Calendar.HOUR, 0);

        return Math.round((me.getTimeInMillis() - initial.getTimeInMillis()) / 86400000.0);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRain() {
        return rain;
    }

    public void setRain(String rain) {
        this.rain = rain;
    }
}