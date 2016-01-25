package cz.martykan.forecastie;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SettingsActivity extends PreferenceActivity {

    // Thursday 2016-01-14 16:00:00
    Date SAMPLE_DATE = new Date(1452805200000l);

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
                finish();
            }
        });

        addPreferencesFromResource(R.xml.prefs);

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
                sdformat.applyPattern(sp.getString("dateFormatCustom", dateFormatsValues[0]));
                String renderedCustom;
                try {
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
    }
}