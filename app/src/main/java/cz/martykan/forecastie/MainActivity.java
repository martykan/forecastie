package cz.martykan.forecastie;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;
    Typeface weatherFont;
    Weather todayWeather = new Weather();

    TextView todayTemperature;
    TextView todayDescription;
    TextView todayWind;
    TextView todayPressure;
    TextView todayHumidity;
    TextView todayIcon;
    ViewPager viewPager;
    TabLayout tabLayout;

    View appView;

    ProgressDialog progressDialog;
    int loading = 0;

    boolean darkTheme;
    LocationManager locationManager;
    double latitude;
    double longtitude;
    private List<Weather> longTermWeather;
    private List<Weather> longTermTodayWeather;
    private List<Weather> longTermTomorrowWeather;
    private String mApiKey;
    private String recentCity = "";

    private static void close(Closeable x) {
        try {
            if (x != null) {
                x.close();
            }
        } catch (IOException e) {
            Log.e("IOException Data", "Error occurred while closing stream");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize the associated SharedPreferences file with default values
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

        darkTheme = false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean("darkTheme", false)) {
            setTheme(R.style.AppTheme_NoActionBar_Dark);
            darkTheme = true;
        }
        mApiKey = prefs.getString("apiKey", getResources().getString(R.string.apiKey));

        // Initiate activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        appView = findViewById(R.id.viewApp);

        progressDialog = new ProgressDialog(MainActivity.this);

        // Load toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (darkTheme) {
            toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Dark);
        }

        // Initialize textboxes
        todayTemperature = (TextView) findViewById(R.id.todayTemperature);
        todayDescription = (TextView) findViewById(R.id.todayDescription);
        todayWind = (TextView) findViewById(R.id.todayWind);
        todayPressure = (TextView) findViewById(R.id.todayPressure);
        todayHumidity = (TextView) findViewById(R.id.todayHumidity);
        todayIcon = (TextView) findViewById(R.id.todayIcon);
        weatherFont = Typeface.createFromAsset(this.getAssets(), "fonts/weather.ttf");
        todayIcon.setTypeface(weatherFont);

        // Initialize viewPager
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        // Preload data from cache
        preloadWeather();

        // Update weather online
        if (isNetworkAvailable()) {
            getTodayWeather();
            getLongTermWeather();
        }

        // Set autoupdater
        AlarmReceiver.setRecurringAlarm(this);
    }

    public WeatherRecyclerAdapter getAdapter(int id) {
        WeatherRecyclerAdapter weatherRecyclerAdapter;
        if (id == 0) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermTodayWeather);
        } else if (id == 1) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermTomorrowWeather);
        } else {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermWeather);
        }
        return weatherRecyclerAdapter;
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean darkTheme =
                PreferenceManager.getDefaultSharedPreferences(this).getBoolean("darkTheme", false);
        if (darkTheme != this.darkTheme) {
            // Restart activity to apply theme
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
        } else if (isNetworkAvailable()) {
            mApiKey = PreferenceManager.getDefaultSharedPreferences(this).getString("apiKey", getResources().getString(R.string.apiKey));
            getTodayWeather();
            getLongTermWeather();
        }
    }

    private void preloadWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        String lastToday = sp.getString("lastToday", "");
        if (!lastToday.isEmpty()) {
            parseTodayJson(lastToday);
        }
        String lastLongterm = sp.getString("lastLongterm", "");
        if (!lastLongterm.isEmpty()) {
            parseLongTermJson(lastLongterm);
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
        alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String result = input.getText().toString();
                if (!result.isEmpty()) {
                    saveLocation(result);
                }
            }
        });
        alert.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cancelled
            }
        });
        alert.show();
    }

    private void saveLocation(String result) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        recentCity = preferences.getString("city", "");

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("city", result);
        editor.commit();
        getTodayWeather();
        getLongTermWeather();
    }

    private void aboutDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Forecastie");
        final WebView webView = new WebView(this);
        String about = "<p>A lightweight, opensource weather app.</p>" +
                "<p>Developed by <a href='mailto:t.martykan@gmail.com'>Tomas Martykan</a></p>" +
                "<p>Data provided by <a href='http://openweathermap.org/'>OpenWeatherMap</a>, under the <a href='http://creativecommons.org/licenses/by-sa/2.0/'>Creative Commons license</a>" +
                "<p>Icons are <a href='https://erikflowers.github.io/weather-icons/'>Weather Icons</a>, by <a href='http://www.twitter.com/artill'>Lukas Bischoff</a> and <a href='http://www.twitter.com/Erik_UX'>Erik Flowers</a>, under the <a href='http://scripts.sil.org/OFL'>SIL OFL 1.1</a> licence.";
        if (darkTheme) {
            // Style text color for dark theme
            about = "<style media=\"screen\" type=\"text/css\">" +
                    "body {\n" +
                    "    color:white;\n" +
                    "}\n" +
                    "a:link {color:cyan}\n" +
                    "</style>" +
                    about;
        }
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.loadData(about, "text/html", "UTF-8");
        alert.setView(webView, 32, 0, 32, 0);
        alert.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        alert.show();
    }

    private String setWeatherIcon(int actualId, int hourOfDay) {
        int id = actualId / 100;
        String icon = "";
        if (actualId == 800) {
            if (hourOfDay >= 7 && hourOfDay < 20) {
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

    private String getRainString(JSONObject rainObj) {
        String rain = "0";
        if (rainObj != null) {
            rain = rainObj.optString("3h", "fail");
            if ("fail".equals(rain)) {
                rain = rainObj.optString("1h", "0");
            }
        }
        return rain;
    }

    private ParseResult parseTodayJson(String result) {
        try {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

            JSONObject reader = new JSONObject(result);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                Snackbar.make(appView, getString(R.string.msg_city_not_found), Snackbar.LENGTH_LONG).show();
                return ParseResult.CITY_NOT_FOUND;
            }

            String city = reader.getString("name");
            String country = "";
            JSONObject countryObj = reader.optJSONObject("sys");
            if (countryObj != null) {
                country = countryObj.getString("country");
            }
            todayWeather.setCity(city);
            todayWeather.setCountry(country);
            getSupportActionBar().setTitle(city + (country.isEmpty() ? "" : ", " + country));

            JSONObject main = reader.getJSONObject("main");

            String temperature = main.getString("temp");
            todayWeather.setTemperature(temperature);

            if (sp.getString("unit", "C").equals("C")) {
                temperature = Float.parseFloat(temperature) - 273.15 + "";
            }

            if (sp.getString("unit", "C").equals("F")) {
                temperature = (((9 * (Float.parseFloat(temperature) - 273.15)) / 5) + 32) + "";
            }

            todayWeather.setDescription(reader.getJSONArray("weather").getJSONObject(0).getString("description"));
            todayWeather.setWind(reader.getJSONObject("wind").getString("speed"));
            todayWeather.setPressure(main.getString("pressure"));
            todayWeather.setHumidity(main.getString("humidity"));

            JSONObject rainObj = reader.optJSONObject("rain");
            String rain;
            if (rainObj != null) {
                rain = getRainString(rainObj);
            } else {
                JSONObject snowObj = reader.optJSONObject("snow");
                if (snowObj != null) {
                    rain = getRainString(snowObj);
                } else {
                    rain = "0";
                }
            }
            todayWeather.setRain(rain);

            final String idString = reader.getJSONArray("weather").getJSONObject(0).getString("id");
            todayWeather.setId(idString);
            todayWeather.setIcon(setWeatherIcon(Integer.parseInt(idString), Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));

            double wind = Double.parseDouble(todayWeather.getWind());
            if (sp.getString("speedUnit", "m/s").equals("kph")) {
                wind = wind * 3.59999999712;
            }

            if (sp.getString("speedUnit", "m/s").equals("mph")) {
                wind = wind * 2.23693629205;
            }

            double pressure = Double.parseDouble(todayWeather.getPressure());
            if (sp.getString("pressureUnit", "hPa").equals("kPa")) {
                pressure = pressure / 10;
            }
            if (sp.getString("pressureUnit", "hPa").equals("mm Hg")) {
                pressure = pressure * 0.750061561303;
            }

            todayTemperature.setText(temperature.substring(0, temperature.indexOf(".") + 2) + " °" + sp.getString("unit", "C"));
            if (Float.parseFloat(todayWeather.getRain()) > 0.1) {
                todayDescription.setText(todayWeather.getDescription().substring(0, 1).toUpperCase() + todayWeather.getDescription().substring(1) + " (" + todayWeather.getRain().substring(0, todayWeather.getRain().indexOf(".") + 2) + " mm)");
            } else {
                todayDescription.setText(todayWeather.getDescription().substring(0, 1).toUpperCase() + todayWeather.getDescription().substring(1));
            }
            todayWind.setText(getString(R.string.wind) + ": " + (wind + "").substring(0, (wind + "").indexOf(".") + 2) + " " + sp.getString("speedUnit", "m/s"));
            todayPressure.setText(getString(R.string.pressure) + ": " + (pressure + "").substring(0, (pressure + "").indexOf(".") + 2) + " " + sp.getString("pressureUnit", "hPa"));
            todayHumidity.setText(getString(R.string.humidity) + ": " + todayWeather.getHumidity() + " %");
            todayIcon.setText(todayWeather.getIcon());

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putString("lastToday", result);
            editor.commit();

            return ParseResult.OK;
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            Snackbar.make(appView, getString(R.string.msg_err_parsing_json), Snackbar.LENGTH_LONG).show();
            return ParseResult.JSON_EXCEPTION;
        }
    }

    public ParseResult parseLongTermJson(String result) {
        int i;
        try {
            JSONObject reader = new JSONObject(result);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                Snackbar.make(appView, getString(R.string.msg_city_not_found), Snackbar.LENGTH_LONG).show();
                return ParseResult.CITY_NOT_FOUND;
            }

            JSONArray list = reader.getJSONArray("list");
            longTermWeather = new ArrayList<>();
            longTermTodayWeather = new ArrayList<>();
            longTermTomorrowWeather = new ArrayList<>();

            for (i = 0; i < list.length(); i++) {
                Weather weather = new Weather();

                JSONObject listItem = list.getJSONObject(i);
                JSONObject main = listItem.getJSONObject("main");

                weather.setDate(listItem.getString("dt"));
                weather.setTemperature(main.getString("temp"));
                weather.setDescription(listItem.optJSONArray("weather").getJSONObject(0).getString("description"));
                weather.setWind(listItem.optJSONObject("wind").getString("speed"));
                weather.setPressure(main.getString("pressure"));
                weather.setHumidity(main.getString("humidity"));

                JSONObject rainObj = listItem.optJSONObject("rain");
                String rain = "";
                if (rainObj != null) {
                    rain = getRainString(rainObj);
                } else {
                    JSONObject snowObj = listItem.optJSONObject("snow");
                    if (snowObj != null) {
                        rain = getRainString(snowObj);
                    } else {
                        rain = "0";
                    }
                }
                weather.setRain(rain);

                final String idString = listItem.optJSONArray("weather").getJSONObject(0).getString("id");
                weather.setId(idString);

                final String dateMsString = listItem.getString("dt") + "000";
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(dateMsString));
                weather.setIcon(setWeatherIcon(Integer.parseInt(idString), cal.get(Calendar.HOUR_OF_DAY)));

                if (cal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
                    longTermTodayWeather.add(weather);
                } else if (cal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + 1
                        ) {
                    longTermTomorrowWeather.add(weather);
                } else {
                    longTermWeather.add(weather);
                }
            }
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putString("lastLongterm", result);
            editor.commit();
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            Snackbar.make(appView, getString(R.string.msg_err_parsing_json), Snackbar.LENGTH_LONG).show();
            return ParseResult.JSON_EXCEPTION;
        }
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        Bundle bundleToday = new Bundle();
        bundleToday.putInt("day", 0);
        RecyclerViewFragment recyclerViewFragmentToday = new RecyclerViewFragment();
        recyclerViewFragmentToday.setArguments(bundleToday);
        viewPagerAdapter.addFragment(recyclerViewFragmentToday, getString(R.string.today));

        Bundle bundleTomorrow = new Bundle();
        bundleTomorrow.putInt("day", 1);
        RecyclerViewFragment recyclerViewFragmentTomorrow = new RecyclerViewFragment();
        recyclerViewFragmentTomorrow.setArguments(bundleTomorrow);
        viewPagerAdapter.addFragment(recyclerViewFragmentTomorrow, getString(R.string.tomorrow));

        Bundle bundle = new Bundle();
        bundle.putInt("day", 2);
        RecyclerViewFragment recyclerViewFragment = new RecyclerViewFragment();
        recyclerViewFragment.setArguments(bundle);
        viewPagerAdapter.addFragment(recyclerViewFragment, getString(R.string.later));

        viewPagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        return ParseResult.OK;
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
        if (id == R.id.action_location) {
            getCityByLocation();
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

    private void restorePreviousCity() {
        if (!TextUtils.isEmpty(recentCity)) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putString("city", recentCity);
            editor.commit();
            recentCity = "";
        }
    }

    void getCityByLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                // Explanation not needed, since user requests this himself

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_ACCESS_FINE_LOCATION);
            }

        } else {
            progressDialog.setMessage(getString(R.string.getting_location));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCityByLocation();
                }
                return;
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        progressDialog.hide();
        Log.i("GPS LOCATION", location.getLatitude() + ", " + location.getLongitude());
        latitude = location.getLatitude();
        longtitude = location.getLongitude();
        new GetCityName().execute();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private enum ParseResult {OK, JSON_EXCEPTION, CITY_NOT_FOUND}

    public class GetCityName extends AsyncTask<String, String, Void> {
        String result = "";

        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String language = Locale.getDefault().getLanguage();
                if (language.equals("cs")) {
                    language = "cz";
                }
                String apiKey = sp.getString("apiKey", getResources().getString(R.string.apiKey));
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longtitude + "&lang=" + language + "&mode=json&appid=" + apiKey);
                Log.i("URL", url.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                if (urlConnection.getResponseCode() == 200) {
                    String line = null;
                    while ((line = r.readLine()) != null) {
                        result += line + "\n";
                    }
                    close(r);
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                Log.e("IOException Data", result);
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            Log.i("RESULT", result.toString());
            try {
                JSONObject reader = new JSONObject(result);

                final String code = reader.optString("cod");
                if ("404".equals(code)) {
                    Log.e("Geolocation", "No city found");
                }

                String city = reader.getString("name");
                String country = "";
                JSONObject countryObj = reader.optJSONObject("sys");
                if (countryObj != null) {
                    country = ", " + countryObj.getString("country");
                }

                saveLocation(city + country);

            } catch (JSONException e) {
                Log.e("JSONException Data", result);
                e.printStackTrace();
            }
        }
    }

    public class GetWeatherTask extends AsyncTask<String, String, Void> {
        String result = "";

        protected void onPreExecute() {
            loading = 1;
            if (!progressDialog.isShowing()) {
                progressDialog.setMessage(getString(R.string.downloading_data));
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String language = Locale.getDefault().getLanguage();
                if (language.equals("cs")) {
                    language = "cz";
                }
                String apiKey = sp.getString("apiKey", getResources().getString(R.string.apiKey));
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + URLEncoder.encode(sp.getString("city", Constants.DEFAULT_CITY), "UTF-8") + "&lang=" + language + "&mode=json&appid=" + apiKey);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                if (urlConnection.getResponseCode() == 200) {
                    String line = null;
                    while ((line = r.readLine()) != null) {
                        result += line + "\n";
                    }
                    close(r);
                    urlConnection.disconnect();
                } else {
                    Snackbar.make(appView, getString(R.string.msg_connection_problem), Snackbar.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                Log.e("IOException Data", result);
                e.printStackTrace();
                Snackbar.make(appView, getString(R.string.msg_connection_not_available), Snackbar.LENGTH_LONG).show();
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            //parse JSON data
            if (loading == 1) {
                progressDialog.dismiss();
            }
            loading -= 1;
            final ParseResult parseResult = parseTodayJson(result);
            if (ParseResult.CITY_NOT_FOUND.equals(parseResult)) {
                // Retain previously specified city if current one was not recognized
                restorePreviousCity();
            }
        }
    }

    class GetLongTermWeatherTask extends AsyncTask<String, String, Void> {
        String result = "";

        protected void onPreExecute() {
            loading += 1;
            if (!progressDialog.isShowing()) {
                progressDialog.setMessage(getString(R.string.downloading_data));
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String language = Locale.getDefault().getLanguage();
                if (language.equals("cs")) {
                    language = "cz";
                }

                String apiKey = sp.getString("apiKey", getResources().getString(R.string.apiKey));
                URL url = new URL("http://api.openweathermap.org/data/2.5/forecast?q=" + URLEncoder.encode(sp.getString("city", Constants.DEFAULT_CITY), "UTF-8") + "&lang=" + language + "&mode=json&appid=" + apiKey);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                if (urlConnection.getResponseCode() == 200) {
                    String line = null;
                    while ((line = r.readLine()) != null) {
                        result += line + "\n";
                    }
                    close(r);
                    urlConnection.disconnect();
                } else {
                    Snackbar.make(appView, "There is a problem with your internet connection.", Snackbar.LENGTH_LONG).show();
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
            if (loading == 1) {
                progressDialog.dismiss();
            }
            loading -= 1;
            final ParseResult parseResult = parseLongTermJson(result);
            if (ParseResult.CITY_NOT_FOUND.equals(parseResult)) {
                // Retain previously specified city if current one was not recognized
                restorePreviousCity();
            }
        }
    }
}
