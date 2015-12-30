package cz.martykan.forecastie;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        SharedPreferences sp = context.getSharedPreferences("ForecastiePrefs", 0);

        String temperature = weatherItem.getTemperature();

        if (sp.getString("unit", "C").equals("C")) {
            temperature = Float.parseFloat(temperature) - 273.15 + "";
        }

        if (sp.getString("unit", "C").equals("F")) {
            temperature = (((9 * (Float.parseFloat(temperature) - 273.15)) / 5)  + 32) + "";
        }

        SimpleDateFormat resultFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm");
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        Date date = new Date();
        try {
             date = inputFormat.parse(weatherItem.getDate());
        }
        catch (ParseException e) {
            Log.e("ParseException", "I didn't believe this could ever happen.");
            e.printStackTrace();
        }
        customViewHolder.itemDate.setText(resultFormat.format(date));
        customViewHolder.itemTemperature.setText(temperature.substring(0, temperature.indexOf(".") + 2) + " °"+ sp.getString("unit", "C"));
        customViewHolder.itemDescription.setText(weatherItem.getDescription().substring(0, 1).toUpperCase() + weatherItem.getDescription().substring(1));
        Typeface weatherFont = Typeface.createFromAsset(context.getAssets(), "fonts/weather.ttf");
        customViewHolder.itemIcon.setTypeface(weatherFont);
        customViewHolder.itemIcon.setText(weatherItem.getIcon());
        customViewHolder.itemyWind.setText(context.getString(R.string.wind) + ": " + weatherItem.getWind() + " m/s");
        customViewHolder.itemPressure.setText(context.getString(R.string.pressure) + ": " + weatherItem.getPressure() + " hpa");
        customViewHolder.itemHumidity.setText(context.getString(R.string.humidity) + ": " + weatherItem.getHumidity() + " %");
    }

    @Override
    public int getItemCount() {
        return (null != itemList ? itemList.size() : 0);
    }
}
