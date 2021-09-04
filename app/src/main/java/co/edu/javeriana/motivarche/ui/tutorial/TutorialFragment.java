package co.edu.javeriana.motivarche.ui.tutorial;

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

public class TutorialFragment extends Fragment {
    VideoView videoView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View tutorialView = inflater.inflate(R.layout.fragment_tutorial, container, false);
        MediaController mc= new MediaController(getActivity());
        VideoView view = (VideoView)tutorialView.findViewById(R.id.videoView6);
        String path = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.video_prueba;
        view.setVideoURI(Uri.parse(path));
        view.setMediaController(mc);
        view.start();
        return tutorialView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}