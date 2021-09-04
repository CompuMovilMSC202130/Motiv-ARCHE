package co.edu.javeriana.motivarche.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import co.edu.javeriana.motivarche.R;


public class ProfileFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        View profileView = inflater.inflate(R.layout.fragment_profile, container, false);
        return profileView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}