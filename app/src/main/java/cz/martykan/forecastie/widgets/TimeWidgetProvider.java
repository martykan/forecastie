package cz.martykan.forecastie.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.util.Date;

import cz.martykan.forecastie.AlarmReceiver;
import cz.martykan.forecastie.MainActivity;
import cz.martykan.forecastie.R;
import cz.martykan.forecastie.Weather;

public class TimeWidgetProvider extends AbstractWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.time_widget);

            Intent intent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widgetButtonRefresh, pendingIntent);

            Intent intent2 = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0, intent2, 0);
            remoteViews.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent2);

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Weather widgetWeather = parseWidgetJson(sp.getString("lastToday", ""), context);

            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);

            remoteViews.setTextViewText(R.id.time, timeFormat.format(new Date()));
            remoteViews.setTextViewText(R.id.widgetCity, widgetWeather.getCity() + ", " + widgetWeather.getCountry());
            remoteViews.setTextViewText(R.id.widgetTemperature, widgetWeather.getTemperature());
            remoteViews.setTextViewText(R.id.widgetDescription, widgetWeather.getDescription());
            remoteViews.setImageViewBitmap(R.id.widgetIcon, getWeatherIcon(widgetWeather.getIcon(), context));

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

}
