package cz.martykan.forecastie.utils.localizers;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import cz.martykan.forecastie.R;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 27)
public class WindSpeedUnitsLocalizerTests {
    @Test
    public void localizeWindSpeedUnitsReturnsRightStringResourcesForAllUnits() {
        Assert.assertEquals("m/s result is wrong", R.string.speed_unit_mps,
                WindSpeedUnitsLocalizer.localizeWindSpeedUnits("m/s"));
        Assert.assertEquals("kph result is wrong", R.string.speed_unit_kph,
                WindSpeedUnitsLocalizer.localizeWindSpeedUnits("kph"));
        Assert.assertEquals("mph result is wrong", R.string.speed_unit_mph,
                WindSpeedUnitsLocalizer.localizeWindSpeedUnits("mph"));
        Assert.assertEquals("kn result is wrong", R.string.speed_unit_kn,
                WindSpeedUnitsLocalizer.localizeWindSpeedUnits("kn"));
    }

    @Test
    public void localizeWindSpeedUnitsThrowsIllegalArgumentExceptionForUnknownUnits() {
        Assert.assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                WindSpeedUnitsLocalizer.localizeWindSpeedUnits("");
            }
        });
    }

    @Test
    public void localizeWindSpeedUnitsReturnsRightStringsForAllUnits() {
        Context context = getApplicationContext();
        String expectedMpsUnits = context.getString(R.string.speed_unit_mps);
        String expectedKphUnits = context.getString(R.string.speed_unit_kph);
        String expectedMphUnits = context.getString(R.string.speed_unit_mph);
        String expectedKnUnits = context.getString(R.string.speed_unit_kn);

        String actualMpsUnits = WindSpeedUnitsLocalizer.localizeWindSpeedUnits("m/s", context);
        String actualKphUnits = WindSpeedUnitsLocalizer.localizeWindSpeedUnits("kph", context);
        String actualMphUnits = WindSpeedUnitsLocalizer.localizeWindSpeedUnits("mph", context);
        String actualKnUnits = WindSpeedUnitsLocalizer.localizeWindSpeedUnits("kn", context);

        Assert.assertEquals("m/s result is wrong", expectedMpsUnits, actualMpsUnits);
        Assert.assertEquals("kph result is wrong", expectedKphUnits, actualKphUnits);
        Assert.assertEquals("mph result is wrong", expectedMphUnits, actualMphUnits);
        Assert.assertEquals("kn result is wrong", expectedKnUnits, actualKnUnits);
    }

    @Test
    public void localizeWindSpeedUnitsThrowsIllegalArgumentExceptionForUnknownUnitsToo() {
        final Context context = getApplicationContext();

        Assert.assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                WindSpeedUnitsLocalizer.localizeWindSpeedUnits("abc", context);
            }
        });
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void localizeWindSpeedUnitsChecksParametersForNull() {
        final Context context = getApplicationContext();

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                WindSpeedUnitsLocalizer.localizeWindSpeedUnits(null);
            }
        });

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                WindSpeedUnitsLocalizer.localizeWindSpeedUnits(null, context);
            }
        });

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                WindSpeedUnitsLocalizer.localizeWindSpeedUnits("m/s", null);
            }
        });
    }
}
