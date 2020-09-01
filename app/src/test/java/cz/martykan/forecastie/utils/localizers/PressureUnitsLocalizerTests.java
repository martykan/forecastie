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
public class PressureUnitsLocalizerTests {
    @Test
    public void localizePressureUnitsReturnsRightStringResourcesForAllUnits() {
        Assert.assertEquals("hPa/mBar (\"hPa/mBar\") result is wrong", R.string.pressure_unit_hpa,
                PressureUnitsLocalizer.localizePressureUnits("hPa/mBar"));
        Assert.assertEquals("hPa/mBar (\"hPa\") result is wrong", R.string.pressure_unit_hpa,
                PressureUnitsLocalizer.localizePressureUnits("hPa"));
        Assert.assertEquals("kPa result is wrong", R.string.pressure_unit_kpa,
                PressureUnitsLocalizer.localizePressureUnits("kPa"));
        Assert.assertEquals("mm Hg result is wrong", R.string.pressure_unit_mmhg,
                PressureUnitsLocalizer.localizePressureUnits("mm Hg"));
        Assert.assertEquals("in Hg result is wrong", R.string.pressure_unit_inhg,
                PressureUnitsLocalizer.localizePressureUnits("in Hg"));
    }

    @Test
    public void localizePressureUnitsThrowsIllegalArgumentExceptionForUnknownUnits() {
        Assert.assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                WindSpeedUnitsLocalizer.localizeWindSpeedUnits("");
            }
        });
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Test
    @Config(qualifiers = "ko")
    public void localizePressureUnitsReturnsRightStringsForAllUnits() {
        Context context = getApplicationContext();
        String expectedHpaUnits = context.getString(R.string.pressure_unit_hpa);
        String expectedKpaUnits = context.getString(R.string.pressure_unit_kpa);
        String expectedMmhgUnits = context.getString(R.string.pressure_unit_mmhg);
        String expectedInhgUnits = context.getString(R.string.pressure_unit_inhg);

        String actualHpapmbarUnits =
                PressureUnitsLocalizer.localizePressureUnits("hPa/mBar", context);
        String actualHpaUnits = PressureUnitsLocalizer.localizePressureUnits("hPa", context);
        String actualKpaUnits = PressureUnitsLocalizer.localizePressureUnits("kPa", context);
        String actualMmhgUnits =
                PressureUnitsLocalizer.localizePressureUnits("mm Hg", context);
        String actualInhgUnits =
                PressureUnitsLocalizer.localizePressureUnits("in Hg", context);

        Assert.assertEquals("hPa/mBar (\"hPa/mBar\") result is wrong",
                expectedHpaUnits, actualHpapmbarUnits);
        Assert.assertEquals("hPa/mBar (\"hPa\") result is wrong",
                expectedHpaUnits, actualHpaUnits);
        Assert.assertEquals("kPa result is wrong", expectedKpaUnits, actualKpaUnits);
        Assert.assertEquals("mm Hg result is wrong", expectedMmhgUnits, actualMmhgUnits);
        Assert.assertEquals("in Hg result is wrong", expectedInhgUnits, actualInhgUnits);
    }

    @Test
    public void localizePressureUnitsThrowsIllegalArgumentExceptionForUnknownUnitsToo() {
        final Context context = getApplicationContext();

        Assert.assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                PressureUnitsLocalizer.localizePressureUnits("abc", context);
            }
        });
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void localizePressureUnitsChecksParametersForNull() {
        final Context context = getApplicationContext();

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                PressureUnitsLocalizer.localizePressureUnits(null);
            }
        });

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                PressureUnitsLocalizer.localizePressureUnits(null, context);
            }
        });

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                PressureUnitsLocalizer.localizePressureUnits("kPa", null);
            }
        });
    }
}
