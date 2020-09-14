package cz.martykan.forecastie.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cz.martykan.forecastie.activities.MainActivity;
import cz.martykan.forecastie.R;

public class RecyclerViewFragment extends Fragment {

    public RecyclerViewFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        return inflater.inflate(R.layout.fragment_recycler_view, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        if (view == null)
            return;

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        FragmentActivity activity = getActivity();
        Bundle bundle = this.getArguments();
        if (activity instanceof MainActivity && bundle != null && bundle.containsKey("day")) {
            MainActivity mainActivity = (MainActivity) getActivity();
            recyclerView.setAdapter(mainActivity.getAdapter(bundle.getInt("day")));
        }
    }
}
