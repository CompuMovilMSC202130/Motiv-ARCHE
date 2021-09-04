package co.edu.javeriana.motivarche.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import co.edu.javeriana.motivarche.R;

public class HomeFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View homeView = inflater.inflate(R.layout.fragment_home, container, false);
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        MediaController mc= new MediaController(getActivity());
        VideoView view = (VideoView)homeView.findViewById(R.id.videoView7);
        String path = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.video_prueba;
        view.setVideoURI(Uri.parse(path));
        view.setMediaController(mc);
        alert.setMessage("Aceptar las Condiciones del servicio y la Política de privacidad de MotivARCHE")
                .setCancelable(false)
                .setPositiveButton("Acepto", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            finalize();
                            view.start();
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    }
                });
        AlertDialog title = alert.create();
        title.setTitle("Bienvenido");
        title.show();

        return homeView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}