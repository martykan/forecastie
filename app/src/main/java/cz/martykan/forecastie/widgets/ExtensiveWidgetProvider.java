package cz.martykan.forecastie.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import java.text.DateFormat;

import cz.martykan.forecastie.AlarmReceiver;
import cz.martykan.forecastie.activities.MainActivity;
import cz.martykan.forecastie.R;
import cz.martykan.forecastie.models.Weather;

public class ExtensiveWidgetProvider extends AbstractWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.extensive_widget);

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
            if(!sp.getString("lastToday", "").equals("")) {
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

            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);

            remoteViews.setTextViewText(R.id.widgetCity, widgetWeather.getCity() + ", " + widgetWeather.getCountry());
            remoteViews.setTextViewText(R.id.widgetTemperature, widgetWeather.getTemperature());
            remoteViews.setTextViewText(R.id.widgetDescription, widgetWeather.getDescription());
            remoteViews.setTextViewText(R.id.widgetWind, widgetWeather.getWind());
            remoteViews.setTextViewText(R.id.widgetPressure, widgetWeather.getPressure());
            remoteViews.setTextViewText(R.id.widgetHumidity, context.getString(R.string.humidity) + ": " + widgetWeather.getHumidity() + " %");
            remoteViews.setTextViewText(R.id.widgetSunrise, context.getString(R.string.sunrise) + ": " + timeFormat.format(widgetWeather.getSunrise())); //
            remoteViews.setTextViewText(R.id.widgetSunset, context.getString(R.string.sunset) + ": " + timeFormat.format(widgetWeather.getSunset()));
            remoteViews.setTextViewText(R.id.widgetLastUpdate, widgetWeather.getLastUpdated());
            remoteViews.setImageViewBitmap(R.id.widgetIcon, getWeatherIcon(widgetWeather.getIcon(), context));

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
        scheduleNextUpdate(context);
    }

}
