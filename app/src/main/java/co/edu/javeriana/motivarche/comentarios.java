package co.edu.javeriana.motivarche;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class comentarios extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View actividadcomentarios= inflater.inflate(R.layout.comentarios, container, false);
        return actividadcomentarios;
    }
}
