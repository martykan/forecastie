package cz.martykan.forecastie.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

import cz.martykan.forecastie.R;

public class UnitConvertor {
    public static float convertTemperature(float temperature, SharedPreferences sp) {
        String unit = sp.getString("unit", "°C");
        return convertTemperature(temperature, unit);
    }

    public static float convertTemperature(float temperature, String unit) {
        float result;
        switch (unit) {
            case "°C":
                result = UnitConvertor.kelvinToCelsius(temperature);
                break;
            case "°F":
                result = UnitConvertor.kelvinToFahrenheit(temperature);
                break;
            default:
                result = temperature;
                break;
        }
        return result;
    }

    public static float kelvinToCelsius(float kelvinTemp) {
        return kelvinTemp - 273.15f;
    }

    public static float kelvinToFahrenheit(float kelvinTemp) {
        return ((kelvinTemp - 273.15f) * 1.8f) + 32;
    }

    public static float convertRain(float rain, SharedPreferences sp) {
        if (sp.getString("lengthUnit", "mm").equals("mm")) {
            return rain;
        } else {
            return rain / 25.4f;
        }
    }

    public static String getRainString(double rain, double percentOfPrecipitation, SharedPreferences sp) {
        StringBuilder sb = new StringBuilder();
        if (rain > 0) {
            sb.append(" (");
            String lengthUnit = sp.getString("lengthUnit", "mm");
            boolean isMetric = lengthUnit.equals("mm");

            if (rain < 0.1) {
                sb.append(isMetric ? "<0.1" : "<0.01");
            } else if (isMetric) {
                sb.append(String.format(Locale.ENGLISH, "%.1f %s", rain, lengthUnit));
            } else {
                sb.append(String.format(Locale.ENGLISH, "%.2f %s", rain, lengthUnit));
            }

            if (percentOfPrecipitation > 0) {
                sb.append(", ").append(String.format(Locale.ENGLISH, "%d%%", (int) (percentOfPrecipitation * 100)));
            }

            sb.append(")");
        }

        return sb.toString();
    }

    public static float convertPressure(float pressure, SharedPreferences sp) {
        if (sp.getString("pressureUnit", "hPa").equals("kPa")) {
            return pressure / 10;
        } else if (sp.getString("pressureUnit", "hPa").equals("mm Hg")) {
            return (float) (pressure * 0.750061561303);
        } else if (sp.getString("pressureUnit", "hPa").equals("in Hg")) {
            return (float) (pressure * 0.0295299830714);
        } else {
            return pressure;
        }
    }

    public static double convertPressure(double pressure, String unit) {
        double result;
        switch (unit) {
            case "kPa":
                result = pressure / 10;
                break;
            case "mm Hg":
                result = pressure * 0.750061561303;
                break;
            case "in Hg":
                result = pressure * 0.0295299830714;
                break;
            default:
                result = pressure;
                break;
        }
        return result;
    }

    public static double convertWind(double wind, SharedPreferences sp) {
        double result;
        String unit = sp.getString("speedUnit", "m/s");
        switch (unit) {
            case "kph":
                result = wind * 3.6;
                break;
            case "mph":
                result = wind * 2.23693629205;
                break;
            case "kn":
                result = wind * 1.943844;
                break;
            case "bft":
                result = convertWindIntoBFT(wind);
                break;
            default:
                result = wind;
                break;
        }
        return result;
    }

    public static double convertWind(double wind, String unit) {
        double result;
        switch (unit) {
            case "kph":
                result = wind * 3.6;
                break;
            case "mph":
                result = wind * 2.23693629205;
                break;
            case "kn":
                result = wind * 1.943844;
                break;
            case "bft":
                result = convertWindIntoBFT(wind);
                break;
            default:
                result = wind;
                break;
        }
        return result;
    }

    private static double convertWindIntoBFT(double wind) {
        int result;
        if (wind < 0.3) {
            result = 0; // Calm
        } else if (wind < 1.5) {
            result =  1; // Light air
        } else if (wind < 3.3) {
            result =  2; // Light breeze
        } else if (wind < 5.5) {
            result =  3; // Gentle breeze
        } else if (wind < 7.9) {
            result =  4; // Moderate breeze
        } else if (wind < 10.7) {
            result =  5; // Fresh breeze
        } else if (wind < 13.8) {
            result =  6; // Strong breeze
        } else if (wind < 17.1) {
            result =  7; // High wind
        } else if (wind < 20.7) {
            result =  8; // Gale
        } else if (wind < 24.4) {
            result =  9; // Strong gale
        } else if (wind < 28.4) {
            result =  10; // Storm
        } else if (wind < 32.6) {
            result =  11; // Violent storm
        } else {
            result =  12; // Hurricane
        }
        return result;
    }

    public static String convertUvIndexToRiskLevel(double value, Context context) {
        /* based on: https://en.wikipedia.org/wiki/Ultraviolet_index */
        if (value >= 0.0 && value < 3.0) {
            return context.getString(R.string.uvi_low);
        } else if (value >= 3.0 && value < 6.0) {
            return context.getString(R.string.uvi_moderate);
        } else if (value >= 6.0 && value < 8.0) {
            return context.getString(R.string.uvi_high);
        } else if (value >= 8.0 && value < 11.0) {
            return context.getString(R.string.uvi_very_high);
        } else if (value >= 11.0) {
            return context.getString(R.string.uvi_extreme);
        } else {
            return context.getString(R.string.uvi_no_info);
        }
    }

    public static String getBeaufortName(int wind, Context context) {
        if (wind == 0) {
            return context.getString(R.string.beaufort_calm);
        } else if (wind == 1) {
            return context.getString(R.string.beaufort_light_air);
        } else if (wind == 2) {
            return context.getString(R.string.beaufort_light_breeze);
        } else if (wind == 3) {
            return context.getString(R.string.beaufort_gentle_breeze);
        } else if (wind == 4) {
            return context.getString(R.string.beaufort_moderate_breeze);
        } else if (wind == 5) {
            return context.getString(R.string.beaufort_fresh_breeze);
        } else if (wind == 6) {
            return context.getString(R.string.beaufort_strong_breeze);
        } else if (wind == 7) {
            return context.getString(R.string.beaufort_high_wind);
        } else if (wind == 8) {
            return context.getString(R.string.beaufort_gale);
        } else if (wind == 9) {
            return context.getString(R.string.beaufort_strong_gale);
        } else if (wind == 10) {
            return context.getString(R.string.beaufort_storm);
        } else if (wind == 11) {
            return context.getString(R.string.beaufort_violent_storm);
        } else {
            return context.getString(R.string.beaufort_hurricane);
        }
    }
}
