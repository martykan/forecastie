package cz.martykan.forecastie.activities;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;

import cz.martykan.forecastie.AlarmReceiver;
import cz.martykan.forecastie.R;
import cz.martykan.forecastie.notifications.WeatherNotificationService;
import cz.martykan.forecastie.utils.UI;

public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected static final int MY_PERMISSIONS_FOREGROUND_SERVICE = 2;

    // Thursday 2016-01-14 16:00:00
    private Date SAMPLE_DATE = new Date(1452805200000L);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        int theme;
        setTheme(theme = UI.getTheme(PreferenceManager.getDefaultSharedPreferences(this).getString("theme", "fresh")));

        boolean darkTheme = theme == R.style.AppTheme_NoActionBar_Dark ||
                theme == R.style.AppTheme_NoActionBar_Classic_Dark;
        boolean blackTheme = theme == R.style.AppTheme_NoActionBar_Black ||
                theme == R.style.AppTheme_NoActionBar_Classic_Black;

        UI.setNavigationBarMode(SettingsActivity.this, darkTheme, blackTheme);

        super.onCreate(savedInstanceState);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        View bar = LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0);
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addPreferencesFromResource(R.xml.prefs);
    }

    @Override
    public void onResume(){
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        setCustomDateEnabled();
        updateDateFormatList();

        // Set summaries to current value
        setListPreferenceSummary("unit");
        setListPreferenceSummary("lengthUnit");
        setListPreferenceSummary("speedUnit");
        setListPreferenceSummary("pressureUnit");
        setListPreferenceSummary("refreshInterval");
        setListPreferenceSummary("windDirectionFormat");
        setListPreferenceSummary("theme");
        setListPreferenceSummary(getString(R.string.settings_notification_type_key));
    }

    @Override
    public void onPause(){
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "unit":
            case "lengthUnit":
            case "speedUnit":
            case "pressureUnit":
            case "windDirectionFormat":
                setListPreferenceSummary(key);
                break;
            case "refreshInterval":
                setListPreferenceSummary(key);
                AlarmReceiver.setRecurringAlarm(this);
                break;
            case "dateFormat":
                setCustomDateEnabled();
                setListPreferenceSummary(key);
                break;
            case "dateFormatCustom":
                updateDateFormatList();
                break;
            case "theme":
                // Restart activity to apply theme
                overridePendingTransition(0, 0);
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                break;
            case "updateLocationAutomatically":
                if (sharedPreferences.getBoolean(key, false)) {
                    requestReadLocationPermission();
                }
                break;
            case "apiKey":
                checkKey(key);
                break;
            default:
                if (key.equalsIgnoreCase(getString(R.string.settings_enable_notification_key))) {
                    if (sharedPreferences.getBoolean(key, false)) {
                        requestForegroundServicePermission();
                    } else {
                        hideNotification();
                    }
                } else if (key.equalsIgnoreCase(getString(R.string.settings_notification_type_key))) {
                    setListPreferenceSummary(key);
                }
                break;
        }
    }

    private void requestReadLocationPermission() {
        System.out.println("Calling request location permission");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Explanation not needed, since user requests this themself

            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MainActivity.MY_PERMISSIONS_ACCESS_FINE_LOCATION);
            }
        } else {
            privacyGuardWorkaround();
        }
    }

    private void requestForegroundServicePermission() {
        System.out.println("Calling request foreground service permission");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
                && ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
                        != PackageManager.PERMISSION_GRANTED) {
            // this is normal permission, so no need to show explanation to user
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.FOREGROUND_SERVICE },
                    MY_PERMISSIONS_FOREGROUND_SERVICE);
        } else {
            showNotification();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MainActivity.MY_PERMISSIONS_ACCESS_FINE_LOCATION:
                boolean permissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                CheckBoxPreference checkBox = (CheckBoxPreference) findPreference("updateLocationAutomatically");
                checkBox.setChecked(permissionGranted);
                if (permissionGranted) {
                    privacyGuardWorkaround();
                }
                break;
            case MY_PERMISSIONS_FOREGROUND_SERVICE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showNotification();
                } else {
                    String enableNotificationKey = getString(R.string.settings_enable_notification_key);
                    CheckBoxPreference notificationCheckBox =
                            (CheckBoxPreference) findPreference(enableNotificationKey);
                    notificationCheckBox.setChecked(false);
                }
                break;
        }
    }

    private void privacyGuardWorkaround() {
        // Workaround for CM privacy guard. Register for location updates in order for it to ask us for permission
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            DummyLocationListener dummyLocationListener = new DummyLocationListener();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, dummyLocationListener);
            locationManager.removeUpdates(dummyLocationListener);
        } catch (SecurityException e) {
            // This will most probably not happen, as we just got granted the permission
        }
    }

    private void showNotification() {
        WeatherNotificationService.start(this);
    }

    private void hideNotification() {
        WeatherNotificationService.stop(this);
    }

    private void setListPreferenceSummary(String preferenceKey) {
        ListPreference preference = (ListPreference) findPreference(preferenceKey);
        preference.setSummary(preference.getEntry());
    }

    private void setCustomDateEnabled() {
        SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
        Preference customDatePref = findPreference("dateFormatCustom");
        customDatePref.setEnabled("custom".equals(sp.getString("dateFormat", "")));
    }

    private void updateDateFormatList() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Resources res = getResources();

        ListPreference dateFormatPref = (ListPreference) findPreference("dateFormat");
        String[] dateFormatsValues = res.getStringArray(R.array.dateFormatsValues);
        String[] dateFormatsEntries = new String[dateFormatsValues.length];

        EditTextPreference customDateFormatPref = (EditTextPreference) findPreference("dateFormatCustom");
        customDateFormatPref.setDefaultValue(dateFormatsValues[0]);

        SimpleDateFormat sdformat = new SimpleDateFormat();
        for (int i=0; i<dateFormatsValues.length; i++) {
            String value = dateFormatsValues[i];
            if ("custom".equals(value)) {
                String renderedCustom;
                try {
                    sdformat.applyPattern(sp.getString("dateFormatCustom", dateFormatsValues[0]));
                    renderedCustom = sdformat.format(SAMPLE_DATE);
                } catch (IllegalArgumentException e) {
                    renderedCustom = res.getString(R.string.error_dateFormat);
                }
                dateFormatsEntries[i] = String.format("%s:\n%s",
                        res.getString(R.string.setting_dateFormatCustom),
                        renderedCustom);
            } else {
                sdformat.applyPattern(value);
                dateFormatsEntries[i] = sdformat.format(SAMPLE_DATE);
            }
        }

        dateFormatPref.setDefaultValue(dateFormatsValues[0]);
        dateFormatPref.setEntries(dateFormatsEntries);

        setListPreferenceSummary("dateFormat");
    }

    private void checkKey(String key){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getString(key, "").equals("")){
            sp.edit().remove(key).apply();
        }
    }

    public class DummyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

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
    }
}
