package cz.martykan.forecastie.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cz.martykan.forecastie.R;
import cz.martykan.forecastie.adapters.LocationsRecyclerAdapter;
import cz.martykan.forecastie.models.Weather;

public class AmbiguousLocationDialogFragment extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_ambiguous_location, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.dialogToolbar);
        RecyclerView recyclerView = view.findViewById(R.id.locationsRecyclerView);

        toolbar.setTitle("Locations");

        ArrayList<Weather> weatherArrayList = new ArrayList<>();
        LocationsRecyclerAdapter recyclerAdapter =
                new LocationsRecyclerAdapter(getActivity().getApplicationContext(), weatherArrayList);

        Weather testWeather = new Weather();
        testWeather.setCity("New York");
        testWeather.setDescription("Partly Cloudy");
        testWeather.setTemperature("48° F");
        testWeather.setIcon("\uF02E");

        weatherArrayList.add(testWeather);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recyclerAdapter);
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
}
