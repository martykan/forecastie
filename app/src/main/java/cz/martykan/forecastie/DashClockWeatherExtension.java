package cz.martykan.forecastie;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Calendar;

public class DashClockWeatherExtension extends DashClockExtension {
    @Override
    protected void onUpdateData(int reason) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String result = sp.getString("lastToday", "{}");
        try {
            JSONObject reader = new JSONObject(result);

            String temperature = reader.optJSONObject("main").getString("temp").toString();
            if (sp.getString("unit", "C").equals("C")) {
                temperature = Float.parseFloat(temperature) - 273.15 + "";
            }

            if (sp.getString("unit", "C").equals("F")) {
                temperature = (((9 * (Float.parseFloat(temperature) - 273.15)) / 5) + 32) + "";
            }

            double wind = Double.parseDouble(reader.optJSONObject("wind").getString("speed").toString());
            if (sp.getString("speedUnit", "m/s").equals("kph")) {
                wind = wind * 3.59999999712;
            }

            if (sp.getString("speedUnit", "m/s").equals("mph")) {
                wind = wind * 2.23693629205;
            }

            double pressure = Double.parseDouble(reader.optJSONObject("main").getString("pressure").toString());
            if (sp.getString("pressureUnit", "hPa").equals("kPa")) {
                pressure = pressure / 10;
            }
            if (sp.getString("pressureUnit", "hPa").equals("mm Hg")) {
                pressure = pressure * 0.750061561303;
            }

            publishUpdate(new ExtensionData()
                    .visible(true)
                    .icon(R.drawable.ic_cloud_white_18dp)
                    .status(getString(R.string.dash_clock_status, temperature.substring(0, temperature.indexOf(".") + 2), localize(sp, "unit", "C")))
                    .expandedTitle(getString(R.string.dash_clock_expanded_title, temperature.substring(0, temperature.indexOf(".") + 2), localize(sp, "unit", "C"), reader.optJSONArray("weather").getJSONObject(0).getString("description")))
                    .expandedBody(getString(R.string.dash_clock_expanded_body, reader.getString("name"), reader.optJSONObject("sys").getString("country"),
                            (wind + "").substring(0, (wind + "").indexOf(".") + 2), localize(sp, "speedUnit", "m/s"),
                            (pressure + "").substring(0, (pressure + "").indexOf(".") + 2), localize(sp, "pressureUnit", "hPa"),
                            reader.optJSONObject("main").getString("humidity")))
                    .clickIntent(new Intent(this, MainActivity.class)));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String localize(SharedPreferences sp, String preferenceKey, String defaultValueKey) {
        return MainActivity.localize(sp, this, preferenceKey, defaultValueKey);
    }

}
