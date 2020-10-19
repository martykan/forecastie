package cz.martykan.forecastie.utils.localizers;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import cz.martykan.forecastie.models.Weather;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 27)
public class WindDirectionLocalizerTests {
    @Test
    public void localizeWindDirectionAppliesFormat() {
        Context context = getApplicationContext();

        String actualESE = WindDirectionLocalizer.localizeWindDirection(
                Weather.WindDirection.EAST_SOUTH_EAST, "abbr", context);
        String actualNorthEastArrow = WindDirectionLocalizer.localizeWindDirection(
                Weather.WindDirection.NORTH_EAST, "arrow", context);
        String actualNone = WindDirectionLocalizer.localizeWindDirection(
                Weather.WindDirection.EAST, "none", context);

        Assert.assertEquals("abbreviation for east south east is wrong",
                "ESE", actualESE);
        Assert.assertEquals("arrow for north east is wrong",
                "↙", actualNorthEastArrow);
        Assert.assertEquals("result for none format should be empty string",
                "", actualNone);
    }

    @Test
    @Config(qualifiers = "be")
    public void localizeWindDirectionAppliesFormatAndLocalizeAbbreviation() {
        Context context = getApplicationContext();

        String actualSSW = WindDirectionLocalizer.localizeWindDirection(
                Weather.WindDirection.SOUTH_SOUTH_WEST, "abbr", context);
        String actualWNWArrow = WindDirectionLocalizer.localizeWindDirection(
                Weather.WindDirection.WEST_NORTH_WEST, "arrow", context);

        Assert.assertEquals("abbreviation for east south east is wrong",
                "ЮЮЗ", actualSSW);
        Assert.assertEquals("arrow for west north west is wrong",
                "→", actualWNWArrow);
    }

    @Test
    public void localizeWindDirectionThrowExceptionForUnknownFormat() {
        final Context context = getApplicationContext();

        Assert.assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                WindDirectionLocalizer.localizeWindDirection(Weather.WindDirection.SOUTH,
                        "", context);
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void localizeWindDirectionChecksParametersForNull() {
        final Context context = getApplicationContext();

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                WindDirectionLocalizer.localizeWindDirection(null, "abbr", context);
            }
        });

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                WindDirectionLocalizer.localizeWindDirection(Weather.WindDirection.SOUTH,
                        null, context);
            }
        });

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                WindDirectionLocalizer.localizeWindDirection(Weather.WindDirection.SOUTH,
                        "arrow", null);
            }
        });
    }
}
