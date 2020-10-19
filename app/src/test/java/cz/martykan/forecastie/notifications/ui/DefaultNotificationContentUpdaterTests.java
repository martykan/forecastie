package cz.martykan.forecastie.notifications.ui;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.core.app.NotificationCompat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.models.ImmutableWeather;
import cz.martykan.forecastie.models.WeatherPresentation;
import cz.martykan.forecastie.notifications.ui.DefaultNotificationContentUpdater;
import cz.martykan.forecastie.utils.formatters.WeatherFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultNotificationContentUpdaterTests {
    @Mock private Context contextMock;
    @Mock private WeatherFormatter formatterMock;
    @Mock private ImmutableWeather weatherMock;
    private NotificationCompat.Builder notificationSpy;

    private WeatherPresentation weatherPresentation;
    private DefaultNotificationContentUpdater contentUpdater;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        contentUpdater = new DefaultNotificationContentUpdater(formatterMock);

        notificationSpy = spy(new NotificationCompat.Builder(contextMock, "channel"));

        weatherPresentation = new WeatherPresentation();
    }

    @Test
    public void isLayoutCustomReturnsTrue() {
        boolean actual = contentUpdater.isLayoutCustom();

        Assert.assertFalse("default notification should has default layout", actual);
    }

    @Test
    public void titleHasSetWithTemperature() {
        String expectedTemperature = "22°C";
        when(formatterMock.isEnoughValidData(same(weatherMock))).thenReturn(true);
        when(formatterMock.getTemperature(same(weatherMock), anyString(), anyBoolean()))
                .thenReturn(expectedTemperature);

        weatherPresentation = weatherPresentation.copy(weatherMock);
        contentUpdater.updateNotification(weatherPresentation, notificationSpy, contextMock);

        verify(notificationSpy).setContentTitle(eq(expectedTemperature));

        String expectedTemperatureUnits = "K";
        weatherPresentation = weatherPresentation.copy(true).copyTemperatureUnits(expectedTemperatureUnits);
        contentUpdater.updateNotification(weatherPresentation, notificationSpy, contextMock);

        verify(formatterMock).getTemperature(eq(weatherMock), eq(expectedTemperatureUnits),
                eq(true));
    }

    @Test
    public void textHasSetWithDescription() {
        String expectedDescription = "clear sky";
        when(formatterMock.isEnoughValidData(same(weatherMock))).thenReturn(true);
        when(formatterMock.getDescription(same(weatherMock)))
                .thenReturn(expectedDescription);

        weatherPresentation = weatherPresentation.copy(weatherMock);
        contentUpdater.updateNotification(weatherPresentation, notificationSpy, contextMock);

        verify(notificationSpy).setContentText(eq(expectedDescription));
    }

    @Test
    public void largeIconHasSetWithWeatherIcon() {
        Bitmap expectedIcon = mock(Bitmap.class);
        when(formatterMock.isEnoughValidData(same(weatherMock))).thenReturn(true);
        when(formatterMock.getWeatherIconAsBitmap(same(weatherMock), same(contextMock)))
                .thenReturn(expectedIcon);
        when(notificationSpy.setLargeIcon(any(Bitmap.class))).thenReturn(notificationSpy);

        weatherPresentation = weatherPresentation.copy(weatherMock);;
        contentUpdater.updateNotification(weatherPresentation, notificationSpy, contextMock);

        verify(notificationSpy).setLargeIcon(expectedIcon);
    }

    @Test
    public void noDataStateHasSetIfThereIsNoEnoughData() {
        String expectedString = "no data";
        when(contextMock.getString(R.string.no_data)).thenReturn(expectedString);
        when(formatterMock.isEnoughValidData(same(weatherMock))).thenReturn(false);

        weatherPresentation = weatherPresentation.copy(weatherMock);;
        contentUpdater.updateNotification(weatherPresentation, notificationSpy, contextMock);

        verify(notificationSpy).setContentTitle(eq(expectedString));
        verify(notificationSpy).setContentText((String) isNull());
        verify(notificationSpy).setLargeIcon((Bitmap) isNull());
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void implementedMethodsChecksForNull() {
        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                contentUpdater.updateNotification(null, notificationSpy, contextMock);
            }
        });

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                contentUpdater.updateNotification(weatherPresentation, null, contextMock);
            }
        });

        Assert.assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                contentUpdater.updateNotification(weatherPresentation, notificationSpy, null);
            }
        });
    }
}