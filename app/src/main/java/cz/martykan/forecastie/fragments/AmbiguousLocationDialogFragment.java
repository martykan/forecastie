package cz.martykan.forecastie.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

        Bundle bundle = getArguments();

        Toolbar toolbar = view.findViewById(R.id.dialogToolbar);
        RecyclerView recyclerView = view.findViewById(R.id.locationsRecyclerView);

        toolbar.setTitle("Locations");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Formatting formatting = new Formatting(getActivity(), sharedPreferences);

        try {
            JSONArray cityListArray = new JSONArray(bundle.getString("cityList"));
            ArrayList<Weather> weatherArrayList = new ArrayList<>();
            recyclerAdapter =
                    new LocationsRecyclerAdapter(getActivity().getApplicationContext(), weatherArrayList);

            for (int i = 0; i < cityListArray.length(); i++) {
                JSONObject city = cityListArray.getJSONObject(i);
                JSONObject weatherObj = city.getJSONArray("weather").getJSONObject(0);
                JSONObject main = city.getJSONObject("main");

                final String dateMsString = city.getString("dt") + "000";
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(Long.parseLong(dateMsString));

                float temperature = UnitConvertor.convertTemperature(Float.parseFloat(main.getString("temp")), sharedPreferences);

                Weather weather = new Weather();
                weather.setCity(city.getString("name"));
                weather.setId(city.getString("id"));
                weather.setDescription(weatherObj.getString("description").substring(0, 1).toUpperCase() +
                        weatherObj.getString("description").substring(1));
                //weather.setTemperature(main.getString("temp"));
                if (sharedPreferences.getBoolean("displayDecimalZeroes", false)) {
                    weather.setTemperature(new DecimalFormat("0.0").format(temperature) + " " + sharedPreferences.getString("unit", "°C"));
                } else {
                    weather.setTemperature(new DecimalFormat("#.#").format(temperature) + " " + sharedPreferences.getString("unit", "°C"));
                }
                weather.setIcon(formatting.setWeatherIcon(Integer.parseInt(weatherObj.getString("id")), cal.get(Calendar.HOUR_OF_DAY)));

                weatherArrayList.add(weather);
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerAdapter.setClickListener(AmbiguousLocationDialogFragment.this);
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
        Weather weather = recyclerAdapter.getItem(position);

        sharedPreferences.edit().putString("cityId", weather.getId()).commit();

        Intent intent = new Intent(getActivity(), MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("shouldRefresh", true);
        intent.putExtras(bundle);

        startActivity(intent);
    }
}
