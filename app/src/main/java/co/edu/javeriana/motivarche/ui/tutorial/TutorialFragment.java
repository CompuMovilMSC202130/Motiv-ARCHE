package co.edu.javeriana.motivarche.ui.tutorial;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import co.edu.javeriana.motivarche.R;

public class TutorialFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View tutorialView = inflater.inflate(R.layout.fragment_tutorial,container,false);
        return tutorialView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}