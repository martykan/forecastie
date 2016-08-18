package cz.martykan.forecastie.activities;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import cz.martykan.forecastie.R;

public class MapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String apiKey = sp.getString("apiKey", getResources().getString(R.string.apiKey));

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/map.html?lat=" + prefs.getFloat("latitude", 0) + "&lon=" + prefs.getFloat("longitude", 0) + "&appid=" + apiKey);
    }
}
