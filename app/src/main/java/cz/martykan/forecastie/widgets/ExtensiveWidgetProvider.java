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
import cz.martykan.forecastie.R;
import cz.martykan.forecastie.activities.MainActivity;
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

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Weather widgetWeather = this.getTodayWeather(context);

            if (widgetWeather == null) {
                this.openMainActivity(context, remoteViews);
                return;
            }

            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
            String lastUpdated = context.getString(R.string.last_update_widget, MainActivity.formatTimeWithDayIfNotToday(context, widgetWeather.getLastUpdated()));

            remoteViews.setTextViewText(R.id.widgetCity, widgetWeather.getCity() + ", " + widgetWeather.getCountry());
            remoteViews.setTextViewText(R.id.widgetTemperature, this.getFormattedTemperature(widgetWeather, context, sp));
            remoteViews.setTextViewText(R.id.widgetDescription, widgetWeather.getDescription());
            remoteViews.setTextViewText(R.id.widgetWind, context.getString(R.string.wind) + ": " + this.getFormattedWind(widgetWeather, context, sp));
            remoteViews.setTextViewText(R.id.widgetPressure, context.getString(R.string.pressure) + ": " + this.getFormattedPressure(widgetWeather, context, sp));
            remoteViews.setTextViewText(R.id.widgetHumidity, context.getString(R.string.humidity) + ": " + widgetWeather.getHumidity() + " %");
            remoteViews.setTextViewText(R.id.widgetSunrise, context.getString(R.string.sunrise) + ": " + timeFormat.format(widgetWeather.getSunrise())); //
            remoteViews.setTextViewText(R.id.widgetSunset, context.getString(R.string.sunset) + ": " + timeFormat.format(widgetWeather.getSunset()));
            remoteViews.setTextViewText(R.id.widgetLastUpdate, lastUpdated);
            remoteViews.setImageViewBitmap(R.id.widgetIcon, getWeatherIcon(widgetWeather, context));

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
        scheduleNextUpdate(context);
    }
}
