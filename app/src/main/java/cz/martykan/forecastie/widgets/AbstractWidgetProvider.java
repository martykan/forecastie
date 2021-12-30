package cz.martykan.forecastie.widgets;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import cz.martykan.forecastie.BuildConfig;
import cz.martykan.forecastie.R;
import cz.martykan.forecastie.activities.MainActivity;
import cz.martykan.forecastie.models.Weather;
import cz.martykan.forecastie.utils.Formatting;
import cz.martykan.forecastie.utils.TimeUtils;
import cz.martykan.forecastie.utils.UnitConvertor;
import cz.martykan.forecastie.utils.formatters.WeatherFormatter;
import cz.martykan.forecastie.weatherapi.WeatherStorage;

public abstract class AbstractWidgetProvider extends AppWidgetProvider {
    protected static final long DURATION_MINUTE = TimeUnit.SECONDS.toMillis(30);
    protected static final String ACTION_UPDATE_TIME = "cz.martykan.forecastie.UPDATE_TIME";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_UPDATE_TIME.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName provider = new ComponentName(context.getPackageName(), getClass().getName());
            int ids[] = appWidgetManager.getAppWidgetIds(provider);
            onUpdate(context, appWidgetManager, ids);
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);

        Log.d(this.getClass().getSimpleName(), "Disable updates for this widget");
        cancelUpdate(context);
    }

    protected Bitmap getWeatherIcon(Weather weather, Context context) {
        Formatting formatting = new Formatting(context);
        String weatherIcon = formatting.getWeatherIcon(weather.getWeatherId(), TimeUtils.isDayTime(weather, Calendar.getInstance()));
        return WeatherFormatter.getWeatherIconAsBitmap(context, weatherIcon, Color.WHITE);
    }

    @Nullable
    protected Weather getTodayWeather(Context context) {
        WeatherStorage weatherStorage = new WeatherStorage(context);
        return weatherStorage.getLastToday();
    }

    protected void openMainActivity(Context context, RemoteViews remoteViews) {
        try {
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent);
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    protected String localize(SharedPreferences sp, Context context, String preferenceKey,
                              String defaultValueKey) {
        MainActivity.initMappings();
        return MainActivity.localize(sp, context, preferenceKey, defaultValueKey);
    }

    public static void updateWidgets(Context context) {
        updateWidgets(context, ExtensiveWidgetProvider.class);
        updateWidgets(context, TimeWidgetProvider.class);
        updateWidgets(context, SimpleWidgetProvider.class);
        updateWidgets(context, ClassicTimeWidgetProvider.class);
    }

    private static void updateWidgets(Context context, Class widgetClass) {
        Intent intent = new Intent(context.getApplicationContext(), widgetClass)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(context.getApplicationContext())
                .getAppWidgetIds(new ComponentName(context.getApplicationContext(), widgetClass));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.getApplicationContext().sendBroadcast(intent);
    }

    protected void setTheme(Context context, RemoteViews remoteViews) {
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("transparentWidget", false)){
            remoteViews.setInt(R.id.widgetRoot, "setBackgroundResource", R.drawable.widget_card_transparent);
            return;
        }
        String theme = PreferenceManager.getDefaultSharedPreferences(context).getString("theme", "fresh");
        switch (theme) {
            case "dark":
            case "classicdark":
                remoteViews.setInt(R.id.widgetRoot, "setBackgroundResource", R.drawable.widget_card_dark);
                break;
            case "black":
            case "classicblack":
                remoteViews.setInt(R.id.widgetRoot, "setBackgroundResource", R.drawable.widget_card_black);
                break;
            case "classic":
                remoteViews.setInt(R.id.widgetRoot, "setBackgroundResource", R.drawable.widget_card_classic);
                break;
            default:
                remoteViews.setInt(R.id.widgetRoot, "setBackgroundResource", R.drawable.widget_card);
                break;
        }
    }

    protected void scheduleNextUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long now = new Date().getTime();
        long nextUpdate = now + DURATION_MINUTE - now % DURATION_MINUTE;
        if (BuildConfig.DEBUG) {
            Log.v(this.getClass().getSimpleName(), "Next widget update: " +
                    android.text.format.DateFormat.getTimeFormat(context).format(new Date(nextUpdate)));
        }
        if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC, nextUpdate, getTimeIntent(context));
        } else {
            alarmManager.set(AlarmManager.RTC, nextUpdate, getTimeIntent(context));
        }
    }

    protected void cancelUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getTimeIntent(context));
    }

    protected PendingIntent getTimeIntent(Context context) {
        Intent intent = new Intent(context, this.getClass());
        intent.setAction(ACTION_UPDATE_TIME);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    protected String getFormattedTemperature(Weather weather, Context context, SharedPreferences sp) {
        float temperature = UnitConvertor.convertTemperature((float) weather.getTemperature(), sp);
        if (sp.getBoolean("temperatureInteger", false)) {
            temperature = Math.round(temperature);
        }

        return new DecimalFormat("#.#").format(temperature) + localize(sp, context, "unit", "C");
    }

    protected String getFormattedPressure(Weather weather, Context context, SharedPreferences sp) {
        double pressure = UnitConvertor.convertPressure((float) weather.getPressure(), sp);

        return new DecimalFormat("0.0").format(pressure) + " " + localize(sp, context, "pressureUnit", "hPa");
    }

    protected String getFormattedWind(Weather weather, Context context, SharedPreferences sp) {
        double wind = UnitConvertor.convertWind(weather.getWind(), sp);

        return new DecimalFormat("0.0").format(wind) + " " + localize(sp, context, "speedUnit", "m/s")
                    + (weather.isWindDirectionAvailable() ? " " + MainActivity.getWindDirectionString(sp, context, weather) : "");
    }
}
