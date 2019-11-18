package cz.martykan.forecastie.utils;

import android.content.Context;

import cz.martykan.forecastie.R;

public class Formatting {

    private Context context;

    public Formatting(Context context) {
        this.context = context;
    }

    public String setWeatherIcon(int actualId, boolean day) {
        int id = actualId / 100;
        String icon = "";

        if (id == 2) {
            // thunderstorm
            switch (actualId) {
                case 210:
                case 211:
                case 212:
                case 221:
                    icon = context.getString(R.string.weather_lightning);
                    break;
                case 200:
                case 201:
                case 202:
                case 230:
                case 231:
                case 232:
                default:
                    icon = context.getString(R.string.weather_thunderstorm);
                    break;
            }
        } else if (id == 3) {
            // drizzle/sprinkle
            switch (actualId) {
                case 302:
                case 311:
                case 312:
                case 314:
                    icon = context.getString(R.string.weather_rain);
                    break;
                case 310:
                    icon = context.getString(R.string.weather_rain_mix);
                    break;
                case 313:
                    icon = context.getString(R.string.weather_showers);
                    break;
                case 300:
                case 301:
                case 321:
                default:
                    icon = context.getString(R.string.weather_sprinkle);
                    break;
            }
        } else if (id == 5) {
            // rain
            switch (actualId) {
                case 500:
                    icon = context.getString(R.string.weather_sprinkle);
                    break;
                case 511:
                    icon = context.getString(R.string.weather_rain_mix);
                    break;
                case 520:
                case 521:
                case 522:
                    icon = context.getString(R.string.weather_showers);
                    break;
                case 531:
                    icon = context.getString(R.string.weather_storm_showers);
                    break;
                case 501:
                case 502:
                case 503:
                case 504:
                default:
                    icon = context.getString(R.string.weather_rain);
                    break;
            }
        } else if (id == 6) {
            // snow
            switch (actualId) {
                case 611:
                    icon = context.getString(R.string.weather_sleet);
                    break;
                case 612:
                case 613:
                case 615:
                case 616:
                case 620:
                    icon = context.getString(R.string.weather_rain_mix);
                    break;
                case 600:
                case 601:
                case 602:
                case 621:
                case 622:
                default:
                    icon = context.getString(R.string.weather_snow);
                    break;
            }
        } else if (id == 7) {
            // atmosphere
            switch (actualId) {
                case 711:
                    icon = context.getString(R.string.weather_smoke);
                    break;
                case 721:
                    icon = context.getString(R.string.weather_day_haze);
                    break;
                case 731:
                case 761:
                case 762:
                    icon = context.getString(R.string.weather_dust);
                    break;
                case 751:
                    icon = context.getString(R.string.weather_sandstorm);
                    break;
                case 771:
                    icon = context.getString(R.string.weather_cloudy_gusts);
                    break;
                case 781:
                    icon = context.getString(R.string.weather_tornado);
                    break;
                case 701:
                case 741:
                default:
                    icon = context.getString(R.string.weather_fog);
                    break;
            }
        } else if (id == 8) {
            // clear sky or cloudy
            switch (actualId) {
                case 800:
                    icon = day ? context.getString(R.string.weather_day_sunny) : context.getString(R.string.weather_night_clear);
                    break;
                case 801:
                case 802:
                    icon = day ? context.getString(R.string.weather_day_cloudy) : context.getString(R.string.weather_night_alt_cloudy);
                    break;
                case 803:
                case 804:
                default:
                    icon = context.getString(R.string.weather_cloudy);
                    break;
            }
        } else if (id == 9) {
            switch (actualId) {
                case 900:
                    icon = context.getString(R.string.weather_tornado);
                    break;
                case 901:
                    icon = context.getString(R.string.weather_storm_showers);
                    break;
                case 902:
                    icon = context.getString(R.string.weather_hurricane);
                    break;
                case 903:
                    icon = context.getString(R.string.weather_snowflake_cold);
                    break;
                case 904:
                    icon = context.getString(R.string.weather_hot);
                    break;
                case 905:
                    icon = context.getString(R.string.weather_windy);
                    break;
                case 906:
                    icon = context.getString(R.string.weather_hail);
                    break;
                case 957:
                default:
                    icon = context.getString(R.string.weather_strong_wind);
                    break;
            }
        }

        return icon;
    }
}
