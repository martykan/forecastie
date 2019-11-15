package cz.martykan.forecastie.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.martykan.forecastie.AlarmReceiver;
import cz.martykan.forecastie.R;
import cz.martykan.forecastie.activities.MainActivity;
import cz.martykan.forecastie.models.Weather;

public class TimeWidgetProvider extends AbstractWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.time_widget);

            setTheme(context, remoteViews);

            Intent intent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widgetButtonRefresh, pendingIntent);

            Intent intent2 = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0, intent2, 0);
            remoteViews.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent2);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Weather widgetWeather = new Weather();
            if(!"".equals(sp.getString("lastToday", ""))) {
                widgetWeather = parseWidgetJson(sp.getString("lastToday", ""), context);
            }
            else {
                try {
                    pendingIntent2.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                return;
            }

            DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
            String defaultDateFormat = context.getResources().getStringArray(R.array.dateFormatsValues)[0];
            String simpleDateFormat = sp.getString("dateFormat", defaultDateFormat);
            if ("custom".equals(simpleDateFormat)) {
                simpleDateFormat = sp.getString("dateFormatCustom", defaultDateFormat);
            }
            String dateString;
            try {
                simpleDateFormat = simpleDateFormat.substring(0, simpleDateFormat.indexOf("-") - 1);
                try {
                    SimpleDateFormat resultFormat = new SimpleDateFormat(simpleDateFormat);
                    dateString = resultFormat.format(new Date());
                } catch (IllegalArgumentException e) {
                    dateString = context.getResources().getString(R.string.error_dateFormat);
                }
            } catch (StringIndexOutOfBoundsException e) {
                DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
                dateString = dateFormat.format(new Date());
            }

            remoteViews.setTextViewText(R.id.time, timeFormat.format(new Date()));
            remoteViews.setTextViewText(R.id.date, dateString);
            remoteViews.setTextViewText(R.id.widgetCity, widgetWeather.getCity() + ", " + widgetWeather.getCountry());
            remoteViews.setTextViewText(R.id.widgetTemperature, widgetWeather.getTemperature());
            remoteViews.setTextViewText(R.id.widgetDescription, widgetWeather.getDescription());
            remoteViews.setImageViewBitmap(R.id.widgetIcon, getWeatherIcon(widgetWeather.getIcon(), context));

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            {
                if (dateString.length() > 19)
                    remoteViews.setViewPadding(R.id.widgetIcon, 40, 0, 0, 0);
            }

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
        scheduleNextUpdate(context);
    }
}
