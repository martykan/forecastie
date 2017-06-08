package cz.martykan.forecastie.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

import cz.martykan.forecastie.R;

public class MapActivity extends AppCompatActivity {

    private BottomBar mBottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String apiKey = sp.getString("apiKey", getResources().getString(R.string.apiKey));

        final WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/map.html?lat=" + prefs.getFloat("latitude", 0) + "&lon=" + prefs.getFloat("longitude", 0) + "&appid=" + apiKey);

        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.setItems(R.menu.menu_map_bottom);
        mBottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                if (menuItemId == R.id.map_rain) {
                    webView.loadUrl("javascript:map.removeLayer(windLayer);map.removeLayer(tempLayer);map.addLayer(rainLayer);");
                } else if (menuItemId == R.id.map_wind) {
                    webView.loadUrl("javascript:map.removeLayer(rainLayer);map.removeLayer(tempLayer);map.addLayer(windLayer);");
                } else if (menuItemId == R.id.map_temperature) {
                    webView.loadUrl("javascript:map.removeLayer(windLayer);map.removeLayer(rainLayer);map.addLayer(tempLayer);");
                }
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mBottomBar.onSaveInstanceState(outState);
    }
}
