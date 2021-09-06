package co.edu.javeriana.motivarche;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;


public class PrincipalMenu extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.principal_menu);
        setSupportActionBar(findViewById(R.id.toolbar));
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        addClickOptionCloseSession();
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_scanner, R.id.nav_museum,R.id.nav_profile,R.id.nav_tutorial,R.id.nav_comentarios,R.id.nav_preguntas,R.id.nav_close_session)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    private void addClickOptionCloseSession() {
        MenuItem item = navigationView.getMenu().findItem(R.id.nav_close_session);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Intent i = new Intent(getBaseContext(),MainActivity.class);
                startActivity(i);
                return false;
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();


    }

    public void setupDatabase(Config config, Session session){
        Bitmap foxBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.fox);
        AugmentedImageDatabase aid = new AugmentedImageDatabase(session);
        aid.addImage("fox",foxBitmap);
        config.setAugmentedImageDatabase(aid);
    }
}