package cz.martykan.forecastie.utils.formatters;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import cz.martykan.forecastie.models.ImmutableWeather;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 27)
public class DescriptionFormatterTests {
    @Test
    public void getDescriptionReturnsDescriptionWithFirstUpperLetter() {
        String json = "{\"weather\": [{\"description\": \"clear sky\"}]}";
        ImmutableWeather weather = ImmutableWeather.fromJson(json, -1);

        Assert.assertEquals("description is wrong", "Clear sky",
                DescriptionFormatter.getDescription(weather));
    }

    @Test
    public void getDescriptionReturnsDoesNotTryToChangeEmptyString() {
        Assert.assertEquals("description is wrong", "",
                DescriptionFormatter.getDescription(ImmutableWeather.EMPTY));
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void getDescriptionChecksParametersForNull() {
        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                DescriptionFormatter.getDescription(null);
            }
        });
    }
}
