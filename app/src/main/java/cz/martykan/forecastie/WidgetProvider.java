package cz.martykan.forecastie;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

public class WidgetProvider extends AppWidgetProvider {

    Weather widgetWeather = new Weather();
    RemoteViews remoteViews;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];

            remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.weather_widget);

            Intent intent = new Intent(context, WidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widgetButtonRefresh, pendingIntent);


            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

            if (sp.getString("lastToday", "{}") != "{}") {
                parseWidgetJson(sp.getString("lastToday", "{}"), context);
            }

            if (isNetworkAvailable(context)) {
                new GetWeatherTask(context).execute();
            }

            Intent intent2 = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0, intent2, 0);
            remoteViews.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent2);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public Bitmap buildUpdate(String text, Context context) {
        Bitmap myBitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_4444);
        Canvas myCanvas = new Canvas(myBitmap);
        Paint paint = new Paint();
        Typeface clock = Typeface.createFromAsset(context.getAssets(), "fonts/weather.ttf");
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(clock);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(150);
        paint.setTextAlign(Paint.Align.CENTER);
        myCanvas.drawText(text, 128, 180, paint);
        return myBitmap;
    }

    private String setWeatherIcon(int actualId, int hourOfDay, Context context) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            if (hourOfDay >= 7 && hourOfDay < 20) {
                icon = context.getString(R.string.weather_sunny);
            } else {
                icon = context.getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2:
                    icon = context.getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = context.getString(R.string.weather_drizzle);
                    break;
                case 7:
                    icon = context.getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = context.getString(R.string.weather_cloudy);
                    break;
                case 6:
                    icon = context.getString(R.string.weather_snowy);
                    break;
                case 5:
                    icon = context.getString(R.string.weather_rainy);
                    break;
            }
        }
        return icon;
    }

    private void parseWidgetJson(String result, Context context) {
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putString("lastToday", result);
            editor.commit();

            JSONObject reader = new JSONObject(result);

            widgetWeather.setCity(reader.getString("name").toString());
            widgetWeather.setCountry(reader.optJSONObject("sys").getString("country").toString());
            remoteViews.setTextViewText(R.id.widgetCity, widgetWeather.getCity() + ", " + widgetWeather.getCountry());

            String temperature = reader.optJSONObject("main").getString("temp").toString();
            widgetWeather.setTemperature(temperature);

            if (sp.getString("unit", "C").equals("C")) {
                temperature = Float.parseFloat(temperature) - 273.15 + "";
            }

            if (sp.getString("unit", "C").equals("F")) {
                temperature = (((9 * (Float.parseFloat(temperature) - 273.15)) / 5) + 32) + "";
            }

            widgetWeather.setDescription(reader.optJSONArray("weather").getJSONObject(0).getString("description").toString());
            widgetWeather.setWind(reader.optJSONObject("wind").getString("speed").toString());
            widgetWeather.setPressure(reader.optJSONObject("main").getString("pressure").toString());
            widgetWeather.setHumidity(reader.optJSONObject("main").getString("humidity").toString());
            widgetWeather.setIcon(setWeatherIcon(Integer.parseInt(reader.optJSONArray("weather").getJSONObject(0).getString("id").toString()), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), context));


            double wind = Double.parseDouble(widgetWeather.getWind());
            if (sp.getString("speedUnit", "m/s").equals("kph")) {
                wind = wind * 3.59999999712;
            }

            if (sp.getString("speedUnit", "m/s").equals("mph")) {
                wind = wind * 2.23693629205;
            }

            double pressure = Double.parseDouble(widgetWeather.getPressure());
            if (sp.getString("pressureUnit", "hPa").equals("kPa")) {
                pressure = pressure / 10;
            }
            if (sp.getString("pressureUnit", "hPa").equals("mm Hg")) {
                pressure = pressure * 0.750061561303;
            }

            remoteViews.setTextViewText(R.id.widgetTemperature, temperature.substring(0, temperature.indexOf(".") + 2) + " °" + sp.getString("unit", "C"));
            remoteViews.setTextViewText(R.id.widgetDescription, widgetWeather.getDescription().substring(0, 1).toUpperCase() + widgetWeather.getDescription().substring(1));
            remoteViews.setTextViewText(R.id.widgetWind, context.getString(R.string.wind) + ": " + (wind + "").substring(0, (wind + "").indexOf(".") + 2) + " " + sp.getString("speedUnit", "m/s")
                    + " " + MainActivity.getWindDirectionString(sp, context, widgetWeather));
            remoteViews.setTextViewText(R.id.widgetPressure, context.getString(R.string.pressure) + ": " + (pressure + "").substring(0, (pressure + "").indexOf(".") + 2) + " " + sp.getString("pressureUnit", "hPa"));
            remoteViews.setTextViewText(R.id.widgetHumidity, context.getString(R.string.humidity) + ": " + widgetWeather.getHumidity() + " %");
            remoteViews.setImageViewBitmap(R.id.widgetIcon, buildUpdate(widgetWeather.getIcon(), context));
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
        }
    }

    public class GetWeatherTask extends AsyncTask<String, String, Void> {
        String result = "";
        Context context;

        public GetWeatherTask(Context context) {
            this.context = context;
        }

        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                String language = Locale.getDefault().getLanguage();
                if (language.equals("cs")) {
                    language = "cz";
                }
                String apiKey = sp.getString("apiKey", context.getResources().getString(R.string.apiKey));
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + URLEncoder.encode(sp.getString("city", Constants.DEFAULT_CITY), "UTF-8") + "&lang=" + language + "&appid=" + apiKey);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                String line = null;
                while ((line = r.readLine()) != null) {
                    result += line + "\n";
                }
            } catch (IOException e) {
                Log.e("IOException Data", result);
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            //parse JSON data
            parseWidgetJson(result, context);
        }
    }
}
