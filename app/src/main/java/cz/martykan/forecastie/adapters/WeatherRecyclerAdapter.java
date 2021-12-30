package cz.martykan.forecastie.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.activities.MainActivity;
import cz.martykan.forecastie.models.Weather;
import cz.martykan.forecastie.models.WeatherViewHolder;
import cz.martykan.forecastie.utils.Formatting;
import cz.martykan.forecastie.utils.TimeUtils;
import cz.martykan.forecastie.utils.UnitConvertor;

public class WeatherRecyclerAdapter extends RecyclerView.Adapter<WeatherViewHolder> {
    private List<Weather> itemList;

    public WeatherRecyclerAdapter(List<Weather> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_row, viewGroup, false);
        return new WeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder customViewHolder, int i) {
        if (i < 0 || i >= itemList.size())
            return;

        Context context = customViewHolder.itemView.getContext();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        Weather weatherItem = itemList.get(i);

        // Temperature
        float temperature = UnitConvertor.convertTemperature((float) weatherItem.getTemperature(), sp);
        if (sp.getBoolean("temperatureInteger", false)) {
            temperature = Math.round(temperature);
        }

        // Rain
        String rainString = UnitConvertor.getRainString(weatherItem.getRain(), weatherItem.getChanceOfPrecipitation(), sp);

        // Wind
        double wind = UnitConvertor.convertWind(weatherItem.getWind(), sp);

        // Pressure
        double pressure = UnitConvertor.convertPressure(weatherItem.getPressure(), sp);

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
            int color;
            if (weatherItem.getNumDaysFrom(now) > 1) {
                TypedArray ta = context.obtainStyledAttributes(new int[]{R.attr.colorTintedBackground, R.attr.colorBackground});
                if (weatherItem.getNumDaysFrom(now) % 2 == 1) {
                    color = ta.getColor(0, context.getResources().getColor(R.color.colorTintedBackground));
                } else {
                    /* We must explicitly set things back, because RecyclerView seems to reuse views and
                     * without restoring back the "normal" color, just about everything gets tinted if we
                     * scroll a couple of times! */
                    color = ta.getColor(1, context.getResources().getColor(R.color.colorBackground));
                }
                ta.recycle();
                customViewHolder.itemView.setBackgroundColor(color);
            }
        }

        customViewHolder.itemDate.setText(dateString);
        if (sp.getBoolean("displayDecimalZeroes", false)) {
            customViewHolder.itemTemperature.setText(new DecimalFormat("0.0").format(temperature) + " " + sp.getString("unit", "°C"));
        } else {
            customViewHolder.itemTemperature.setText(new DecimalFormat("#.#").format(temperature) + " " + sp.getString("unit", "°C"));
        }
        customViewHolder.itemDescription.setText(weatherItem.getDescription().substring(0, 1).toUpperCase() +
                weatherItem.getDescription().substring(1) + rainString);
        Typeface weatherFont = Typeface.createFromAsset(context.getAssets(), "fonts/weather.ttf");
        customViewHolder.itemIcon.setTypeface(weatherFont);
        customViewHolder.itemIcon.setText(this.getWeatherIcon(weatherItem, context));
        if (sp.getString("speedUnit", "m/s").equals("bft")) {
            customViewHolder.itemyWind.setText(context.getString(R.string.wind) + ": " +
                    UnitConvertor.getBeaufortName((int) wind, context) + " " + MainActivity.getWindDirectionString(sp, context, weatherItem));
        } else {
            customViewHolder.itemyWind.setText(context.getString(R.string.wind) + ": " + new DecimalFormat("0.0").format(wind) + " " +
                    MainActivity.localize(sp, context, "speedUnit", "m/s")
                    + " " + MainActivity.getWindDirectionString(sp, context, weatherItem));
        }
        customViewHolder.itemPressure.setText(context.getString(R.string.pressure) + ": " + new DecimalFormat("0.0").format(pressure) + " " +
                MainActivity.localize(sp, context, "pressureUnit", "hPa"));
        customViewHolder.itemHumidity.setText(context.getString(R.string.humidity) + ": " + weatherItem.getHumidity() + " %");
    }

    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }

    private String getWeatherIcon(Weather weather, Context context) {
        Formatting formatting = new Formatting(context);

        return formatting.getWeatherIcon(weather.getWeatherId(), TimeUtils.isDayTime(weather, Calendar.getInstance()));
    }
}
