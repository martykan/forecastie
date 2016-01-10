package cz.martykan.forecastie;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        View bar = LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        root.addView(bar, 0);
        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startActivity = new Intent(getApplicationContext(), MainActivity.class);
                int pendingIntentId = 123456;
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), pendingIntentId, startActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) getSystemService(ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 200, pendingIntent);
                System.exit(0);
            }
        });

        addPreferencesFromResource(R.xml.prefs);
    }

    public void onBackPressed() {
        super.onBackPressed();

        Intent startActivity = new Intent(getApplicationContext(), MainActivity.class);
        int pendingIntentId = 123456;
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), pendingIntentId, startActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getSystemService(ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 200, pendingIntent);
        System.exit(0);
    }
}