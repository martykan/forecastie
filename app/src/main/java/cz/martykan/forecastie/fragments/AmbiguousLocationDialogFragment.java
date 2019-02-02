package cz.martykan.forecastie.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.activities.MainActivity;
import cz.martykan.forecastie.adapters.LocationsRecyclerAdapter;
import cz.martykan.forecastie.models.Weather;
import cz.martykan.forecastie.utils.Formatting;
import cz.martykan.forecastie.utils.UnitConvertor;

public class AmbiguousLocationDialogFragment extends DialogFragment implements LocationsRecyclerAdapter.ItemClickListener {

    private LocationsRecyclerAdapter recyclerAdapter;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_ambiguous_location, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Formatting formatting = new Formatting(getActivity());
        final Bundle bundle = getArguments();
        final Toolbar toolbar = view.findViewById(R.id.dialogToolbar);
        final RecyclerView recyclerView = view.findViewById(R.id.locationsRecyclerView);
        final LinearLayout linearLayout = view.findViewById(R.id.locationsLinearLayout);

        toolbar.setTitle("Locations");

        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        final int theme = getTheme(sharedPreferences.getString("theme", "fresh"));
        final boolean darkTheme = theme == R.style.AppTheme_NoActionBar_Dark ||
                theme == R.style.AppTheme_NoActionBar_Classic_Dark;
        final boolean blackTheme = theme == R.style.AppTheme_NoActionBar_Black ||
                theme == R.style.AppTheme_NoActionBar_Classic_Black;

        if (darkTheme) {
            linearLayout.setBackgroundColor(Color.parseColor("#2f2f2f"));
        }

        if (blackTheme) {
            linearLayout.setBackgroundColor(Color.BLACK);
        }

        try {
            final JSONArray cityListArray = new JSONArray(bundle.getString("cityList"));
            final ArrayList<Weather> weatherArrayList = new ArrayList<>();
            recyclerAdapter =
                    new LocationsRecyclerAdapter(getActivity().getApplicationContext(), weatherArrayList, darkTheme, blackTheme);

            recyclerAdapter.setClickListener(AmbiguousLocationDialogFragment.this);

            for (int i = 0; i < cityListArray.length(); i++) {
                final JSONObject cityObject = cityListArray.getJSONObject(i);
                final JSONObject weatherObject = cityObject.getJSONArray("weather").getJSONObject(0);
                final JSONObject mainObject = cityObject.getJSONObject("main");
                final JSONObject coordObject = cityObject.getJSONObject("coord");
                final JSONObject sysObject = cityObject.getJSONObject("sys");

                final Calendar calendar = Calendar.getInstance();
                final String dateMsString = cityObject.getString("dt") + "000";
                final String city = cityObject.getString("name");
                final String country = sysObject.getString("country");
                final String cityId = cityObject.getString("id");
                final String description = weatherObject.getString("description");
                final String weatherId = weatherObject.getString("id");
                final float temperature = UnitConvertor.convertTemperature(Float.parseFloat(mainObject.getString("temp")), sharedPreferences);
                final double lat = coordObject.getDouble("lat");
                final double lon = coordObject.getDouble("lon");

                calendar.setTimeInMillis(Long.parseLong(dateMsString));

                Weather weather = new Weather();
                weather.setCity(city);
                weather.setCountry(country);
                weather.setId(cityId);
                weather.setDescription(description.substring(0, 1).toUpperCase() + description.substring(1));
                weather.setLat(lat);
                weather.setLon(lon);
                weather.setIcon(formatting.setWeatherIcon(Integer.parseInt(weatherId), calendar.get(Calendar.HOUR_OF_DAY)));

                if (sharedPreferences.getBoolean("displayDecimalZeroes", false)) {
                    weather.setTemperature(new DecimalFormat("0.0").format(temperature) + " " + sharedPreferences.getString("unit", "°C"));
                } else {
                    weather.setTemperature(new DecimalFormat("#.#").format(temperature) + " " + sharedPreferences.getString("unit", "°C"));
                }

                weatherArrayList.add(weather);
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(recyclerAdapter);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onItemClickListener(View view, int position) {
        final Weather weather = recyclerAdapter.getItem(position);
        final Intent intent = new Intent(getActivity(), MainActivity.class);
        final Bundle bundle = new Bundle();

        sharedPreferences.edit().putString("cityId", weather.getId()).commit();
        bundle.putBoolean("shouldRefresh", true);
        intent.putExtras(bundle);

        startActivity(intent);
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
