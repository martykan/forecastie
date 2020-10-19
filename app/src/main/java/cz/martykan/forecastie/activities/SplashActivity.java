package cz.martykan.forecastie.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.notifications.WeatherNotificationService;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // we should check permission here because user can update Android version between app launches
        boolean foregroundServicesPermissionGranted =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.P
                        ||
                        ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE)
                                == PackageManager.PERMISSION_GRANTED;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs != null
                && prefs.getBoolean(getString(R.string.settings_enable_notification_key), false)
                && foregroundServicesPermissionGranted
        ) {
            WeatherNotificationService.start(this);
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
