package cz.martykan.forecastie;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class WeatherRecyclerAdapter extends RecyclerView.Adapter<WeatherViewHolder> {
    private List<Weather> itemList;
    private Context context;

    public WeatherRecyclerAdapter(Context context, List<Weather> itemList) {
        this.itemList = itemList;
        this.context = context;
    }

    @Override
    public WeatherViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, null);

        WeatherViewHolder viewHolder = new WeatherViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(WeatherViewHolder customViewHolder, int i) {
        Weather weatherItem = itemList.get(i);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        String temperature = weatherItem.getTemperature();

        if (sp.getString("unit", "C").equals("C")) {
            temperature = Float.parseFloat(temperature) - 273.15 + "";
        }

        if (sp.getString("unit", "C").equals("F")) {
            temperature = (((9 * (Float.parseFloat(temperature) - 273.15)) / 5) + 32) + "";
        }

        double wind = Double.parseDouble(weatherItem.getWind());
        if (sp.getString("speedUnit", "m/s").equals("kph")) {
            wind = wind * 3.59999999712;
        }

        if (sp.getString("speedUnit", "m/s").equals("mph")) {
            wind = wind * 2.23693629205;
        }

        double pressure = Double.parseDouble(weatherItem.getPressure());
        if (sp.getString("pressureUnit", "hPa").equals("kPa")) {
            pressure = pressure / 10;
        }
        if (sp.getString("pressureUnit", "hPa").equals("mm Hg")) {
            pressure = pressure * 0.750061561303;
        }

        TimeZone tz = TimeZone.getDefault();
        String defaultDateFormat = context.getResources().getStringArray(R.array.dateFormatsValues)[0];
        String dateFormat = sp.getString("dateFormat", defaultDateFormat);
        if ("custom".equals(dateFormat)) {
            dateFormat = sp.getString("dateFormatCustom", defaultDateFormat);
        }
        String dateString;
        try {
            SimpleDateFormat resultFormat = new SimpleDateFormat(dateFormat);
            resultFormat.setTimeZone(tz);
            dateString = resultFormat.format(weatherItem.getDate());
        } catch (IllegalArgumentException e) {
            dateString = context.getResources().getString(R.string.error_dateFormat);
        }

        if (sp.getBoolean("differentiateDaysByTint", false)) {
            Date now = new Date();
            /* Unfortunately, the getColor() that takes a theme (the next commented line) is Android 6.0 only, so we have to do it manually
             * customViewHolder.itemView.setBackgroundColor(context.getResources().getColor(R.attr.colorTintedBackground, context.getTheme())); */
            int colorResourceId;
            if (weatherItem.getNumDaysFrom(now) > 1) {
                if (weatherItem.getNumDaysFrom(now) % 2 == 1) {
                    if (sp.getBoolean("darkTheme", false)) {
                        colorResourceId = R.color.darkTheme_colorTintedBackground;
                    } else {
                        colorResourceId = R.color.colorTintedBackground;
                    }
                } else {
                    /* We must explicitly set things back, because RecyclerView seems to reuse views and
                     * without restoring back the "normal" color, just about everything gets tinted if we
                     * scroll a couple of times! */
                    if (sp.getBoolean("darkTheme", false)) {
                        colorResourceId = R.color.darkTheme_colorBackground;
                    } else {
                        colorResourceId = R.color.colorBackground;
                    }
                }
                customViewHolder.itemView.setBackgroundColor(context.getResources().getColor(colorResourceId));
            }
        }

        if (new BigDecimal(temperature).setScale(0, RoundingMode.DOWN).intValue() == 0) {
            temperature = "0";
        } else {
            temperature = temperature.substring(0, temperature.indexOf(".") + 2);
        }

        customViewHolder.itemDate.setText(dateString);
        customViewHolder.itemTemperature.setText(temperature + " °" + sp.getString("unit", "C"));
        if (Float.parseFloat(weatherItem.getRain()) > 0.1) {
            customViewHolder.itemDescription.setText(weatherItem.getDescription().substring(0, 1).toUpperCase() +
                    weatherItem.getDescription().substring(1) +
                    " (" + weatherItem.getRain().substring(0, weatherItem.getRain().indexOf(".") + 2) + " mm)");
        } else {
            customViewHolder.itemDescription.setText(weatherItem.getDescription().substring(0, 1).toUpperCase() +
                    weatherItem.getDescription().substring(1));

        }
        Typeface weatherFont = Typeface.createFromAsset(context.getAssets(), "fonts/weather.ttf");
        customViewHolder.itemIcon.setTypeface(weatherFont);
        customViewHolder.itemIcon.setText(weatherItem.getIcon());
        customViewHolder.itemyWind.setText(context.getString(R.string.wind) + ": " + (wind + "").substring(0, (wind + "").indexOf(".") + 2) + " " +
                MainActivity.localize(sp, context, "speedUnit", "m/s")
                + " " + MainActivity.getWindDirectionString(sp, context, weatherItem));
        customViewHolder.itemPressure.setText(context.getString(R.string.pressure) + ": " + (pressure + "").substring(0, (pressure + "").indexOf(".") + 2) + " " +
                MainActivity.localize(sp, context, "pressureUnit", "hPa"));
        customViewHolder.itemHumidity.setText(context.getString(R.string.humidity) + ": " + weatherItem.getHumidity() + " %");
    }

    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }
}
