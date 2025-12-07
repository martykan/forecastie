package cz.martykan.forecastie.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import cz.martykan.forecastie.R;

public class UI {

    public static void setNavigationBarMode(Activity context, boolean darkTheme, boolean blackTheme) {
        Window window = context.getWindow();

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false);

        // Configure status bar appearance - use light icons (false) for all themes
        WindowCompat.getInsetsController(window, window.getDecorView()).setAppearanceLightStatusBars(false);

        // Configure navigation bar appearance for Android 8.1+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            WindowCompat.getInsetsController(window, window.getDecorView()).setAppearanceLightNavigationBars(!darkTheme && !blackTheme);
        }

        // Make status bar transparent
        window.setStatusBarColor(android.graphics.Color.TRANSPARENT);

        // Make navigation bar transparent on Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarColor(android.graphics.Color.TRANSPARENT);
            window.setNavigationBarContrastEnforced(false);
        }
    }

    public static int getTheme(String themePref) {
        switch (themePref) {
            case "dark":
                return R.style.AppTheme_NoActionBar_Dark;
            case "black":
                return R.style.AppTheme_NoActionBar_Black;
            case "classic":
                return R.style.AppTheme_NoActionBar_Classic;
            case "classicdark":
                return R.style.AppTheme_NoActionBar_Classic_Dark;
            case "classicblack":
                return R.style.AppTheme_NoActionBar_Classic_Black;
            default:
                return R.style.AppTheme_NoActionBar;
        }
    }
}
