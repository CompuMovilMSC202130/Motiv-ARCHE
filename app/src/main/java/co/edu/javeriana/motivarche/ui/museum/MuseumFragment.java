package co.edu.javeriana.motivarche.ui.museum;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import co.edu.javeriana.motivarche.R;

public class MuseumFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View museumView = inflater.inflate(R.layout.fragment_museum, container, false);
        return museumView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}