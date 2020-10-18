package cz.martykan.forecastie.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ServiceTestRule;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowNotificationManager;
import org.robolectric.shadows.ShadowPendingIntent;

import java.util.concurrent.TimeoutException;

import cz.martykan.forecastie.activities.MainActivity;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 27)
public class WeatherNotificationServicesTests {
    @Rule
    public ServiceTestRule serviceTestRule = new ServiceTestRule();

    WeatherNotificationService service;

    private Intent weatherNotificationStartIntent;

    @Before
    public void setUp() {
        Context context = getApplicationContext();
        weatherNotificationStartIntent = new Intent(context, WeatherNotificationService.class);
    }

    @After
    public void tearDown() {
        service = null;
        weatherNotificationStartIntent = null;
    }

    @Test
    public void serviceIsSticky() {
        service = new WeatherNotificationService();

        int actual = service.onStartCommand(weatherNotificationStartIntent, 0, 0);

        Assert.assertEquals("service mode is wrong", Service.START_STICKY, actual);
    }

    @Test
    public void stopForegroundHasBeenInvokedOnServiceDestroyWithRemoveNotificationArgument() {
        service = spy(WeatherNotificationService.class);

        service.onDestroy();

        verify(service).stopForeground(true);
    }

    // TODO add test that startForeground has been invoked during service creation.
    // tried to configure PowerMock with Robolectric but couldn't. I know how to do it with
    // mockk library but it is kotlin library and it is difficult to use it from java.

    @Ignore("onCreate of the service doesn't invoked")
    @Test
    public void clickOnTheWeatherNotificationRoutesToMainActivity() throws TimeoutException {
        Context context = getApplicationContext();
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // have to use Shadow implicitly instead of shadowOf because Robolectric 4.x requires
        // compileSdkVersion 28 or higher
        ShadowNotificationManager shadowNotificationManager =
                (ShadowNotificationManager) Shadow.extract(notificationManager);

        serviceTestRule.startService(weatherNotificationStartIntent);

        Assert.assertFalse("there is no notifications",
                shadowNotificationManager.getAllNotifications().isEmpty());
        Notification notification = shadowNotificationManager
                .getNotification(WeatherNotificationService.WEATHER_NOTIFICATION_ID);
        Assert.assertNotNull("there is no our weather notification", notification);
        // have to use Shadow implicitly instead of shadowOf because Robolectric 4.x requires
        // compileSdkVersion 28 or higher
        ShadowPendingIntent pendingIntent =
                (ShadowPendingIntent) Shadow.extract(notification.contentIntent);
        Assert.assertTrue("pending intent has wrong type",
                pendingIntent.isActivityIntent());
        Intent intent = pendingIntent.getSavedIntent();
        //noinspection ConstantConditions
        Assert.assertEquals("notification will try to open wrong activity",
                MainActivity.class.getName(), intent.getComponent().getClassName());
    }
}