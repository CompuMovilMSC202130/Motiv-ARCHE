package co.edu.javeriana.motivarche;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Utils extends AppCompatActivity {

    public static final int CONTACT_REQUEST_CODE = 1;
    public static final int IMAGE_PICKER_REQUEST_CODE = 2;
    public static final int IMAGE_TAKE_REQUEST_CODE = 3;
    public static final int GPS_REQUEST_CODE = 4;
    public static final int CHECK_SETTINGS = 5;
    public static final int WRITE_EXTERNAL_REQUEST = 6;
    public static final String contactsPermission = Manifest.permission.READ_CONTACTS;
    public static final String imagePickerPermission = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String cameraPermission = Manifest.permission.CAMERA;
    public static final String gpsPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    public static void requestPermission(Activity context, String permission, String justification,String title, int idPermission){


        if(ContextCompat.checkSelfPermission(context,permission) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(context, permission)){
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(justification).setTitle(title);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ActivityCompat.requestPermissions(context, new String[]{permission}, idPermission);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }else{
                ActivityCompat.requestPermissions(context, new String[]{permission}, idPermission);
            }
        }
    }

    //metodo que permite revisar los permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String [] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case Utils.GPS_REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //checkSettingsLocation();
                    Log.i("posicion","se tiene acceso al gps desde utils");
                }else{
                    Log.i("posicion","no se tiene acceso al gps");
                    Toast.makeText(this,"No se tiene acceso al gps del dispositivo",Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    //metodo que revisa si tiene el gps activo en el dispositivo
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Utils.CHECK_SETTINGS: {
                if (resultCode == RESULT_OK) {
                    //startLocationUpdates();
                } else {
                    Toast.makeText(this, "Sin acceso a localización, hardware deshabilitado!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

}
