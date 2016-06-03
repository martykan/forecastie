package cz.martykan.forecastie;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import cz.martykan.forecastie.widgets.AbstractWidgetProvider;

public class ForecastieApp extends Application {

    private static final String TAG = "ForecastieApp";

    private static final long DURATION_MINUTE = TimeUnit.MINUTES.toMillis(1);

    private final Handler mHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();
        updateWidgetTime();
    }

    /**
     * Updates widgets on every full minute, to ensure that correct time is shown.
     */
    private void updateWidgetTime() {
        long timeToNextMinute = DURATION_MINUTE - new Date().getTime() % DURATION_MINUTE;
        Log.i(TAG, "Scheduling widget update in " + timeToNextMinute / 1000 + " seconds");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AbstractWidgetProvider.updateWidgets(ForecastieApp.this);
                updateWidgetTime();
            }
        }, timeToNextMinute);
    }

}
