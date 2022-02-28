package cz.martykan.forecastie.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.martykan.forecastie.AlarmReceiver;
import cz.martykan.forecastie.R;
import cz.martykan.forecastie.models.Weather;

public class ClassicTimeWidgetProvider extends AbstractWidgetProvider {

    private static final String TAG = "TimeWidgetProvider";

    private static final String ACTION_UPDATE_TIME = "cz.martykan.forecastie.UPDATE_TIME";

    //private static final long DURATION_MINUTE = TimeUnit.SECONDS.toMillis(30);

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.time_widget_classic);

            setTheme(context, remoteViews);

            Intent intent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widgetButtonRefresh, pendingIntent);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Weather widgetWeather = this.getTodayWeather(context);

            if (widgetWeather == null) {
                this.openMainActivity(context, remoteViews);
                return;
            }

            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
            String defaultDateFormat = context.getResources().getStringArray(R.array.dateFormatsValues)[0];
            String dateFormat = sp.getString("dateFormat", defaultDateFormat);
            dateFormat = dateFormat.substring(0, dateFormat.indexOf("-")-1);
            if ("custom".equals(dateFormat)) {
                dateFormat = sp.getString("dateFormatCustom", defaultDateFormat);
            }
            String dateString;
            try {
                SimpleDateFormat resultFormat = new SimpleDateFormat(dateFormat);
                dateString = resultFormat.format(new Date());
            } catch (IllegalArgumentException e) {
                dateString = context.getResources().getString(R.string.error_dateFormat);
            }

            remoteViews.setTextViewText(R.id.time, timeFormat.format(new Date()));
            remoteViews.setTextViewText(R.id.date, dateString);
            remoteViews.setTextViewText(R.id.widgetCity, widgetWeather.getCity() + ", " + widgetWeather.getCountry());
            remoteViews.setTextViewText(R.id.widgetTemperature, this.getFormattedTemperature(widgetWeather, context, sp));
            remoteViews.setTextViewText(R.id.widgetDescription, widgetWeather.getDescription());
            remoteViews.setImageViewBitmap(R.id.widgetIcon, getWeatherIcon(widgetWeather, context));

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
        scheduleNextUpdate(context);
    }

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

        Log.d(TAG, "Disable time widget updates");
        cancelUpdate(context);
    }

    /*
    private static void scheduleNextUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long now = new Date().getTime();
        long nextUpdate = now + DURATION_MINUTE - now % DURATION_MINUTE;
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "Next widget update: " +
                    android.text.format.DateFormat.getTimeFormat(context).format(new Date(nextUpdate)));
        }
        if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC, nextUpdate, getTimeIntent(context));
        } else {
            alarmManager.set(AlarmManager.RTC, nextUpdate, getTimeIntent(context));
        }
    }

    private static void cancelUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getTimeIntent(context));
    }

    private static PendingIntent getTimeIntent(Context context) {
        Intent intent = new Intent(context, TimeWidgetProvider.class);
        intent.setAction(ACTION_UPDATE_TIME);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }
    */

}
