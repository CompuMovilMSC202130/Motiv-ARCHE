package co.edu.javeriana.motivarche;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class PictureActivity extends AppCompatActivity {

    Button pickPictureButton;
    Button takePicturebutton;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        pickPictureButton = findViewById(R.id.buttonSelectImage);
        takePicturebutton = findViewById(R.id.buttonTakePicture);
        image = findViewById(R.id.imageCameraPicture);

        pickPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String justification = "Es necesario el acceso a la galería para obtener las imágenes del dispositivo";
                String titleJustification = "Solicitud permiso galería";
                Utils.requestPermission((Activity) view.getContext(),Utils.imagePickerPermission,justification,titleJustification,Utils.IMAGE_PICKER_REQUEST_CODE);
                initPickPicture();
            }
        });

        takePicturebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String justification = "Es necesario el acceso a la cámara del dispositivo para tomar la fotografía";
                String titleJustification = "Solicitud permiso cámara";
                Utils.requestPermission((Activity) view.getContext(),Utils.cameraPermission,justification,titleJustification,Utils.IMAGE_TAKE_REQUEST_CODE);
                initTakePicture();
            }
        });

    }

    private void initPickPicture(){
        if(ContextCompat.checkSelfPermission(this,Utils.imagePickerPermission) == PackageManager.PERMISSION_GRANTED){
            Intent pickImage = new Intent(Intent.ACTION_PICK);
            pickImage.setType("image/*");
            startActivityForResult(pickImage, Utils.IMAGE_PICKER_REQUEST_CODE);
        }
    }

    private void initTakePicture(){
        if(ContextCompat.checkSelfPermission(this,Utils.cameraPermission) == PackageManager.PERMISSION_GRANTED){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                startActivityForResult(takePictureIntent, Utils.IMAGE_TAKE_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                Log.e("PERMISSION_APP", e.getMessage());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String [] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case Utils.IMAGE_PICKER_REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initPickPicture();
                }else{
                    Toast.makeText(this,"No se tiene acceso a los archivos del dipositivo para seleccionar la imagen",Toast.LENGTH_SHORT).show();
                }
                break;
            case Utils.IMAGE_TAKE_REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    initTakePicture();
                }else{
                    Toast.makeText(this,"No se tiene acceso a la cámara del dispositivo para tomar la imagen",Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case Utils.IMAGE_PICKER_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    try {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        image.setImageBitmap(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Utils.IMAGE_TAKE_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    image.setImageBitmap(imageBitmap);
                }
                break;
        }
    }
}