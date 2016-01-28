package cz.martykan.forecastie;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

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

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1;
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

    private List<Weather> longTermWeather;
    private List<Weather> longTermTodayWeather;
    private List<Weather> longTermTomorrowWeather;

    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        darkTheme = false;
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("darkTheme", false)) {
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
        setRecurringAlarm(this);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                                       .addConnectionCallbacks(this)
                                       .addOnConnectionFailedListener(this)
                                       .addApi(LocationServices.API)
                                       .build();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    onConnectedTask();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        onConnectedTask();
    }

    private void onConnectedTask() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                                                                           Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Provide Location Permission")
                        .setMessage("Require Location permission to access your location")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                                                         new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                                         MY_PERMISSIONS_ACCESS_COARSE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            return;
        }
        Log.d("MainActivity", "Got Location");
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            String lat = String.valueOf(mLastLocation.getLatitude());
            String lon = String.valueOf(mLastLocation.getLongitude());
            saveLocation(lat, lon);
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("MainActivity", "Connection Failed");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("MainActivity", "suspended "+i);
    }

    public WeatherRecyclerAdapter getAdapter(int id){
        WeatherRecyclerAdapter weatherRecyclerAdapter;
        if(id == 0) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermTodayWeather);
        }
        else if (id == 1) {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermTomorrowWeather);
        }
        else {
            weatherRecyclerAdapter = new WeatherRecyclerAdapter(this, longTermWeather);
        }
        return  weatherRecyclerAdapter;
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
            getTodayWeather();
            getLongTermWeather();
        }
    }

    private void setRecurringAlarm(Context context) {
        String interval = PreferenceManager.getDefaultSharedPreferences(this).getString("refreshInterval", "1");
        if(!interval.equals("0")) {
            Intent refresh = new Intent(context, AlarmReceiver.class);
            PendingIntent recurringRefresh = PendingIntent.getBroadcast(context,
                    0, refresh, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager alarms = (AlarmManager) getSystemService(
                    Context.ALARM_SERVICE);
            if(interval.equals("15")) {
                alarms.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, AlarmManager.INTERVAL_FIFTEEN_MINUTES, AlarmManager.INTERVAL_FIFTEEN_MINUTES, recurringRefresh);
            }
            else if(interval.equals("30")) {
                alarms.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, AlarmManager.INTERVAL_HALF_HOUR, AlarmManager.INTERVAL_HALF_HOUR, recurringRefresh);
            }
            else if(interval.equals("1")) {
                alarms.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, AlarmManager.INTERVAL_HOUR, AlarmManager.INTERVAL_HOUR, recurringRefresh);
            }
            else if(interval.equals("12")) {
                alarms.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, AlarmManager.INTERVAL_HALF_DAY, AlarmManager.INTERVAL_HALF_DAY, recurringRefresh);
            }
            else if(interval.equals("24")) {
                alarms.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, recurringRefresh);
            }
        }
    }

    private void preloadWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        if (sp.getString("lastToday", "{}") != "{}") {
            parseTodayJson(sp.getString("lastToday", "{}"));
        }
        if (sp.getString("lastLongterm", "{}") != "{}") {
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
                    saveLocation(result);
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

    private void saveLocation(String result) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
        editor.putString("city", result);
        editor.commit();
        getTodayWeather();
        getLongTermWeather();
    }

    private void saveLocation(String lat, String lon) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
        editor.putString("lat", lat);
        editor.putString("lon", lon);
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
            } catch (Exception e) {
                try {
                    todayWeather.setRain(reader.optJSONObject("rain").getString("3h").toString());
                } catch (Exception e2) {
                    try {
                        todayWeather.setRain(reader.optJSONObject("snow").getString("1h").toString());
                    } catch (Exception e3) {
                        try {
                            todayWeather.setRain(reader.optJSONObject("snow").getString("3h").toString());
                        } catch (Exception e4) {
                            todayWeather.setRain("0");
                        }
                    }
                }
            }
            todayWeather.setId(reader.optJSONArray("weather").getJSONObject(0).getString("id").toString());
            todayWeather.setIcon(setWeatherIcon(Integer.parseInt(reader.optJSONArray("weather").getJSONObject(0).getString("id").toString()), Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));


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
            longTermTodayWeather = new ArrayList<>();
            longTermTomorrowWeather = new ArrayList<>();

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
                } catch (Exception e) {
                    try {
                        weather.setRain(list.getJSONObject(i).optJSONObject("rain").getString("3h").toString());
                    } catch (Exception e2) {
                        try {
                            weather.setRain(list.getJSONObject(i).optJSONObject("snow").getString("1h").toString());
                        } catch (Exception e3) {
                            try {
                                weather.setRain(list.getJSONObject(i).optJSONObject("snow").getString("3h").toString());
                            } catch (Exception e4) {
                                weather.setRain("0");
                            }
                        }
                    }
                }
                weather.setId(list.getJSONObject(i).optJSONArray("weather").getJSONObject(0).getString("id").toString());
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(list.getJSONObject(i).getString("dt")) * 1000);
                weather.setIcon(setWeatherIcon(Integer.parseInt(list.getJSONObject(i).optJSONArray("weather").getJSONObject(0).getString("id").toString()), cal.get(Calendar.HOUR_OF_DAY)));
                if(cal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)){
                    longTermTodayWeather.add(weather);
                }
                else if(cal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)+1
                        ){
                    longTermTomorrowWeather.add(weather);
                }
                else {
                    longTermWeather.add(weather);
                }
            }
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            Snackbar.make(appView, "Error parsing JSON.", Snackbar.LENGTH_LONG).show();
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
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
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

        protected void onPreExecute() {
            loading = 1;
            if(!progressDialog.isShowing()) {
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
                boolean autoDetectLocation = sp.getBoolean("autoDetectLocation", true);
                URL url;
                if (sp.getString("lat", "abc").equals("abc") || !autoDetectLocation) {
                    url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + URLEncoder.encode(sp.getString("city", Constants.DEFAULT_CITY), "UTF-8") + "&lang=" + language + "&appid=78dfe9e10dd180fadd805075dd1a10d6");
                } else {
                    url = new URL("http://api.openweathermap.org/data/2.5/weather?" +
                                          "lat=" + sp.getString("lat", Constants.DEFAULT_LAT) +"&lon="+sp.getString("lon", Constants.DEFAULT_LON)+ "&lang=" + language + "&appid=78dfe9e10dd180fadd805075dd1a10d6");
                }
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                if (urlConnection.getResponseCode() == 200) {
                    String line = null;
                    while ((line = r.readLine()) != null) {
                        result += line + "\n";
                    }
                } else {
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
            if(loading == 1) {
                progressDialog.dismiss();
            }
            loading -= 1;
            parseTodayJson(result);
        }
    }

    class GetLongTermWeatherTask extends AsyncTask<String, String, Void> {
        String result = "";

        protected void onPreExecute() {
            loading += 1;
            if(!progressDialog.isShowing()) {
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

                boolean autoDetectLocation = sp.getBoolean("autoDetectLocation", true);
                URL url;
                if (sp.getString("lat", "abc").equals("abc") || !autoDetectLocation) {
                    url = new URL("http://api.openweathermap.org/data/2.5/forecast?q=" + URLEncoder.encode(sp.getString("city", Constants.DEFAULT_CITY), "UTF-8") + "&lang=" + language + "&mode=json&appid=78dfe9e10dd180fadd805075dd1a10d6");
                }else{
                    url = new URL("http://api.openweathermap.org/data/2.5/forecast?" +
                                          "lat=" + sp.getString("lat", Constants.DEFAULT_LAT) +"&lon="+sp.getString("lon", Constants.DEFAULT_LON)+ "&lang=" + language + "&mode=json&appid=78dfe9e10dd180fadd805075dd1a10d6");
                }
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                if (urlConnection.getResponseCode() == 200) {
                    String line = null;
                    while ((line = r.readLine()) != null) {
                        result += line + "\n";
                    }
                } else {
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
            if(loading == 1) {
                progressDialog.dismiss();
            }
            loading -= 1;
            parseLongTermJson(result);
        }
    }
}
