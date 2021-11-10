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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import co.edu.javeriana.motivarche.common.ProviderType;
import co.edu.javeriana.motivarche.ui.comentarios.ComentarioActivity;
import co.edu.javeriana.motivarche.ui.museum.MapsActivity;
import co.edu.javeriana.motivarche.ui.preguntas.PreguntaActivity;
import co.edu.javeriana.motivarche.ui.profile.ProfileActivity;
import co.edu.javeriana.motivarche.ui.scanner.AugmentedImageActivity;
import co.edu.javeriana.motivarche.ui.scanner.UploadImage;
import co.edu.javeriana.motivarche.ui.tutorial.TutorialActivity;
import de.hdodenhof.circleimageview.CircleImageView;


public class PrincipalMenu extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView mEmail;
    private CircleImageView profile_image;
    private TextView username;
    private DatabaseReference referenceUser;
    private ProviderType providerType;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.principal_menu);

        mAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.textEmail);
        username = findViewById(R.id.username);
        profile_image = findViewById(R.id.profile_image);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("images");
        createARDatabase();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            referenceUser = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            referenceUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Usuario usuario = snapshot.getValue(Usuario.class);
                    username.setText(usuario.getUsername());
                    mEmail.setText(usuario.getEmail());
                    if(usuario.getImageURL().equals("default")){
                        profile_image.setImageResource(R.mipmap.ic_launcher);
                    }else{
                        Glide.with(PrincipalMenu.this).load(usuario.getImageURL()).into(profile_image);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

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
            if(providerType == ProviderType.FACEBOOK){
                LoginManager.getInstance().logOut();
            }
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

    public void abrirChat(View view) {
        Intent intent = new Intent(PrincipalMenu.this, ChatMessageActivity.class);
        startActivity(intent);
    }

    private void createARDatabase(){
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot postSnapshot : snapshot.getChildren()){
                    UploadImage uploadImage = postSnapshot.getValue(UploadImage.class);

                    Picasso.get().load(uploadImage.getUrlImage()).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            Utils.arImages.put(uploadImage.getNameImage(),bitmap);
                            Log.i("TARGETS","agregando target "+uploadImage.getNameImage());
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            Log.i("targets-error","error target "+uploadImage.getNameImage()+"/"+e.getMessage());
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {}
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i("TARGETS error","error database "+error.getMessage());
            }
        });
    }
}