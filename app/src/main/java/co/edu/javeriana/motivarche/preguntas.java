package co.edu.javeriana.motivarche;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.edu.javeriana.motivarche.R;

public class preguntas extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View actividadpreguntas= inflater.inflate(R.layout.preguntas, container, false);
        return actividadpreguntas;
    }
}
