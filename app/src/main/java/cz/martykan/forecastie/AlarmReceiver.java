package cz.martykan.forecastie;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            String interval = sp.getString("refreshInterval", "1");
            if (!interval.equals("0")) {
                setRecurringAlarm(context);
                getWeather();
            }
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            // Get weather if last attempt failed
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            String interval = sp.getString("refreshInterval", "1");
            if (!interval.equals("0") && sp.getBoolean("backgroundRefreshFailed", false)) {
                getWeather();
            }
        } else {
            getWeather();
        }
    }

    private void getWeather() {
        Log.d("Alarm", "Recurring alarm; requesting download service.");
        boolean failed;
        if (isNetworkAvailable()) {
            failed = false;
            new GetWeatherTask().execute();
            new GetLongTermWeatherTask().execute();
        } else {
            failed = true;
        }
        SharedPreferences.Editor editor =
                PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("backgroundRefreshFailed", failed);
        editor.apply();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public class GetWeatherTask extends AsyncTask<String, String, Void> {

        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            String result = "";
            try {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                String language = Locale.getDefault().getLanguage();
                if(language.equals("cs")) { language = "cz"; }
                String apiKey = sp.getString("apiKey", context.getResources().getString(R.string.apiKey));
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + URLEncoder.encode(sp.getString("city", Constants.DEFAULT_CITY), "UTF-8") + "&lang="+ language +"&appid=" + apiKey);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                if(urlConnection.getResponseCode() == 200) {
                    String line = null;
                    while ((line = r.readLine()) != null) {
                        result += line + "\n";
                    }
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    editor.putString("lastToday", result);
                    editor.apply();
                }
                else {
                    // Connection problem
                }
            } catch (IOException e) {
                // No connection
            }
            return null;
        }

        protected void onPostExecute(Void v) {

        }
    }

    class GetLongTermWeatherTask extends AsyncTask<String, String, Void> {

        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            String result = "";
            try {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                String language = Locale.getDefault().getLanguage();
                if(language.equals("cs")) { language = "cz"; }
                String apiKey = sp.getString("apiKey", context.getResources().getString(R.string.apiKey));
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?q=" + URLEncoder.encode(sp.getString("city", Constants.DEFAULT_CITY), "UTF-8") + "&lang="+ language +"&mode=json&appid=" + apiKey);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                if(urlConnection.getResponseCode() == 200) {
                    String line = null;
                    while ((line = r.readLine()) != null) {
                        result += line + "\n";
                    }
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    editor.putString("lastLongterm", result);
                    editor.apply();
                }
                else {
                    // Connection problem
                }
            } catch (IOException e) {
                // No connection
            }
            return null;
        }

        protected void onPostExecute(Void v) {

        }
    }

    public static void setRecurringAlarm(Context context) {
        String interval = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("refreshInterval", "1");
        Intent refresh = new Intent(context, AlarmReceiver.class);
        PendingIntent recurringRefresh = PendingIntent.getBroadcast(context,
                0, refresh, PendingIntent.FLAG_CANCEL_CURRENT);
        if(!interval.equals("0")) {
            AlarmManager alarms = (AlarmManager) context.getSystemService(
                    Context.ALARM_SERVICE);
            if(interval.equals("15")) {
                alarms.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                        AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                        recurringRefresh);
            }
            else if(interval.equals("30")) {
                alarms.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_HOUR,
                        AlarmManager.INTERVAL_HALF_HOUR,
                        recurringRefresh);
            }
            else if(interval.equals("1")) {
                alarms.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
                        AlarmManager.INTERVAL_HOUR,
                        recurringRefresh);
            }
            else if(interval.equals("12")) {
                alarms.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_DAY,
                        AlarmManager.INTERVAL_HALF_DAY,
                        recurringRefresh);
            }
            else if(interval.equals("24")) {
                alarms.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_DAY,
                        AlarmManager.INTERVAL_DAY,
                        recurringRefresh);
            }
        }
        else {
            // Cancel previous alarm
            AlarmManager alarms = (AlarmManager) context.getSystemService(
                    Context.ALARM_SERVICE);
            alarms.cancel(recurringRefresh);
        }
    }
}