package cz.martykan.forecastie.utils.localizers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import cz.martykan.forecastie.R;

/**
 * Class to localize (translate) wind speed units to current locale.
 */
// TODO replace "singleton" with DI
public abstract class WindSpeedUnitsLocalizer {
    /**
     * Localize {@code units} to current locale.
     * @param units wind speed units
     * @return string resource for {@code units}
     * @throws NullPointerException if {@code units} is null
     * @throws IllegalArgumentException if {@code units} have value other than "m/s", "kph", "mph" or "kn"
     */
    // TODO replace String with enum
    @StringRes
    public static int localizeWindSpeedUnits(@NonNull String units)
            throws NullPointerException, IllegalArgumentException {
        //noinspection ConstantConditions
        if (units == null)
            throw new NullPointerException("units should not be null");

        switch (units) {
            case "m/s": return R.string.speed_unit_mps;
            case "kph": return R.string.speed_unit_kph;
            case "mph": return R.string.speed_unit_mph;
            case "kn": return R.string.speed_unit_kn;
            default: throw new IllegalArgumentException("Unknown units: \"" + units + "\"");
        }
    }

    /**
     * Localize {@code units} to current locale.
     * @param units wind speed units
     * @param context android context
     * @return string for {@code units}
     * @throws NullPointerException if any of parameters is null
     * @throws IllegalArgumentException if {@code units} have value other than "m/s", "kph", "mph" or "kn"
     */
    // TODO replace String with enum
    @NonNull
    public static String localizeWindSpeedUnits(@NonNull String units, @NonNull Context context)
            throws NullPointerException, IllegalArgumentException{
        //noinspection ConstantConditions
        if (units == null)
            throw new NullPointerException("units should not be null");
        //noinspection ConstantConditions
        if (context == null)
            throw new NullPointerException("context should not be null");

        int unitsResourceId = localizeWindSpeedUnits(units);
        return context.getString(unitsResourceId);
    }
}
