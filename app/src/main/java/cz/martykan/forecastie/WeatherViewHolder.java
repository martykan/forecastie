package cz.martykan.forecastie;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class WeatherViewHolder extends RecyclerView.ViewHolder {
    protected TextView itemDate;
    protected TextView itemTemperature;
    protected TextView itemDescription;
    protected TextView itemyWind;
    protected TextView itemPressure;
    protected TextView itemHumidity;
    protected TextView itemIcon;
    protected View lineView;

    public WeatherViewHolder(View view) {
        super(view);
        this.itemDate = (TextView) view.findViewById(R.id.itemDate);
        this.itemTemperature = (TextView) view.findViewById(R.id.itemTemperature);
        this.itemDescription = (TextView) view.findViewById(R.id.itemDescription);
        this.itemyWind = (TextView) view.findViewById(R.id.itemWind);
        this.itemPressure = (TextView) view.findViewById(R.id.itemPressure);
        this.itemHumidity = (TextView) view.findViewById(R.id.itemHumidity);
        this.itemIcon = (TextView) view.findViewById(R.id.itemIcon);
        this.lineView = view.findViewById(R.id.lineView);
    }
}