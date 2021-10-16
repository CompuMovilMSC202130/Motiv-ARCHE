package co.edu.javeriana.motivarche;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.javeriana.motivarche.ui.comentarios.ComentarioActivity;
import co.edu.javeriana.motivarche.ui.museum.MapsActivity;
import co.edu.javeriana.motivarche.ui.preguntas.PreguntaActivity;
import co.edu.javeriana.motivarche.ui.profile.ProfileActivity;
import co.edu.javeriana.motivarche.ui.scanner.AugmentedImageActivity;
import co.edu.javeriana.motivarche.ui.tutorial.TutorialActivity;


public class PrincipalMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.principal_menu);
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

    public void abrirComentarios(View v){
        Intent comentario = new Intent(this, ComentarioActivity.class);
        startActivity(comentario);
    }

    public void abrirPreguntas(View v){
        Intent pregunta = new Intent(this, PreguntaActivity.class);
        startActivity(pregunta);
    }

    public void abrirPerfil(View v){
        Intent perfil = new Intent(this, ProfileActivity.class);
        startActivity(perfil);
    }

    public void cerrarSesion(View v){
        Intent close = new Intent(this, MainActivity.class);
        startActivity(close);
    }







}