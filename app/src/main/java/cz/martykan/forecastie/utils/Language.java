package cz.martykan.forecastie.utils;

import java.util.Locale;

public class Language {

    /**
     * Returns the language code used by OpenWeatherMap corresponding to the system's default
     * language.
     * @return language code
     */
    public static String getOwmLanguage() {
        String language = Locale.getDefault().getLanguage();

        if (language.equals(new Locale("cs").getLanguage())) { // Czech
            return "cz";
        } else if (language.equals(new Locale("ko").getLanguage())) { // Korean
            return "kr";
        } else if (language.equals(new Locale("lv").getLanguage())) { // Latvian
            return "la";
        } else {
            return language;
        }
    }

}
