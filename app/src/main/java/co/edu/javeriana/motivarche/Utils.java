package co.edu.javeriana.motivarche;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utils extends AppCompatActivity {

    public static final int CONTACT_REQUEST_CODE = 1;
    public static final int IMAGE_PICKER_REQUEST_CODE = 2;
    public static final int IMAGE_TAKE_REQUEST_CODE = 3;
    public static final int GPS_REQUEST_CODE = 4;
    public static final int CHECK_SETTINGS = 5;
    public static final int WRITE_EXTERNAL_REQUEST = 6;
    public static final int FINGERPRINT_REQUEST_CODE = 7;
    public static final String contactsPermission = Manifest.permission.READ_CONTACTS;
    public static final String imagePickerPermission = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String cameraPermission = Manifest.permission.CAMERA;
    public static final String gpsPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static boolean gpsPermissionEnabled = false;
    public static Map<String,Bitmap> arImages = new HashMap<>();

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

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
