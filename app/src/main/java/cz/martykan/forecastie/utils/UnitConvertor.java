package cz.martykan.forecastie.utils;

import android.content.SharedPreferences;

import java.util.Locale;

public class UnitConvertor {
    public static float convertTemperature(float temperature, SharedPreferences sp) {
        if (sp.getString("unit", "C").equals("C")) {
            return UnitConvertor.kelvinToCelsius(temperature);
        } else if (sp.getString("unit", "C").equals("F")) {
            return UnitConvertor.kelvinToFahrenheit(temperature);
        } else {
            return temperature;
        }
    }

    public static float kelvinToCelsius(float kelvinTemp) {
        return kelvinTemp - 273.15f;
    }

    public static float kelvinToFahrenheit(float kelvinTemp) {
        return (((9 * kelvinToCelsius(kelvinTemp)) / 5) + 32);
    }

    public static String getRainString(double rain, SharedPreferences sp) {
        if (rain > 0) {
            if (sp.getString("lengthUnit", "mm").equals("mm")) {
                if (rain < 0.1) {
                    return " (<0.1 mm)";
                } else {
                    return String.format(Locale.ENGLISH, " (%.1f %s)", rain, sp.getString("lengthUnit", "mm"));
                }
            } else {
                rain = rain / 25.4;
                if (rain < 0.01) {
                    return " (<0.01 in)";
                } else {
                    return String.format(Locale.ENGLISH, " (%.2f %s)", rain, sp.getString("lengthUnit", "mm"));
                }
            }
        } else {
            return "";
        }
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

    public static double convertWind(double wind, SharedPreferences sp) {
        if (sp.getString("speedUnit", "m/s").equals("kph")) {
            return wind * 3.59999999712;
        }
        else if (sp.getString("speedUnit", "m/s").equals("mph")) {
            return wind * 2.23693629205;
        }
        else if (sp.getString("speedUnit", "m/s").equals("bft")) {
            if(wind < 0.3) {
                return 0; // Calm
            }
            else if (wind < 1.5) {
                return 1; // Light air
            }
            else if (wind < 3.3) {
                return 2; // Light breeze
            }
            else if (wind < 5.5) {
                return 3; // Gentle breeze
            }
            else if (wind < 7.9) {
                return 4; // Moderate breeze
            }
            else if (wind < 10.7) {
                return 5; // Fresh breeze
            }
            else if (wind < 13.8) {
                return 6; // Strong breeze
            }
            else if (wind < 17.1) {
                return 7; // High wind
            }
            else if (wind < 20.7) {
                return 8; // Gale
            }
            else if (wind < 24.4) {
                return 9; // Strong gale
            }
            else if (wind < 28.4) {
                return 10; // Storm
            }
            else if (wind < 32.6) {
                return 11; // Violent storm
            }
            else {
                return 12; // Hurricane
            }
        }
        else {
            return wind;
        }
    }

    public static String getBeaufortName(int wind) {
        if(wind == 0) {
            return "Calm";
        }
        else if (wind == 1) {
            return "Light air";
        }
        else if (wind == 2) {
            return "Light breeze";
        }
        else if (wind == 3) {
            return "Gentle breeze";
        }
        else if (wind == 4) {
            return "Moderate breeze";
        }
        else if (wind == 5) {
            return "Fresh breeze";
        }
        else if (wind == 6) {
            return "Strong breeze";
        }
        else if (wind == 7) {
            return "High wind";
        }
        else if (wind == 8) {
            return "Gale";
        }
        else if (wind == 9) {
            return "Strong gale";
        }
        else if (wind == 10) {
            return "Storm";
        }
        else if (wind == 11) {
            return "Violent storm";
        }
        else {
            return  "Hurricane";
        }
    }
}
