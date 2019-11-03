package cz.martykan.forecastie.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.BarSet;
import com.db.chart.model.LineSet;
import com.db.chart.view.BarChartView;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.models.Weather;
import cz.martykan.forecastie.tasks.ParseResult;
import cz.martykan.forecastie.utils.UnitConvertor;

public class GraphActivity extends BaseActivity {

    private SharedPreferences sp;

    private ArrayList<Weather> weatherList = new ArrayList<>();

    private Paint gridPaint = new Paint();

    private String labelColor = "#000000";
    private String lineColor = "#333333";

    private boolean darkTheme = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(theme = getTheme(prefs.getString("theme", "fresh")));
        darkTheme = theme == R.style.AppTheme_NoActionBar_Dark ||
                theme == R.style.AppTheme_NoActionBar_Black ||
                theme == R.style.AppTheme_NoActionBar_Classic_Dark ||
                theme == R.style.AppTheme_NoActionBar_Classic_Black;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Toolbar toolbar = findViewById(R.id.graph_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView temperatureTextView = findViewById(R.id.graphTemperatureTextView);
        TextView rainTextView = findViewById(R.id.graphRainTextView);
        TextView pressureTextView = findViewById(R.id.graphPressureTextView);
        TextView windSpeedTextView = findViewById(R.id.graphWindSpeedTextView);

        if (darkTheme) {
            toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Dark);
            labelColor = "#FFFFFF";
            lineColor = "#FAFAFA";

            temperatureTextView.setTextColor(Color.parseColor(labelColor));
            rainTextView.setTextColor(Color.parseColor(labelColor));
            pressureTextView.setTextColor(Color.parseColor(labelColor));
            windSpeedTextView.setTextColor(Color.parseColor(labelColor));
        }

        sp = PreferenceManager.getDefaultSharedPreferences(GraphActivity.this);
        String lastLongterm = sp.getString("lastLongterm", "");

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setColor(Color.parseColor(lineColor));
        gridPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
        gridPaint.setStrokeWidth(1);

        if (parseLongTermJson(lastLongterm) == ParseResult.OK) {
            temperatureGraph();
            rainGraph();
            pressureGraph();
            windSpeedGraph();
        } else {
            Snackbar.make(findViewById(android.R.id.content), R.string.msg_err_parsing_json, Snackbar.LENGTH_LONG).show();
        }
    }

    private void temperatureGraph() {
        LineChartView lineChartView = findViewById(R.id.graph_temperature);

        float minTemp = 100000;
        float maxTemp = 0;

        // Data
        LineSet lineDataset = new LineSet();
        for (int i = 0; i < weatherList.size(); i++) {
            float temperature = UnitConvertor.convertTemperature(Float.parseFloat(weatherList.get(i).getTemperature()), sp);

            minTemp = Math.min(temperature, minTemp);
            maxTemp = Math.max(temperature, maxTemp);

            lineDataset.addPoint(getDateLabel(weatherList.get(i), i), temperature);
        }
        lineDataset.setSmooth(false);
        lineDataset.setColor(Color.parseColor("#FF5722"));
        lineDataset.setThickness(4);
        lineChartView.addData(lineDataset);

        float range = Math.abs(maxTemp - minTemp);
        int middle = Math.round(minTemp + (maxTemp - minTemp) / 2);
        int stepSize = (int) Math.ceil(range / 4);

        lineChartView.setGrid(ChartView.GridType.HORIZONTAL, 4, 1, gridPaint);
        lineChartView.setBorderSpacing(Tools.fromDpToPx(10));
        lineChartView.setAxisBorderValues(middle - 2 * stepSize, middle + 2 * stepSize);
        lineChartView.setStep(stepSize);
        lineChartView.setXAxis(false);
        lineChartView.setYAxis(false);
        lineChartView.setLabelsColor(Color.parseColor(labelColor));
        lineChartView.show();

        BarChartView barChartView = getBackgroundBarChart(R.id.graph_temperature_background, middle - 2 * stepSize, middle + 2 * stepSize, false);
        barChartView.show();

        TextView textView = findViewById(R.id.graphTemperatureTextView);
        textView.setText(String.format("%s (%s)", getString(R.string.temperature), sp.getString("unit", "°C")));
    }

    private void rainGraph() {
        BarChartView lineChartView = findViewById(R.id.graph_rain);

        float maxRain = 0;

        // Data
        BarSet dataset = new BarSet();
        for (int i = 0; i < weatherList.size(); i++) {
            float rain = UnitConvertor.convertRain(Float.parseFloat(weatherList.get(i).getRain()), sp);

            maxRain = Math.max(rain, maxRain);

            dataset.addBar(getDateLabel(weatherList.get(i), i), rain);
        }
        dataset.setColor(Color.parseColor("#2196F3"));

        int step = 1;
        if (maxRain > 6) {
            maxRain = (float) Math.ceil(maxRain / 6) * 6;
            step = (int) Math.ceil(maxRain / 6);
        } else {
            maxRain = (float) Math.ceil(maxRain);
        }

        lineChartView.addData(dataset);

        lineChartView.setGrid(ChartView.GridType.HORIZONTAL, (int) maxRain, 1, gridPaint);
        lineChartView.setBorderSpacing(Tools.fromDpToPx(10));
        lineChartView.setAxisBorderValues(0, (int) Math.ceil(maxRain));
        lineChartView.setStep(step);
        lineChartView.setXAxis(false);
        lineChartView.setYAxis(false);
        lineChartView.setLabelsColor(Color.parseColor(labelColor));

        lineChartView.show();

        BarChartView barChartView = getBackgroundBarChart(R.id.graph_rain_background, 0, (int) maxRain, true);
        barChartView.show();

        TextView textView = findViewById(R.id.graphRainTextView);
        textView.setText(String.format("%s (%s)", getString(R.string.rain), sp.getString("lengthUnit", "mm")));
    }

    private void pressureGraph() {
        LineChartView lineChartView = findViewById(R.id.graph_pressure);

        float minPressure = 100000;
        float maxPressure = 0;

        // Data
        LineSet dataset = new LineSet();
        for (int i = 0; i < weatherList.size(); i++) {
            float pressure = UnitConvertor.convertPressure(Float.parseFloat(weatherList.get(i).getPressure()), sp);

            minPressure = Math.min(pressure, minPressure);
            maxPressure = Math.max(pressure, maxPressure);

            dataset.addPoint(getDateLabel(weatherList.get(i), i), pressure);
        }
        dataset.setSmooth(false);
        dataset.setColor(Color.parseColor("#4CAF50"));
        dataset.setThickness(4);

        lineChartView.addData(dataset);

        float range = Math.abs(maxPressure - minPressure);
        int middle = Math.round(minPressure + (maxPressure - minPressure) / 2);
        int stepSize = (int) Math.ceil(range / 4);

        // Grid
        lineChartView.setGrid(ChartView.GridType.HORIZONTAL, 4, 1, gridPaint);
        lineChartView.setBorderSpacing(Tools.fromDpToPx(10));
        lineChartView.setAxisBorderValues(middle - 2 * stepSize, middle + 2 * stepSize);
        lineChartView.setStep(stepSize);
        lineChartView.setXAxis(false);
        lineChartView.setYAxis(false);
        lineChartView.setLabelsColor(Color.parseColor(labelColor));

        lineChartView.show();

        BarChartView barChartView = getBackgroundBarChart(R.id.graph_pressure_background, middle - 2 * stepSize, middle + 2 * stepSize, false);
        barChartView.show();

        TextView textView = findViewById(R.id.graphPressureTextView);
        textView.setText(String.format("%s (%s)", getString(R.string.pressure), sp.getString("pressureUnit", "hPa")));
    }

    private void windSpeedGraph() {
        LineChartView lineChartView = findViewById(R.id.graph_windspeed);
        String graphLineColor = "#efd214";

        float maxWindSpeed = 0;

        if (darkTheme) {
            graphLineColor = "#FFF600";
        }

        // Data
        LineSet dataset = new LineSet();
        for (int i = 0; i < weatherList.size(); i++) {
            float windSpeed = (float) UnitConvertor.convertWind(Float.parseFloat(weatherList.get(i).getWind()), sp);

            maxWindSpeed = Math.max(windSpeed, maxWindSpeed);

            dataset.addPoint(getDateLabel(weatherList.get(i), i), windSpeed);
        }
        dataset.setSmooth(false);
        dataset.setColor(Color.parseColor(graphLineColor));
        dataset.setThickness(4);

        int step = 1;
        if (maxWindSpeed > 6) {
            maxWindSpeed = (float) Math.ceil(maxWindSpeed / 6) * 6;
            step = (int) Math.ceil(maxWindSpeed / 6);
        } else {
            maxWindSpeed = (float) Math.ceil(maxWindSpeed);
        }

        lineChartView.addData(dataset);

        lineChartView.setGrid(ChartView.GridType.HORIZONTAL, (int) maxWindSpeed, 1, gridPaint);
        lineChartView.setBorderSpacing(Tools.fromDpToPx(10));
        lineChartView.setAxisBorderValues(0, (int) maxWindSpeed);
        lineChartView.setStep(step);
        lineChartView.setXAxis(false);
        lineChartView.setYAxis(false);
        lineChartView.setLabelsColor(Color.parseColor(labelColor));

        lineChartView.show();

        BarChartView barChartView = getBackgroundBarChart(R.id.graph_windspeed_background, 0, (int) maxWindSpeed, false);
        barChartView.show();

        TextView textView = findViewById(R.id.graphWindSpeedTextView);
        textView.setText(String.format("%s (%s)", getString(R.string.wind_speed), sp.getString("speedUnit", "m/s")));
    }

    private ParseResult parseLongTermJson(String result) {
        try {
            JSONObject reader = new JSONObject(result);

            final String code = reader.optString("cod");
            if ("404".equals(code)) {
                return ParseResult.CITY_NOT_FOUND;
            }

            JSONArray list = reader.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                Weather weather = new Weather();

                JSONObject listItem = list.getJSONObject(i);
                JSONObject main = listItem.getJSONObject("main");

                JSONObject windObj = listItem.optJSONObject("wind");
                weather.setWind(windObj.getString("speed"));

                weather.setPressure(main.getString("pressure"));
                weather.setHumidity(main.getString("humidity"));

                JSONObject rainObj = listItem.optJSONObject("rain");
                JSONObject snowObj = listItem.optJSONObject("snow");
                if (rainObj != null) {
                    weather.setRain(MainActivity.getRainString(rainObj));
                } else {
                    weather.setRain(MainActivity.getRainString(snowObj));
                }

                weather.setDate(listItem.getString("dt"));
                weather.setTemperature(main.getString("temp"));

                weatherList.add(weather);
            }
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }

    private String getDateLabel(Weather weather, int i) {
        SimpleDateFormat resultFormat = new SimpleDateFormat("E");
        resultFormat.setTimeZone(TimeZone.getDefault());
        String output = resultFormat.format(weather.getDate());

        if (i == 0 && weather.getDate().getHours() > 13) {
            return output;
        } else if (i == weatherList.size() - 1 && weather.getDate().getHours() < 11) {
            return output;
        } else if (weather.getDate().getHours() >= 11 && weather.getDate().getHours() <= 13) {
            return output;
        } else {
            return "";
        }
    }

    private BarChartView getBackgroundBarChart(int id, int min, int max, boolean includeLast) {
        boolean visible = false;
        int lastDay = -1;

        if (Math.abs(min) > Math.abs(max)) {
            max = min;
        }

        BarSet barDataset = new BarSet();
        for (int i = 0; i < weatherList.size(); i++) {
            if (weatherList.get(i).getDate().getDay() != lastDay) {
                lastDay = weatherList.get(i).getDate().getDay();
                visible = !visible;
            }

            if (i != weatherList.size() - 1 || includeLast) {
                barDataset.addBar("", visible ? max : 0);
            }
        }
        barDataset.setColor(Color.parseColor("#000000"));
        barDataset.setAlpha(0.05f);

        BarChartView barChartView = findViewById(id);
        barChartView.addData(barDataset);
        barChartView.setXAxis(false);
        barChartView.setYAxis(false);
        barChartView.setBarSpacing(0);
        barChartView.setAxisBorderValues(Math.min(0, max), Math.max(0, max));
        barChartView.setBorderSpacing(Tools.fromDpToPx(10));
        barChartView.setLabelsColor(Color.parseColor("#00ffffff"));

        return barChartView;
    }

    private int getTheme(String themePref) {
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
