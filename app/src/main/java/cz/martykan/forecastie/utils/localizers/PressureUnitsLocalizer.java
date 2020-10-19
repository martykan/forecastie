package cz.martykan.forecastie.utils.localizers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import cz.martykan.forecastie.R;

/**
 * Class to localize (translate) pressure units to current locale.
 */
// TODO replace "singleton" with DI
public abstract class PressureUnitsLocalizer {
    /**
     * Localize {@code units} to current locale.
     * @param units pressure units
     * @return string resource for {@code units}
     * @throws NullPointerException if {@code units} is null
     * @throws IllegalArgumentException if {@code units} have value other than "hPa", "hPa/mBar",
     * "kPa", "mm Hg" or "in Hg"
     */
    // TODO replace String with enum
    @StringRes
    public static int localizePressureUnits(@NonNull String units)
            throws NullPointerException, IllegalArgumentException {
        //noinspection ConstantConditions
        if (units == null)
            throw new NullPointerException("units should not be null");

        switch (units) {
            // hPa added for future compatibility with other code where it is used as default value
            case "hPa":
            case "hPa/mBar":
                return R.string.pressure_unit_hpa;
            case "kPa":
                return R.string.pressure_unit_kpa;
            case "mm Hg":
                return R.string.pressure_unit_mmhg;
            case "in Hg":
                return R.string.pressure_unit_inhg;
            default:
                throw new IllegalArgumentException("Unknown units: \"" + units + "\"");
        }
    }

    /**
     * Localize {@code units} to current locale.
     * @param units pressure units
     * @param context android context
     * @return string for {@code units}
     * @throws NullPointerException if any of parameters is null
     * @throws IllegalArgumentException if {@code units} have value other than "hPa", "hPa/mBar",
     * "kPa", "mm Hg" or "in Hg"
     */
    // TODO replace String with enum
    @NonNull
    public static String localizePressureUnits(@NonNull String units, @NonNull Context context)
            throws NullPointerException, IllegalArgumentException {
        //noinspection ConstantConditions
        if (units == null)
            throw new NullPointerException("units should not be null");
        //noinspection ConstantConditions
        if (context == null)
            throw new NullPointerException("context should not be null");

        int unitsResourceId = localizePressureUnits(units);
        return context.getString(unitsResourceId);
    }
}
