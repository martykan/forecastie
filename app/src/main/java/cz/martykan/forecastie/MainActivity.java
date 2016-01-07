package cz.martykan.forecastie;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Typeface weatherFont;
    Weather todayWeather = new Weather();

    TextView todayTemperature;
    TextView todayDescription;
    TextView todayWind;
    TextView todayPressure;
    TextView todayHumidity;
    TextView todayIcon;
    RecyclerView recyclerView;

    View appView;

    private List<Weather> longTermWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initiate activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        appView = findViewById(R.id.viewApp);

        // Load toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize textboxes
        todayTemperature = (TextView) findViewById(R.id.todayTemperature);
        todayDescription = (TextView) findViewById(R.id.todayDescription);
        todayWind = (TextView) findViewById(R.id.todayWind);
        todayPressure = (TextView) findViewById(R.id.todayPressure);
        todayHumidity = (TextView) findViewById(R.id.todayHumidity);
        todayIcon = (TextView) findViewById(R.id.todayIcon);
        weatherFont = Typeface.createFromAsset(this.getAssets(), "fonts/weather.ttf");
        todayIcon.setTypeface(weatherFont);

        // Initialize recyclerview
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Preload data from cache
        preloadWeather();

        // Update weather online
        if (isNetworkAvailable()) {
            getTodayWeather();
            getLongTermWeather();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (isNetworkAvailable()) {
            getTodayWeather();
            getLongTermWeather();
        }
    }

    private void preloadWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        if(sp.getString("lastToday", "{}") != "{}") {
            parseTodayJson(sp.getString("lastToday", "{}"));
        }
        if(sp.getString("lastLongterm", "{}") != "{}") {
            parseLongTermJson(sp.getString("lastLongterm", "{}"));
        }
    }

    private void getTodayWeather() {
        new GetWeatherTask().execute();
    }

    private void getLongTermWeather() {
        new GetLongTermWeatherTask().execute();
    }

    private void searchCities() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(this.getString(R.string.search_title));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setMaxLines(1);
        input.setSingleLine(true);
        alert.setView(input, 32, 0, 32, 0);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String result = input.getText().toString();
                if (result.matches("")) {

                } else {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                    editor.putString("city", result);
                    editor.commit();
                    getTodayWeather();
                    getLongTermWeather();
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cancelled
            }
        });
        alert.show();
    }

    private void aboutDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Forecastie");
        final WebView webView = new WebView(this);
        webView.loadData(
                "<p>A lightweight, opensource weather app.</p>" +
                        "<p>Developed by <a href='mailto:t.martykan@gmail.com'>Tomas Martykan</a></p>" +
                        "<p>Data provided by <a href='http://openweathermap.org/'>OpenWeatherMap</a>, under the <a href='http://creativecommons.org/licenses/by-sa/2.0/'>Creative Commons license</a>" +
                        "<p>Icons are <a href='https://erikflowers.github.io/weather-icons/'>Weather Icons</a>, by <a href='http://www.twitter.com/artill'>Lukas Bischoff</a> and <a href='http://www.twitter.com/Erik_UX'>Erik Flowers</a>, under the <a href='http://scripts.sil.org/OFL'>SIL OFL 1.1</a> licence.", "text/html", "UTF-8");
        alert.setView(webView, 32, 0, 32, 0);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        alert.show();
    }

    private String setWeatherIcon(int actualId, int hourOfDay) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            if(hourOfDay >= 7 && hourOfDay < 20) {
                icon = this.getString(R.string.weather_sunny);
            } else {
                icon = this.getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2:
                    icon = this.getString(R.string.weather_thunder);
                    break;
                case 3:
                    icon = this.getString(R.string.weather_drizzle);
                    break;
                case 7:
                    icon = this.getString(R.string.weather_foggy);
                    break;
                case 8:
                    icon = this.getString(R.string.weather_cloudy);
                    break;
                case 6:
                    icon = this.getString(R.string.weather_snowy);
                    break;
                case 5:
                    icon = this.getString(R.string.weather_rainy);
                    break;
            }
        }
        return icon;
    }

    private void parseTodayJson(String result) {
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putString("lastToday", result);
            editor.commit();

            JSONObject reader = new JSONObject(result);

            todayWeather.setCity(reader.getString("name").toString());
            todayWeather.setCountry(reader.optJSONObject("sys").getString("country").toString());
            getSupportActionBar().setTitle(todayWeather.getCity() + ", " + todayWeather.getCountry());

            String temperature = reader.optJSONObject("main").getString("temp").toString();
            todayWeather.setTemperature(temperature);

            if (sp.getString("unit", "C").equals("C")) {
                temperature = Float.parseFloat(temperature) - 273.15 + "";
            }

            if (sp.getString("unit", "C").equals("F")) {
                temperature = (((9 * (Float.parseFloat(temperature) - 273.15)) / 5) + 32) + "";
            }

            todayWeather.setDescription(reader.optJSONArray("weather").getJSONObject(0).getString("description").toString());
            todayWeather.setWind(reader.optJSONObject("wind").getString("speed").toString());
            todayWeather.setPressure(reader.optJSONObject("main").getString("pressure").toString());
            todayWeather.setHumidity(reader.optJSONObject("main").getString("humidity").toString());
            try {
                todayWeather.setRain(reader.optJSONObject("rain").getString("1h").toString());
            }
            catch (Exception e) {
                try {
                    todayWeather.setRain(reader.optJSONObject("rain").getString("3h").toString());
                }
                catch (Exception e2) {
                    try {
                        todayWeather.setRain(reader.optJSONObject("snow").getString("1h").toString());
                    }
                    catch (Exception e3) {
                        try {
                            todayWeather.setRain(reader.optJSONObject("snow").getString("3h").toString());
                        }
                        catch (Exception e4) {
                            todayWeather.setRain("0");
                        }
                    }
                }
            }
            todayWeather.setId(reader.optJSONArray("weather").getJSONObject(0).getString("id").toString());
            todayWeather.setIcon(setWeatherIcon(Integer.parseInt(reader.optJSONArray("weather").getJSONObject(0).getString("id").toString()), Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));


            double wind = Double.parseDouble(todayWeather.getWind());
            if(sp.getString("speedUnit", "m/s").equals("kph")){
                wind = wind * 3.59999999712;
            }

            if (sp.getString("speedUnit", "m/s").equals("mph")) {
                wind = wind * 2.23693629205;
            }

            double pressure = Double.parseDouble(todayWeather.getPressure());
            if(sp.getString("pressureUnit", "hPa").equals("kPa")){
                pressure = pressure/10;
            }
            if(sp.getString("pressureUnit", "hPa").equals("mm Hg")){
                pressure = pressure*0.750061561303;
            }

            todayTemperature.setText(temperature.substring(0, temperature.indexOf(".") + 2) + " °" + sp.getString("unit", "C"));
            if(Float.parseFloat(todayWeather.getRain()) > 0.1) {
                todayDescription.setText(todayWeather.getDescription().substring(0, 1).toUpperCase() + todayWeather.getDescription().substring(1) + " (" + todayWeather.getRain().substring(0, todayWeather.getRain().indexOf(".") + 2) + " mm)");
            }
            else {
                todayDescription.setText(todayWeather.getDescription().substring(0, 1).toUpperCase() + todayWeather.getDescription().substring(1));
            }
            todayWind.setText(getString(R.string.wind) + ": " + (wind+"").substring(0, (wind+"").indexOf(".") + 2) + " " + sp.getString("speedUnit", "m/s"));
            todayPressure.setText(getString(R.string.pressure) + ": " + (pressure+"").substring(0, (pressure + "").indexOf(".") + 2) + " " + sp.getString("pressureUnit", "hPa"));
            todayHumidity.setText(getString(R.string.humidity) + ": " + todayWeather.getHumidity() + " %");
            todayIcon.setText(todayWeather.getIcon());
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            Snackbar.make(appView, "Error parsing JSON.", Snackbar.LENGTH_LONG).show();
        }
    }

    public void parseLongTermJson(String result) {
        int i;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
        editor.putString("lastLongterm", result);
        editor.commit();
        try {
            JSONObject reader = new JSONObject(result);
            JSONArray list = reader.getJSONArray("list");
            longTermWeather = new ArrayList<>();

            for (i = 0; i < list.length(); i++) {
                Weather weather = new Weather();
                weather.setDate(list.getJSONObject(i).getString("dt"));
                weather.setTemperature(list.getJSONObject(i).optJSONObject("main").getString("temp"));
                weather.setDescription(list.getJSONObject(i).optJSONArray("weather").getJSONObject(0).getString("description").toString());
                weather.setWind(list.getJSONObject(i).optJSONObject("wind").getString("speed").toString());
                weather.setPressure(list.getJSONObject(i).optJSONObject("main").getString("pressure").toString());
                weather.setHumidity(list.getJSONObject(i).optJSONObject("main").getString("humidity").toString());
                try {
                    weather.setRain(list.getJSONObject(i).optJSONObject("rain").getString("1h").toString());
                }
                catch (Exception e) {
                    try {
                        weather.setRain(list.getJSONObject(i).optJSONObject("rain").getString("3h").toString());
                    }
                    catch (Exception e2) {
                        try {
                            weather.setRain(list.getJSONObject(i).optJSONObject("snow").getString("1h").toString());
                        }
                        catch (Exception e3) {
                            try {
                                weather.setRain(list.getJSONObject(i).optJSONObject("snow").getString("3h").toString());
                            }
                            catch (Exception e4) {
                                weather.setRain("0");
                            }
                        }
                    }
                }
                weather.setId(list.getJSONObject(i).optJSONArray("weather").getJSONObject(0).getString("id").toString());
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(list.getJSONObject(i).getString("dt")));
                weather.setIcon(setWeatherIcon(Integer.parseInt(list.getJSONObject(i).optJSONArray("weather").getJSONObject(0).getString("id").toString()), cal.get(Calendar.HOUR_OF_DAY)));
                longTermWeather.add(weather);
            }
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            Snackbar.make(appView, "Error parsing JSON.", Snackbar.LENGTH_LONG).show();
        }
        WeatherRecyclerAdapter adapter = new WeatherRecyclerAdapter(MainActivity.this, longTermWeather);
        recyclerView.setAdapter(adapter);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            getTodayWeather();
            getLongTermWeather();
            return true;
        }
        if (id == R.id.action_search) {
            searchCities();
            return true;
        }
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_about) {
            aboutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class GetWeatherTask extends AsyncTask<String, String, Void> {
        String result = "";
        private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        protected void onPreExecute() {
            progressDialog.setMessage("Downloading your data...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String language = Locale.getDefault().getLanguage();
                if(language.equals("cs")) { language = "cz"; }
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + URLEncoder.encode(sp.getString("city", "London"), "UTF-8") + "&lang="+ language +"&appid=2de143494c0b295cca9337e1e96b00e0");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                if(urlConnection.getResponseCode() == 200) {
                    String line = null;
                    while ((line = r.readLine()) != null) {
                        result += line + "\n";
                    }
                }
                else {
                    Snackbar.make(appView, "There is a problem with your interent connection.", Snackbar.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                Log.e("IOException Data", result);
                e.printStackTrace();
                Snackbar.make(appView, "Connection not available.", Snackbar.LENGTH_LONG).show();
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            //parse JSON data
            parseTodayJson(result);
            this.progressDialog.dismiss();
        }
    }

    class GetLongTermWeatherTask extends AsyncTask<String, String, Void> {
        String result = "";
        private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        protected void onPreExecute() {
            progressDialog.setMessage("Downloading your data...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String language = Locale.getDefault().getLanguage();
                if(language.equals("cs")) { language = "cz"; }
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?q=" + URLEncoder.encode(sp.getString("city", "London"), "UTF-8") + "&lang="+ language +"&mode=json&appid=2de143494c0b295cca9337e1e96b00e0");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                if(urlConnection.getResponseCode() == 200) {
                    String line = null;
                    while ((line = r.readLine()) != null) {
                        result += line + "\n";
                    }
                }
                else {
                    Snackbar.make(appView, "There is a problem with your interent connection.", Snackbar.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                Log.e("IOException Data", result);
                e.printStackTrace();
                Snackbar.make(appView, "Connection not available.", Snackbar.LENGTH_LONG).show();
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            //parse JSON data
            parseLongTermJson(result);
            this.progressDialog.dismiss();
        }
    }
}
