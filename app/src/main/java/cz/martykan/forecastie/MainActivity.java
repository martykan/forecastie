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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cz.martykan.forecastie.widgets.AbstractWidgetProvider;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;

    // Time in milliseconds; only reload weather if last update is longer ago than this value
    private static final int NO_UPDATE_REQUIRED_THRESHOLD = 300000;

    private static Map<String, Integer> speedUnits = new HashMap<>(3);
    private static Map<String, Integer> pressUnits = new HashMap<>(3);
    private static boolean mappingsInitialised = false;

    Typeface weatherFont;
    Weather todayWeather = new Weather();

    TextView todayTemperature;
    TextView todayDescription;
    TextView todayWind;
    TextView todayPressure;
    TextView todayHumidity;
    TextView todaySunrise;
    TextView todaySunset;
    TextView lastUpdate;
    TextView todayIcon;
    ViewPager viewPager;
    TabLayout tabLayout;

    View appView;

    ProgressDialog progressDialog;
    int loading = 0;

    boolean darkTheme;
    boolean destroyed = false;

    private List<Weather> longTermWeather;
    private List<Weather> longTermTodayWeather;
    private List<Weather> longTermTomorrowWeather;
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
        todaySunrise = (TextView) findViewById(R.id.todaySunrise);
        todaySunset = (TextView) findViewById(R.id.todaySunset);
        lastUpdate = (TextView) findViewById(R.id.lastUpdate);
        todayIcon = (TextView) findViewById(R.id.todayIcon);
        weatherFont = Typeface.createFromAsset(this.getAssets(), "fonts/weather.ttf");
        todayIcon.setTypeface(weatherFont);

        // Initialize viewPager
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        destroyed = false;

        initMappings();

        // Preload data from cache
        preloadWeather();
        updateLastUpdateTime();

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
        } else if (shouldUpdate() && isNetworkAvailable()) {
            getTodayWeather();
            getLongTermWeather();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyed = true;
    }

    private void preloadWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        String lastToday = sp.getString("lastToday", "");
        if (!lastToday.isEmpty()) {
            new TodayWeatherTask().execute("cachedResponse", lastToday);
        }
        String lastLongterm = sp.getString("lastLongterm", "");
        if (!lastLongterm.isEmpty()) {
            new LongTermWeatherTask().execute("cachedResponse", lastLongterm);
        }
    }

    private void getTodayWeather() {
        new TodayWeatherTask().execute();
    }

    private void getLongTermWeather() {
        new LongTermWeatherTask().execute();
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
        recentCity = preferences.getString("city", Constants.DEFAULT_CITY);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("city", result);
        editor.commit();

        if (!recentCity.equals(result)) {
            // New location, update weather
            getTodayWeather();
            getLongTermWeather();
        }
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
            JSONObject reader = new JSONObject(result);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                return ParseResult.CITY_NOT_FOUND;
            }

            String city = reader.getString("name");
            String country = "";
            JSONObject countryObj = reader.optJSONObject("sys");
            if (countryObj != null) {
                country = countryObj.getString("country");
                todayWeather.setSunrise(countryObj.getString("sunrise"));
                todayWeather.setSunset(countryObj.getString("sunset"));
            }
            todayWeather.setCity(city);
            todayWeather.setCountry(country);

            JSONObject main = reader.getJSONObject("main");

            todayWeather.setTemperature(main.getString("temp"));
            todayWeather.setDescription(reader.getJSONArray("weather").getJSONObject(0).getString("description"));
            JSONObject windObj = reader.getJSONObject("wind");
            todayWeather.setWind(windObj.getString("speed"));
            if (windObj.has("deg")) {
                todayWeather.setWindDirectionDegree(windObj.getDouble("deg"));
            } else {
                Log.e("parseTodayJson", "No wind direction available");
                todayWeather.setWindDirectionDegree(null);
            }
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

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
            editor.putString("lastToday", result);
            editor.commit();

        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }

    private void updateTodayWeatherUI() {
        String city = todayWeather.getCity();
        String country = todayWeather.getCountry();
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
        getSupportActionBar().setTitle(city + (country.isEmpty() ? "" : ", " + country));

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        float temperature = Float.parseFloat(todayWeather.getTemperature());
        if (sp.getString("unit", "C").equals("C")) {
            temperature = temperature - 273.15f;
        }

        if (sp.getString("unit", "C").equals("F")) {
            temperature = (((9 * (temperature - 273.15f)) / 5) + 32);
        }

        double rain = Double.parseDouble(todayWeather.getRain());
        String rainString = "";
        if(rain > 0)
        {
            if (sp.getString("lengthUnit", "mm").equals("mm")) {
                if(rain < 0.1) {
                    rainString = " (<0.1 mm)";
                }
                else {
                    rainString = String.format(Locale.ENGLISH," (%.1f %s)", rain, sp.getString("lengthUnit", "mm"));
                }
            }
            else {
                rain = rain/25.4;
                if(rain < 0.01) {
                    rainString = " (<0.01 in)";
                }
                else {
                    rainString = String.format(Locale.ENGLISH," (%.2f %s)", rain, sp.getString("lengthUnit", "mm"));
                }
            }

        }

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



        todayTemperature.setText(new DecimalFormat("#.#").format(temperature) + " °" + sp.getString("unit", "C"));
        todayDescription.setText(todayWeather.getDescription().substring(0, 1).toUpperCase() +
                    todayWeather.getDescription().substring(1) + rainString);
        todayWind.setText(getString(R.string.wind) + ": " + new DecimalFormat("#.0").format(wind) + " " +
                localize(sp, "speedUnit", "m/s") +
                (todayWeather.isWindDirectionAvailable() ? " " + getWindDirectionString(sp, this, todayWeather) : ""));
        todayPressure.setText(getString(R.string.pressure) + ": " + new DecimalFormat("#.0").format(pressure) + " " +
                localize(sp, "pressureUnit", "hPa"));
        todayHumidity.setText(getString(R.string.humidity) + ": " + todayWeather.getHumidity() + " %");
        todaySunrise.setText(getString(R.string.sunrise) + ": " + timeFormat.format(todayWeather.getSunrise()));
        todaySunset.setText(getString(R.string.sunset) + ": " + timeFormat.format(todayWeather.getSunset()));
        todayIcon.setText(todayWeather.getIcon());
    }

    public ParseResult parseLongTermJson(String result) {
        int i;
        try {
            JSONObject reader = new JSONObject(result);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                return ParseResult.CITY_NOT_FOUND;
            }

            longTermWeather = new ArrayList<>();
            longTermTodayWeather = new ArrayList<>();
            longTermTomorrowWeather = new ArrayList<>();

            JSONArray list = reader.getJSONArray("list");
            for (i = 0; i < list.length(); i++) {
                Weather weather = new Weather();

                JSONObject listItem = list.getJSONObject(i);
                JSONObject main = listItem.getJSONObject("main");

                weather.setDate(listItem.getString("dt"));
                weather.setTemperature(main.getString("temp"));
                weather.setDescription(listItem.optJSONArray("weather").getJSONObject(0).getString("description"));
                JSONObject windObj = listItem.optJSONObject("wind");
                if(windObj!=null) {
                    weather.setWind(windObj.getString("speed"));
                    weather.setWindDirectionDegree(windObj.getDouble("deg"));
                }
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

                Calendar today = Calendar.getInstance();
                if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                    longTermTodayWeather.add(weather);
                } else if (cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) + 1) {
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
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }

    private void updateLongTermWeatherUI() {
        if (destroyed) {
            return;
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

        int currentPage = viewPager.getCurrentItem();

        viewPagerAdapter.notifyDataSetChanged();
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        if (currentPage == 0 && longTermTodayWeather.isEmpty()) {
            currentPage = 1;
        }
        viewPager.setCurrentItem(currentPage, false);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean shouldUpdate() {
        long lastUpdate = PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", -1);
        // Update if never checked or last update is longer ago than specified threshold
        return lastUpdate < 0 || (Calendar.getInstance().getTimeInMillis() - lastUpdate) > NO_UPDATE_REQUIRED_THRESHOLD;
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
            if(isNetworkAvailable()) {
                getTodayWeather();
                getLongTermWeather();
            }
            else {
                Snackbar.make(appView, getString(R.string.msg_connection_not_available), Snackbar.LENGTH_LONG).show();
            }
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

    public static void initMappings() {
        if (mappingsInitialised)
            return;
        mappingsInitialised = true;
        speedUnits.put("m/s", R.string.speed_unit_mps);
        speedUnits.put("kph", R.string.speed_unit_kph);
        speedUnits.put("mph", R.string.speed_unit_mph);

        pressUnits.put("hPa", R.string.pressure_unit_hpa);
        pressUnits.put("kPa", R.string.pressure_unit_kpa);
        pressUnits.put("mm Hg", R.string.pressure_unit_mmhg);
    }

    private String localize(SharedPreferences sp, String preferenceKey, String defaultValueKey) {
        return localize(sp, this, preferenceKey, defaultValueKey);
    }

    public static String localize(SharedPreferences sp, Context context, String preferenceKey, String defaultValueKey) {
        String preferenceValue = sp.getString(preferenceKey, defaultValueKey);
        String result = preferenceValue;
        if ("speedUnit".equals(preferenceKey)) {
            if (speedUnits.containsKey(preferenceValue)) {
                result = context.getString(speedUnits.get(preferenceValue));
            }
        } else if ("pressureUnit".equals(preferenceKey)) {
            if (pressUnits.containsKey(preferenceValue)) {
                result = context.getString(pressUnits.get(preferenceValue));
            }
        }
        return result;
    }

    public static String getWindDirectionString(SharedPreferences sp, Context context, Weather weather) {
        try {
            if (Double.parseDouble(weather.getWind()) != 0) {
                String pref = sp.getString("windDirectionFormat", null);
                if ("arrow".equals(pref)) {
                    return weather.getWindDirection(8).getArrow(context);
                } else if ("abbr".equals(pref)) {
                    return weather.getWindDirection().getLocalizedString(context);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return "";
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
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        new ProvideCityNameTask().execute("coords", Double.toString(latitude), Double.toString(longitude));
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

    abstract class GenericRequestTask extends AsyncTask<String, String, TaskOutput> {
        private void incLoadingCounter() {
            loading++;
        }

        private void decLoadingCounter() {
            loading--;
        }

        @Override
        protected void onPreExecute() {
            incLoadingCounter();
            if(!progressDialog.isShowing()) {
                progressDialog.setMessage(getString(R.string.downloading_data));
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }
        }

        @Override
        protected TaskOutput doInBackground(String... params) {
            TaskOutput output = new TaskOutput();

            String response = "";
            String[] coords = new String[]{};

            if (params != null && params.length > 0) {
                final String zeroParam = params[0];
                if ("cachedResponse".equals(zeroParam)) {
                    response = params[1];
                    // Actually we did nothing in this case :)
                    output.taskResult = TaskResult.SUCCESS;
                } else if ("coords".equals(zeroParam)) {
                    String lat = params[1];
                    String lon = params[2];
                    coords = new String[]{lat, lon};
                }
            }

            if (response.isEmpty()) {
                try {
                    URL url = provideURL(coords);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    if (urlConnection.getResponseCode() == 200) {
                        InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
                        BufferedReader r = new BufferedReader(inputStreamReader);

                        int responseCode = urlConnection.getResponseCode();
                        String line = null;
                        while ((line = r.readLine()) != null) {
                            response += line + "\n";
                        }
                        close(r);
                        urlConnection.disconnect();
                        // Background work finished successfully
                        Log.i("Task", "done successfully");
                        output.taskResult = TaskResult.SUCCESS;
                        // Save date/time for latest successful result
                        saveLastUpdateTime(PreferenceManager.getDefaultSharedPreferences(MainActivity.this));
                    }
                    else if (urlConnection.getResponseCode() == 429) {
                        // Too many requests
                        Log.i("Task", "too many requests");
                        output.taskResult = TaskResult.TOO_MANY_REQUESTS;
                    }
                    else {
                        // Bad response from server
                        Log.i("Task", "bad response");
                        output.taskResult = TaskResult.BAD_RESPONSE;
                    }
                } catch (IOException e) {
                    Log.e("IOException Data", response);
                    e.printStackTrace();
                    // Exception while reading data from url connection
                    output.taskResult = TaskResult.IO_EXCEPTION;
                }
            }

            if (TaskResult.SUCCESS.equals(output.taskResult)) {
                // Parse JSON data
                ParseResult parseResult = parseResponse(response);
                if (ParseResult.CITY_NOT_FOUND.equals(parseResult)) {
                    // Retain previously specified city if current one was not recognized
                    restorePreviousCity();
                }
                output.parseResult = parseResult;
            }

            return output;
        }

        @Override
        protected void onPostExecute(TaskOutput output) {
            if(loading == 1) {
                progressDialog.dismiss();
            }
            decLoadingCounter();

            updateMainUI();

            handleTaskOutput(output);
        }

        protected final void handleTaskOutput(TaskOutput output) {
            switch (output.taskResult) {
                case SUCCESS: {
                    ParseResult parseResult = output.parseResult;
                    if (ParseResult.CITY_NOT_FOUND.equals(parseResult)) {
                        Snackbar.make(appView, getString(R.string.msg_city_not_found), Snackbar.LENGTH_LONG).show();
                    } else if (ParseResult.JSON_EXCEPTION.equals(parseResult)) {
                        Snackbar.make(appView, getString(R.string.msg_err_parsing_json), Snackbar.LENGTH_LONG).show();
                    }
                    break;
                }
                case TOO_MANY_REQUESTS: {
                    Snackbar.make(appView, getString(R.string.msg_too_many_requests), Snackbar.LENGTH_LONG).show();
                    break;
                }
                case BAD_RESPONSE: {
                    Snackbar.make(appView, getString(R.string.msg_connection_problem), Snackbar.LENGTH_LONG).show();
                    break;
                }
                case IO_EXCEPTION: {
                    Snackbar.make(appView, getString(R.string.msg_connection_not_available), Snackbar.LENGTH_LONG).show();
                    break;
                }
            }
        }

        private String getLanguage() {
            String language = Locale.getDefault().getLanguage();
            if (language.equals("cs")) {
                language = "cz";
            }
            return language;
        }

        private URL provideURL(String[] coords) throws UnsupportedEncodingException, MalformedURLException {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            final String apiKey = sp.getString("apiKey", getResources().getString(R.string.apiKey));

            StringBuilder urlBuilder = new StringBuilder("http://api.openweathermap.org/data/2.5/");
            urlBuilder.append(getAPIName()).append("?");
            if (coords.length == 2) {
                urlBuilder.append("lat=").append(coords[0]).append("&lon=").append(coords[1]);
            } else {
                final String city = sp.getString("city", Constants.DEFAULT_CITY);
                urlBuilder.append("q=").append(URLEncoder.encode(city, "UTF-8"));
            }
            urlBuilder.append("&lang=").append(getLanguage());
            urlBuilder.append("&mode=json");
            urlBuilder.append("&appid=").append(apiKey);

            return new URL(urlBuilder.toString());
        }

        protected void updateMainUI() { }

        protected abstract ParseResult parseResponse(String response);
        protected abstract String getAPIName();
    }

    class TodayWeatherTask extends GenericRequestTask {
        @Override
        protected void onPreExecute() {
            loading = 0;
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(TaskOutput output) {
            super.onPostExecute(output);
            // Update widgets
            AbstractWidgetProvider.updateWidgets(MainActivity.this);
            DashClockWeatherExtension.updateDashClock(MainActivity.this);
        }

        @Override
        protected ParseResult parseResponse(String response) {
            return parseTodayJson(response);
        }

        @Override
        protected String getAPIName() {
            return "weather";
        }

        @Override
        protected void updateMainUI() {
            updateTodayWeatherUI();
            updateLastUpdateTime();
        }
    }

    class LongTermWeatherTask extends GenericRequestTask {
        @Override
        protected ParseResult parseResponse(String response) {
            return parseLongTermJson(response);
        }

        @Override
        protected String getAPIName() {
            return "forecast";
        }

        @Override
        protected void updateMainUI() {
            updateLongTermWeatherUI();
        }
    }

    class ProvideCityNameTask extends GenericRequestTask {

        @Override
        protected void onPreExecute() { /*Nothing*/ }

        @Override
        protected String getAPIName() {
            return "weather";
        }

        @Override
        protected ParseResult parseResponse(String response) {
            Log.i("RESULT", response.toString());
            try {
                JSONObject reader = new JSONObject(response);

                final String code = reader.optString("cod");
                if ("404".equals(code)) {
                    Log.e("Geolocation", "No city found");
                    return ParseResult.CITY_NOT_FOUND;
                }

                String city = reader.getString("name");
                String country = "";
                JSONObject countryObj = reader.optJSONObject("sys");
                if (countryObj != null) {
                    country = ", " + countryObj.getString("country");
                }

                saveLocation(city + country);

            } catch (JSONException e) {
                Log.e("JSONException Data", response);
                e.printStackTrace();
                return ParseResult.JSON_EXCEPTION;
            }

            return ParseResult.OK;
        }

        @Override
        protected void onPostExecute(TaskOutput output) {
            /* Handle possible errors only */
            handleTaskOutput(output);
        }
    }

    private class TaskOutput {
        /**
         * Indicates result of parsing server response
         */
        ParseResult parseResult;
        /**
         * Indicates result of background task
         */
        TaskResult taskResult;
    }

    private enum ParseResult {OK, JSON_EXCEPTION, CITY_NOT_FOUND}

    private enum TaskResult { SUCCESS, BAD_RESPONSE, IO_EXCEPTION, TOO_MANY_REQUESTS; }

    public static long saveLastUpdateTime(SharedPreferences sp) {
        Calendar now = Calendar.getInstance();
        sp.edit().putLong("lastUpdate", now.getTimeInMillis()).apply();
        return now.getTimeInMillis();
    }

    private void updateLastUpdateTime() {
        updateLastUpdateTime(
                PreferenceManager.getDefaultSharedPreferences(this).getLong("lastUpdate", -1)
        );
    }

    private void updateLastUpdateTime(long timeInMillis) {
        if (timeInMillis < 0) {
            // No time
            lastUpdate.setText("");
        } else {
            lastUpdate.setText(getString(R.string.last_update, formatTimeWithDayIfNotToday(this, timeInMillis)));
        }
    }

    public static String formatTimeWithDayIfNotToday(Context context, long timeInMillis) {
        Calendar now = Calendar.getInstance();
        Calendar lastCheckedCal = new GregorianCalendar();
        lastCheckedCal.setTimeInMillis(timeInMillis);
        Date lastCheckedDate = new Date(timeInMillis);
        String timeFormat = android.text.format.DateFormat.getTimeFormat(context).format(lastCheckedDate);
        if (now.get(Calendar.YEAR) == lastCheckedCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == lastCheckedCal.get(Calendar.DAY_OF_YEAR)) {
            // Same day, only show time
            return timeFormat;
        } else {
            return android.text.format.DateFormat.getDateFormat(context).format(lastCheckedDate) + " " + timeFormat;
        }
    }
}
