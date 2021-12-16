package cz.martykan.forecastie.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.appcompat.widget.Toolbar;

import com.db.chart.Tools;
import com.db.chart.model.BarSet;
import com.db.chart.model.ChartSet;
import com.db.chart.model.LineSet;
import com.db.chart.view.BarChartView;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.models.Weather;
import cz.martykan.forecastie.tasks.ParseResult;
import cz.martykan.forecastie.utils.UI;
import cz.martykan.forecastie.utils.UnitConvertor;
import cz.martykan.forecastie.weatherapi.owm.OpenWeatherMapJsonParser;

public class GraphActivity extends BaseActivity {

    private SharedPreferences sp;

    private ArrayList<Weather> weatherList = new ArrayList<>();

    private Paint gridPaint = new Paint() {{
        setStyle(Paint.Style.STROKE);
        setAntiAlias(true);
        setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
        setStrokeWidth(1);
    }};

    private SimpleDateFormat dateFormat = new SimpleDateFormat("E") {{
        setTimeZone(TimeZone.getDefault());
    }};

    private String labelColor = "#000000";
    private String lineColor = "#333333";
    private String backgroundBarColor = "#000000";

    private boolean darkTheme = false;

    private int numWeatherData = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Toolbar toolbar = findViewById(R.id.graph_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setTheme(theme = UI.getTheme(sp.getString("theme", "fresh")));
        darkTheme = theme == R.style.AppTheme_NoActionBar_Dark ||
                theme == R.style.AppTheme_NoActionBar_Black ||
                theme == R.style.AppTheme_NoActionBar_Classic_Dark ||
                theme == R.style.AppTheme_NoActionBar_Classic_Black;

        Switch graphSwitch = findViewById(R.id.graph_switch);
        graphSwitch.setChecked(sp.getString("graphsMoreDays", "off").equals("on"));
        graphSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // show graphs for whole five-day forecast
                    numWeatherData = weatherList.size();
                    sp.edit().putString("graphsMoreDays", "on").apply();
                } else {
                    // show graphs for only two days
                    numWeatherData = 2 * weatherList.size() / 5;
                    sp.edit().putString("graphsMoreDays", "off").apply();
                }

                updateGraphs();
            }
        });

        TextView temperatureTextView = findViewById(R.id.graph_temperature_textview);
        temperatureTextView.setText(String.format("%s (%s)", getString(R.string.temperature), sp.getString("unit", "°C")));

        TextView rainTextView = findViewById(R.id.graph_rain_textview);
        rainTextView.setText(String.format("%s (%s)", getString(R.string.rain), sp.getString("lengthUnit", "mm")));

        TextView windSpeedTextView = findViewById(R.id.graph_windspeed_textview);
        windSpeedTextView.setText(String.format("%s (%s)", getString(R.string.wind_speed), sp.getString("speedUnit", "m/s")));

        TextView pressureTextView = findViewById(R.id.graph_pressure_textview);
        pressureTextView.setText(String.format("%s (%s)", getString(R.string.pressure), sp.getString("pressureUnit", "hPa/mBar")));

        TextView humidityTextView = findViewById(R.id.graph_humidity_textview);
        humidityTextView.setText(String.format("%s (%s)", getString(R.string.humidity), "%"));

        if (darkTheme) {
            toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay_Dark);
            labelColor = "#FFFFFF";
            lineColor = "#FAFAFA";
            backgroundBarColor = "#FFFFFF";

            temperatureTextView.setTextColor(Color.parseColor(labelColor));
            rainTextView.setTextColor(Color.parseColor(labelColor));
            windSpeedTextView.setTextColor(Color.parseColor(labelColor));
            pressureTextView.setTextColor(Color.parseColor(labelColor));
            humidityTextView.setTextColor(Color.parseColor(labelColor));
        }

        gridPaint.setColor(Color.parseColor(lineColor));

        String lastLongterm = sp.getString("lastLongterm", "");

        if (parseLongTermJson(lastLongterm) == ParseResult.OK) {
            if (sp.getString("graphsMoreDays", "off").equals("off")) {
                numWeatherData = 2 * weatherList.size() / 5;
            } else {
                numWeatherData = weatherList.size();
            }

            updateGraphs();
        } else {
            Snackbar.make(findViewById(android.R.id.content), R.string.msg_err_parsing_json, Snackbar.LENGTH_LONG).show();
        }
    }

    private void updateGraphs() {
        temperatureGraph();
        rainGraph();
        windSpeedGraph();
        pressureGraph();
        humidityGraph();
    }

    private void temperatureGraph() {
        final LineChartView lineChartView = findViewById(R.id.graph_temperature);

        float minTemp = 1000;
        float maxTemp = -1000;

        LineSet dataset = new LineSet();
        for (int i = 0; i < numWeatherData; i++) {
            float temperature = UnitConvertor.convertTemperature((float) weatherList.get(i).getTemperature(), sp);

            minTemp = (float) Math.min(Math.floor(temperature), minTemp);
            maxTemp = (float) Math.max(Math.ceil(temperature), maxTemp);

            dataset.addPoint(getDateLabel(weatherList.get(i), i), temperature);
        }
        dataset.setSmooth(false);
        dataset.setColor(Color.parseColor("#FF5722"));
        dataset.setThickness(4);

        int middle = Math.round(minTemp + (maxTemp - minTemp) / 2);
        int stepSize = Math.max(1, (int) Math.ceil(Math.abs(maxTemp - minTemp) / 4));
        int min = middle - 2 * stepSize;
        int max = middle + 2 * stepSize;

        ArrayList<ChartSet> data = new ArrayList<>();
        data.add(dataset);
        lineChartView.addData(data);
        lineChartView.setGrid(ChartView.GridType.HORIZONTAL, 4, 1, gridPaint);
        lineChartView.setAxisBorderValues(min, max);
        lineChartView.setStep(stepSize);
        lineChartView.setLabelsColor(Color.parseColor(labelColor));
        lineChartView.setXAxis(false);
        lineChartView.setYAxis(false);
        lineChartView.setBorderSpacing(Tools.fromDpToPx(10));
        lineChartView.show();

        BarChartView backgroundChartView = getBackgroundBarChart(R.id.graph_temperature_background, min, max, false);
        backgroundChartView.show();
    }

    private void rainGraph() {
        BarChartView barChartView = findViewById(R.id.graph_rain);

        float maxRain = 1;

        BarSet dataset = new BarSet();
        for (int i = 0; i < numWeatherData; i++) {
            float rain = UnitConvertor.convertRain((float) weatherList.get(i).getRain(), sp);

            maxRain = Math.max(rain, maxRain);

            dataset.addBar(getDateLabel(weatherList.get(i), i), rain);
        }
        dataset.setColor(Color.parseColor("#2196F3"));

        int stepSize = 1;
        if (maxRain > 6) {
            maxRain = (float) Math.ceil(maxRain / 6) * 6;
            stepSize = (int) Math.ceil(maxRain / 6);
        } else {
            maxRain = (float) Math.ceil(maxRain);
        }
        int max = (int) maxRain;

        ArrayList<ChartSet> data = new ArrayList<>();
        data.add(dataset);
        barChartView.addData(data);
        barChartView.setGrid(ChartView.GridType.HORIZONTAL, max / stepSize, 1, gridPaint);
        barChartView.setAxisBorderValues(0, (int) Math.ceil(maxRain));
        barChartView.setStep(stepSize);
        barChartView.setLabelsColor(Color.parseColor(labelColor));
        barChartView.setXAxis(false);
        barChartView.setYAxis(false);
        barChartView.setBorderSpacing(Tools.fromDpToPx(10));
        barChartView.show();

        BarChartView backgroundChartView = getBackgroundBarChart(R.id.graph_rain_background, 0, max, true);
        backgroundChartView.show();
    }

    private void windSpeedGraph() {
        LineChartView lineChartView = findViewById(R.id.graph_windspeed);
        String graphLineColor = "#efd214";

        float maxWindSpeed = 1;

        if (darkTheme) {
            graphLineColor = "#FFF600";
        }

        LineSet dataset = new LineSet();
        for (int i = 0; i < numWeatherData; i++) {
            float windSpeed = (float) UnitConvertor.convertWind(weatherList.get(i).getWind(), sp);

            maxWindSpeed = Math.max(windSpeed, maxWindSpeed);

            dataset.addPoint(getDateLabel(weatherList.get(i), i), windSpeed);
        }
        dataset.setSmooth(false);
        dataset.setColor(Color.parseColor(graphLineColor));
        dataset.setThickness(4);

        int stepSize = 1;
        if (maxWindSpeed > 6) {
            maxWindSpeed = (float) Math.ceil(maxWindSpeed / 6) * 6;
            stepSize = (int) Math.ceil(maxWindSpeed / 6);
        } else {
            maxWindSpeed = (float) Math.ceil(maxWindSpeed);
        }
        int max = (int) maxWindSpeed;

        ArrayList<ChartSet> data = new ArrayList<>();
        data.add(dataset);
        lineChartView.addData(data);
        lineChartView.setGrid(ChartView.GridType.HORIZONTAL, max / stepSize, 1, gridPaint);
        lineChartView.setAxisBorderValues(0, (int) maxWindSpeed);
        lineChartView.setStep(stepSize);
        lineChartView.setLabelsColor(Color.parseColor(labelColor));
        lineChartView.setXAxis(false);
        lineChartView.setYAxis(false);
        lineChartView.setBorderSpacing(Tools.fromDpToPx(10));
        lineChartView.show();

        BarChartView barChartView = getBackgroundBarChart(R.id.graph_windspeed_background, 0, max, false);
        barChartView.show();
    }

    private void pressureGraph() {
        LineChartView lineChartView = findViewById(R.id.graph_pressure);

        float minPressure = 100000;
        float maxPressure = 0;

        LineSet dataset = new LineSet();
        for (int i = 0; i < numWeatherData; i++) {
            float pressure = UnitConvertor.convertPressure(weatherList.get(i).getPressure(), sp);

            minPressure = (float) Math.min(Math.floor(pressure), minPressure);
            maxPressure = (float) Math.max(Math.ceil(pressure), maxPressure);

            dataset.addPoint(getDateLabel(weatherList.get(i), i), pressure);
        }
        dataset.setSmooth(false);
        dataset.setColor(Color.parseColor("#4CAF50"));
        dataset.setThickness(4);

        int middle = Math.round(minPressure + (maxPressure - minPressure) / 2);
        int stepSize = Math.max(1, (int) Math.ceil(Math.abs(maxPressure - minPressure) / 4));
        int min = middle - 2 * stepSize;
        int max = middle + 2 * stepSize;
        int rows = 4;
        if (Math.ceil(maxPressure) - Math.floor(minPressure) <= 3) {
            min = (int) Math.floor(minPressure);
            max = Math.max(min + 1, (int) Math.ceil(maxPressure));
            rows = max - min;
        }

        ArrayList<ChartSet> data = new ArrayList<>();
        data.add(dataset);
        lineChartView.addData(data);
        lineChartView.setGrid(ChartView.GridType.HORIZONTAL, rows, 1, gridPaint);
        lineChartView.setAxisBorderValues(min, max);
        lineChartView.setStep(stepSize);
        lineChartView.setLabelsColor(Color.parseColor(labelColor));
        lineChartView.setXAxis(false);
        lineChartView.setYAxis(false);
        lineChartView.setBorderSpacing(Tools.fromDpToPx(10));
        lineChartView.show();

        BarChartView barChartView = getBackgroundBarChart(R.id.graph_pressure_background, min, max, false);
        barChartView.show();
    }

    private void humidityGraph() {
        LineChartView lineChartView = findViewById(R.id.graph_humidity);

        float minHumidity = 100000;
        float maxHumidity = 0;

        LineSet dataset = new LineSet();
        for (int i = 0; i < numWeatherData; i++) {
            float humidity = weatherList.get(i).getHumidity();

            minHumidity = Math.min(humidity, minHumidity);
            maxHumidity = Math.max(humidity, maxHumidity);

            dataset.addPoint(getDateLabel(weatherList.get(i), i), humidity);
        }
        dataset.setSmooth(false);
        dataset.setColor(Color.parseColor("#2196F3"));
        dataset.setThickness(4);

        int min = (int) minHumidity / 10 * 10;
        int max = (int) Math.ceil(maxHumidity / 10) * 10;
        if (min == max) {
            max = Math.min(max + 10, 100);
            min = Math.max(min - 10, 0);
        }
        int stepSize = (max - min == 100) ? 20 : 10;

        ArrayList<ChartSet> data = new ArrayList<>();
        data.add(dataset);
        lineChartView.addData(data);
        lineChartView.setGrid(ChartView.GridType.HORIZONTAL, (max - min) / stepSize, 1, gridPaint);
        lineChartView.setAxisBorderValues(min, max);
        lineChartView.setStep(stepSize);
        lineChartView.setLabelsColor(Color.parseColor(labelColor));
        lineChartView.setXAxis(false);
        lineChartView.setYAxis(false);
        lineChartView.setBorderSpacing(Tools.fromDpToPx(10));
        lineChartView.show();

        BarChartView barChartView = getBackgroundBarChart(R.id.graph_humidity_background, min, max, false);
        barChartView.show();
    }

    public ParseResult parseLongTermJson(String result) {
        try {
            List<Weather> parsedWeatherList = OpenWeatherMapJsonParser.convertJsonToWeatherList(result);
            weatherList.addAll(parsedWeatherList);
        } catch (JSONException e) {
            Log.e("JSONException Data", result);
            e.printStackTrace();
            return ParseResult.JSON_EXCEPTION;
        }

        return ParseResult.OK;
    }

    /**
     * Returns a label for the dates, only one per day preferably at noon.
     * @param weather weather entity
     * @param i number of weather in long term forecast
     * @return label (either short form of day in week or empty string)
     */
    private String getDateLabel(Weather weather, int i) {
        String output = dateFormat.format(weather.getDate());

        Calendar cal = Calendar.getInstance();
        cal.setTime(weather.getDate());
        int weatherHour = cal.get(Calendar.HOUR_OF_DAY);

        // label for first day if it starts after 13:00
        if (i == 0 && weatherHour > 13) {
            return output;
        }
        // label for the last day if it ends before 11:00
        else if (i == numWeatherData - 1 && weatherHour < 11) {
            return output;
        }
        // label in the middle of the day at 11:00 / 12:00 / 13:00 for all other days
        else if (weatherHour >= 11 && weatherHour <= 13) {
            return output;
        }
        // normal case: no date label
        else {
            return "";
        }
    }

    /**
     * Returns a background chart with alternating vertical bars for each day.
     * @param id BarChartView resource id
     * @param min foreground chart min label
     * @param max foreground chart max label
     * @param includeLast true for foreground bar charts, false for foreground line charts
     * @return background bar chart
     */
    private BarChartView getBackgroundBarChart(@IdRes int id, int min, int max, boolean includeLast) {
        boolean visible = false;
        int lastHour = 25;

        // get label with biggest visual length
        if (getLengthAsString(min) > getLengthAsString(max)) {
            max = min;
        }

        BarSet barDataset = new BarSet();
        for (int i = 0; i < numWeatherData; i++) {
            if (i != numWeatherData - 1 || includeLast) {
                for (int j = 0; j < 3; j++) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(weatherList.get(i).getDate());
                    int hour = cal.get(Calendar.HOUR_OF_DAY);

                    // 23:00 to 0:00 new day
                    if (hour < lastHour) {
                        visible = !visible;
                    }

                    barDataset.addBar("", visible ? max : 0);
                    lastHour = hour;
                }
            }
        }
        barDataset.setColor(Color.parseColor(backgroundBarColor));
        barDataset.setAlpha(0.075f);

        ArrayList<ChartSet> data = new ArrayList<>();
        data.add(barDataset);
        BarChartView barChartView = findViewById(id);
        barChartView.addData(data);
        barChartView.setBarSpacing(0); // visually join bars into one bar per day
        barChartView.setAxisBorderValues(Math.min(0, max), Math.max(0, max));
        barChartView.setLabelsColor(Color.parseColor("#00ffffff")); // fully transparent (= invisible) labels
        barChartView.setXAxis(false);
        barChartView.setYAxis(false);
        barChartView.setBorderSpacing(Tools.fromDpToPx(10));

        return barChartView;
    }

    /**
     * Returns a comparable abstract length/width an integer number uses as a chart label (works best for fonts with monospaced digits).
     * @param i number
     * @return length
     */
    private int getLengthAsString(int i) {
        char[] array = String.valueOf(i).toCharArray();
        int sum = 0;
        for (char c : array) {
            sum += (c == '-') ? 1 : 2; // minus is smaller than digits
        }
        return sum;
    }

}
