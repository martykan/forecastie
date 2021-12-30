package cz.martykan.forecastie.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.activities.MainActivity;
import cz.martykan.forecastie.adapters.LocationsRecyclerAdapter;
import cz.martykan.forecastie.models.Weather;
import cz.martykan.forecastie.utils.UnitConvertor;
import cz.martykan.forecastie.weatherapi.WeatherStorage;

public class AmbiguousLocationDialogFragment extends DialogFragment implements LocationsRecyclerAdapter.ItemClickListener {

    private LocationsRecyclerAdapter recyclerAdapter;
    private SharedPreferences sharedPreferences;
    private WeatherStorage weatherStorage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_ambiguous_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Bundle bundle = getArguments();
        final Toolbar toolbar = view.findViewById(R.id.dialogToolbar);
        final RecyclerView recyclerView = view.findViewById(R.id.locationsRecyclerView);
        final LinearLayout linearLayout = view.findViewById(R.id.locationsLinearLayout);

        toolbar.setTitle(getString(R.string.location_search_heading));

        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close();
            }
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        weatherStorage = new WeatherStorage(getActivity());

        @SuppressWarnings("ConstantConditions")
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
            recyclerAdapter = new LocationsRecyclerAdapter(view.getContext().getApplicationContext(),
                    weatherArrayList, darkTheme, blackTheme);

            recyclerAdapter.setClickListener(AmbiguousLocationDialogFragment.this);

            for (int i = 0; i < cityListArray.length(); i++) {
                final JSONObject cityObject = cityListArray.getJSONObject(i);
                final JSONObject weatherObject = cityObject.getJSONArray("weather").getJSONObject(0);
                final JSONObject mainObject = cityObject.getJSONObject("main");
                final JSONObject coordObject = cityObject.getJSONObject("coord");
                final JSONObject sysObject = cityObject.getJSONObject("sys");

                final String city = cityObject.getString("name");
                final String country = sysObject.getString("country");
                final int cityId = cityObject.getInt("id");
                final String description = weatherObject.getString("description");
                final int weatherId = weatherObject.getInt("id");
                final float temperature = UnitConvertor.convertTemperature((float) mainObject.getDouble("temp"), sharedPreferences);
                final double lat = coordObject.getDouble("lat");
                final double lon = coordObject.getDouble("lon");

                Weather weather = new Weather();
                weather.setCity(city);
                weather.setCityId(cityId);
                weather.setCountry(country);
                weather.setWeatherId(weatherId);
                weather.setDescription(description.substring(0, 1).toUpperCase() + description.substring(1));
                weather.setTemperature(temperature);
                weather.setLat(lat);
                weather.setLon(lon);

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
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final Bundle bundle = new Bundle();

        weatherStorage.setCityId(weather.getCityId());
        weatherStorage.setLatitude(weather.getLat());
        weatherStorage.setLongitude(weather.getLon());

        bundle.putBoolean(MainActivity.SHOULD_REFRESH_FLAG, true);
        intent.putExtras(bundle);

        startActivity(intent);
        close();
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

    private void close() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.getSupportFragmentManager().popBackStack();
        }
    }
}
