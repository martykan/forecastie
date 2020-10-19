package cz.martykan.forecastie.notifications.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.models.ImmutableWeather;
import cz.martykan.forecastie.models.WeatherPresentation;
import cz.martykan.forecastie.notifications.ui.SimpleNotificationContentUpdater;
import cz.martykan.forecastie.utils.formatters.WeatherSimpleNotificationFormatter;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 27)
public class SimpleNotificationContentUpdaterTests {
    private Context context = getApplicationContext();
    @Mock private WeatherSimpleNotificationFormatter formatterMock;
    @Mock private ImmutableWeather weatherMock;
    private NotificationCompat.Builder notificationSpy;
    private RemoteViews layoutSpy;

    private WeatherPresentation weatherPresentation;
    private SimpleNotificationContentUpdater contentUpdater;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        contentUpdater = new SimpleNotificationContentUpdater(formatterMock);

        layoutSpy = spy(new RemoteViews(context.getPackageName(), R.layout.notification_simple));
        notificationSpy = spy(new NotificationCompat.Builder(context, "channel"));

        weatherPresentation = new WeatherPresentation();
    }

    @Test
    public void isLayoutCustomReturnsTrue() {
        boolean actual = contentUpdater.isLayoutCustom();

        Assert.assertTrue("simple notification should has custom layout", actual);
    }

    @Test
    public void prepareViewCreatesRemoteViewForSimpleNotification() {
        RemoteViews actual = contentUpdater.prepareRemoteView(context);

        Assert.assertEquals("layout resource id is wrong", R.layout.notification_simple,
                actual.getLayoutId());
    }

    @Test
    public void updateNotificationSetsLayoutAsCustomLayoutIntoNotification() {
        contentUpdater.updateNotification(weatherPresentation, notificationSpy, layoutSpy, context);

        verify(notificationSpy).setContent(refEq(layoutSpy));
        verify(notificationSpy).setCustomBigContentView(refEq(layoutSpy));
    }

    @Test
    public void pressureShowsNoDataAndTheOtherFieldsAreEmptyIfThereIsNoWeatherData() {
        when(formatterMock.isEnoughValidData(same(weatherMock))).thenReturn(false);
        when(formatterMock.isEnoughValidMainData(same(weatherMock))).thenReturn(false);
        String noDataString = context.getString(R.string.no_data);

        weatherPresentation = weatherPresentation.copy(weatherMock);
        contentUpdater.updateNotification(weatherPresentation, notificationSpy, layoutSpy, context);

        verify(layoutSpy).setTextViewText(eq(R.id.temperature), eq(""));
        verify(layoutSpy).setTextViewText(eq(R.id.description), eq(""));
        verify(layoutSpy).setViewVisibility(eq(R.id.icon), eq(View.GONE));
        verify(layoutSpy).setTextViewText(eq(R.id.wind), eq(""));
        verify(layoutSpy).setTextViewText(eq(R.id.pressure), eq(noDataString));
        verify(layoutSpy).setTextViewText(eq(R.id.humidity), eq(""));
    }

    @Test
    public void pressureShowsNoDataButTemperatureIsShownIfThereIsOnlyTemperatureData() {
        when(formatterMock.isEnoughValidData(same(weatherMock))).thenReturn(false);
        when(formatterMock.isEnoughValidMainData(same(weatherMock))).thenReturn(true);
        String expectedDescription = "description";
        when(formatterMock.getDescription(same(weatherMock))).thenReturn(expectedDescription);
        String expectedTemperature = "22°C";
        when(formatterMock.getTemperature(same(weatherMock), anyString(), anyBoolean()))
                .thenReturn(expectedTemperature);
        String noDataString = context.getString(R.string.no_data);

        weatherPresentation = weatherPresentation.copy(weatherMock);
        contentUpdater.updateNotification(weatherPresentation, notificationSpy, layoutSpy, context);

        verify(layoutSpy).setTextViewText(eq(R.id.temperature), eq(expectedTemperature));
        verify(layoutSpy).setTextViewText(eq(R.id.description), eq(expectedDescription));
        verify(layoutSpy).setViewVisibility(eq(R.id.icon), eq(View.GONE));
        verify(layoutSpy).setTextViewText(eq(R.id.wind), eq(""));
        verify(layoutSpy).setTextViewText(eq(R.id.pressure), eq(noDataString));
        verify(layoutSpy).setTextViewText(eq(R.id.humidity), eq(""));
    }

    @Test
    public void formattedTemperatureHasBeenSetWhenItIsKnown() {
        when(formatterMock.isEnoughValidData(same(weatherMock))).thenReturn(true);
        when(formatterMock.isEnoughValidMainData(same(weatherMock))).thenReturn(true);
        String expectedTemperature = "22°C";
        when(formatterMock.getTemperature(same(weatherMock), anyString(), anyBoolean()))
                .thenReturn(expectedTemperature);

        weatherPresentation = weatherPresentation.copy(weatherMock);
        contentUpdater.updateNotification(weatherPresentation, notificationSpy, layoutSpy, context);

        verify(layoutSpy).setTextViewText(eq(R.id.temperature), eq(expectedTemperature));

        String expectedTemperatureUnits = "K";
        weatherPresentation = weatherPresentation.copy(true).copyTemperatureUnits(expectedTemperatureUnits);
        contentUpdater.updateNotification(weatherPresentation, notificationSpy, layoutSpy, context);

        verify(formatterMock).getTemperature(eq(weatherMock), eq(expectedTemperatureUnits),
                eq(true));
    }

    @Test
    public void formattedDescriptionHasBeenSetWhenItIsKnown() {
        when(formatterMock.isEnoughValidData(same(weatherMock))).thenReturn(true);
        when(formatterMock.isEnoughValidMainData(same(weatherMock))).thenReturn(true);
        String expectedDescription = "description";
        when(formatterMock.getDescription(same(weatherMock))).thenReturn(expectedDescription);

        weatherPresentation = weatherPresentation.copy(weatherMock);
        contentUpdater.updateNotification(weatherPresentation, notificationSpy, layoutSpy, context);

        verify(layoutSpy).setTextViewText(eq(R.id.description), eq(expectedDescription));
        verify(formatterMock).getDescription(eq(weatherMock));
    }

    @Test
    public void weatherIconHasBeenSetWhenItIsKnown() {
        when(formatterMock.isEnoughValidData(same(weatherMock))).thenReturn(true);
        when(formatterMock.isEnoughValidMainData(same(weatherMock))).thenReturn(true);
        Bitmap iconMock = mock(Bitmap.class);
        when(formatterMock.getWeatherIconAsBitmap(same(weatherMock), any(Context.class)))
                .thenReturn(iconMock);

        weatherPresentation = weatherPresentation.copy(weatherMock);
        contentUpdater.updateNotification(weatherPresentation, notificationSpy, layoutSpy, context);

        verify(layoutSpy).setViewVisibility(eq(R.id.icon), eq(View.VISIBLE));
        verify(layoutSpy).setImageViewBitmap(eq(R.id.icon), same(iconMock));
        verify(formatterMock).getWeatherIconAsBitmap(eq(weatherMock), same(context));
    }

    @Test
    public void formattedWindHasBeenSetWhenItIsKnown() {
        when(formatterMock.isEnoughValidData(same(weatherMock))).thenReturn(true);
        when(formatterMock.isEnoughValidMainData(same(weatherMock))).thenReturn(true);
        String expectedWind = "Wind: 1.5 m/s NNW";
        when(formatterMock.getWind(same(weatherMock), anyString(), anyString(), any(Context.class)))
                .thenReturn(expectedWind);
        String expectedWindSpeedUnits = "m/s";
        String expectedWindDirectionFormat = "arrow";

        weatherPresentation = weatherPresentation.copy(weatherMock);;
        contentUpdater.updateNotification(weatherPresentation, notificationSpy, layoutSpy, context);

        verify(layoutSpy).setTextViewText(eq(R.id.wind), eq(expectedWind));
        verify(formatterMock)
                .getWind(eq(weatherMock), eq(expectedWindSpeedUnits),
                        eq(expectedWindDirectionFormat), same(context));

        expectedWindSpeedUnits = "kph";
        expectedWindDirectionFormat = "none";
        weatherPresentation = weatherPresentation.copyWindSpeedUnits(expectedWindSpeedUnits)
                .copyWindDirectionFormat(expectedWindDirectionFormat);
        contentUpdater.updateNotification(weatherPresentation, notificationSpy, layoutSpy, context);

        verify(formatterMock)
                .getWind(eq(weatherMock), eq(expectedWindSpeedUnits),
                        eq(expectedWindDirectionFormat), same(context));
    }

    @Test
    public void formattedPressureHasBeenSetWhenItIsKnown() {
        when(formatterMock.isEnoughValidData(same(weatherMock))).thenReturn(true);
        when(formatterMock.isEnoughValidMainData(same(weatherMock))).thenReturn(true);
        String expectedPressure = "Pressure: 1016.0 hPa/mBar";
        when(formatterMock.getPressure(same(weatherMock), anyString(), any(Context.class)))
                .thenReturn(expectedPressure);
        String expectedPressureUnits = "hPa/mBar";

        weatherPresentation = weatherPresentation.copy(weatherMock);
        contentUpdater.updateNotification(weatherPresentation, notificationSpy, layoutSpy, context);

        verify(layoutSpy).setTextViewText(eq(R.id.pressure), eq(expectedPressure));
        verify(formatterMock).getPressure(eq(weatherMock), eq(expectedPressureUnits), same(context));

        expectedPressureUnits = "mm Hg";
        weatherPresentation = weatherPresentation.copyPressureUnits(expectedPressureUnits);
        contentUpdater.updateNotification(weatherPresentation, notificationSpy, layoutSpy, context);

        verify(formatterMock).getPressure(eq(weatherMock), eq(expectedPressureUnits), same(context));
    }

    @Test
    public void formattedHumidityHasBeenSetWhenItIsKnown() {
        when(formatterMock.isEnoughValidData(same(weatherMock))).thenReturn(true);
        when(formatterMock.isEnoughValidMainData(same(weatherMock))).thenReturn(true);
        String expectedHumidity = "Humidity: 1%";
        when(formatterMock.getHumidity(same(weatherMock), any(Context.class)))
                .thenReturn(expectedHumidity);

        weatherPresentation = weatherPresentation.copy(weatherMock);
        contentUpdater.updateNotification(weatherPresentation, notificationSpy, layoutSpy, context);

        verify(layoutSpy).setTextViewText(eq(R.id.humidity), eq(expectedHumidity));
        verify(formatterMock).getHumidity(eq(weatherMock), same(context));
    }

    @SuppressWarnings({"ConstantConditions"})
    @Test
    public void implementedMethodsChecksForNull() {
        assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                contentUpdater.updateNotification(null, notificationSpy, layoutSpy, context);
            }
        });

        assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                contentUpdater.updateNotification(weatherPresentation, null, layoutSpy, context);
            }
        });

        assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                contentUpdater.updateNotification(weatherPresentation, notificationSpy, null, context);
            }
        });

        assertThrows(NullPointerException.class, new ThrowingRunnable() {
            @Override
            public void run() {
                contentUpdater.updateNotification(weatherPresentation, notificationSpy, layoutSpy, null);
            }
        });
    }
}
