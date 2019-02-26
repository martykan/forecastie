package cz.martykan.forecastie.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;

import cz.martykan.forecastie.R;

public class UI {

    public static void setNavigationBarMode(Activity context, boolean darkTheme, boolean blackTheme) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            View content = context.findViewById(android.R.id.content);
            if (!darkTheme && !blackTheme) {
                content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
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
