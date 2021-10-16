package co.edu.javeriana.motivarche.ui.tutorial;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.javeriana.motivarche.R;

public class TutorialActivity extends AppCompatActivity {
    VideoView videoView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        MediaController mc= new MediaController(this);
        VideoView view = findViewById(R.id.videoView6);
        String path = "android.resource://" + this.getPackageName() + "/" + R.raw.video_prueba;
        view.setVideoURI(Uri.parse(path));
        view.setMediaController(mc);
        view.start();
    }

}