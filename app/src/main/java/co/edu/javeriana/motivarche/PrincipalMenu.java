package co.edu.javeriana.motivarche;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import co.edu.javeriana.motivarche.ui.comentarios.ComentarioActivity;
import co.edu.javeriana.motivarche.ui.museum.MapsActivity;
import co.edu.javeriana.motivarche.ui.preguntas.PreguntaActivity;
import co.edu.javeriana.motivarche.ui.profile.ProfileActivity;
import co.edu.javeriana.motivarche.ui.scanner.AugmentedImageActivity;
import co.edu.javeriana.motivarche.ui.scanner.UploadImage;
import co.edu.javeriana.motivarche.ui.tutorial.TutorialActivity;


public class PrincipalMenu extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView mEmail;
    private DatabaseReference mDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.principal_menu);

        mAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.textEmail);

        Bundle extras = getIntent().getExtras();
        String username = extras.getString("username");
        String email = extras.getString("email");

        mEmail.setText(username+"\n"+email);

    }

    public void tomarImagen(View v){
        Intent imagen = new Intent(this,PictureActivity.class);
        startActivity(imagen);
    }

    public void abrirMapa(View v){
        Intent map = new Intent(this,MapsActivity.class);
        startActivity(map);
    }

    public void abrirEscaner(View v){
        Intent scanner = new Intent(this, AugmentedImageActivity.class);
        startActivity(scanner);
    }

    public void abrirTutorial(View v){
        Intent tutorial = new Intent(this, TutorialActivity.class);
        startActivity(tutorial);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemClicked = item.getItemId();
        if(itemClicked == R.id.menuLogOut){
            mAuth.signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if(itemClicked == R.id.menuComentarios){
            Intent intent = new Intent(this, ComentarioActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if(itemClicked == R.id.menuPreguntas){
            Intent intent = new Intent(this, PreguntaActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if(itemClicked == R.id.menuProfile){
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}