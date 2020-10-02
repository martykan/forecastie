package cz.martykan.forecastie.viewmodels;

import androidx.lifecycle.ViewModel;
import android.content.SharedPreferences;

import cz.martykan.forecastie.Constants;


public class MapViewModel extends ViewModel {
    public SharedPreferences sharedPreferences;
    public String apiKey;
    public double mapLat = Double.parseDouble(Constants.DEFAULT_LAT);
    public double mapLon = Double.parseDouble(Constants.DEFAULT_LON);
    public int mapZoom = Constants.DEFAULT_ZOOM_LEVEL;
    public int tabPosition = 0;
}
