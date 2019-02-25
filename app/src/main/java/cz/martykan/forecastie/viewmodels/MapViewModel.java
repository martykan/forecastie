package cz.martykan.forecastie.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.content.SharedPreferences;


public class MapViewModel extends ViewModel {
    public SharedPreferences sharedPreferences;
    public int tabPosition;
    public double mapLat;
    public double mapLon;
}
