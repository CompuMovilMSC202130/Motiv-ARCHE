package co.edu.javeriana.motivarche;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.core.Constants;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import co.edu.javeriana.motivarche.ui.scanner.UploadImage;

public class PictureActivity extends AppCompatActivity {

    Drawable myDrawable;
    Bitmap myLogo;

    Button pickPictureButton;
    Button takePicturebutton;
    Button uploadPicture;
    Button getImages;
    ImageView image;
    EditText imageName;

    private StorageReference mStorageRef;
    FirebaseDatabase database;
    private DatabaseReference mDatabaseRef;
    //a Uri object to store file path
    private Uri filePath;
    private byte [] imageBytes;

    private StorageTask mUploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        pickPictureButton = findViewById(R.id.buttonSelectImage);
        takePicturebutton = findViewById(R.id.buttonTakePicture);
        image = findViewById(R.id.imageCameraPicture);
        getImages = findViewById(R.id.buttonGetImages);
        uploadPicture = findViewById(R.id.uploadImageDatabase);
        database = FirebaseDatabase.getInstance();
        myDrawable = getDrawable(R.drawable.gallery);
        imageName = findViewById(R.id.textImageName);
        myLogo = ((BitmapDrawable) myDrawable).getBitmap();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("images");
        uploadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap bm = ((BitmapDrawable)image.getDrawable()).getBitmap();
                if(!bm.sameAs(myLogo)){
                    if(mUploadTask != null && mUploadTask.isInProgress()){
                        Toast.makeText(v.getContext(),"Se esta subiendo la imagen",Toast.LENGTH_SHORT).show();
                    }else{
                        uploadFile();
                    }

                }else{
                    Toast.makeText(v.getContext(),"Seleccione la imagen que desea subir",Toast.LENGTH_SHORT).show();
                }
            }
        });

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

        getImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              openImagesActivity();
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
                if(resultCode == RESULT_OK && data != null && data.getData() != null){
                    try {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        image.setImageBitmap(selectedImage);
                        filePath = imageUri;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Utils.IMAGE_TAKE_REQUEST_CODE:
                if(resultCode == RESULT_OK){
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    image.setImageBitmap(imageBitmap);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
                    imageBytes = outputStream.toByteArray();
                }
                break;
        }
    }


    private void uploadFile() {

        //if there is a file to upload
        if (!imageName.getText().toString().trim().equals("")) {
            if (filePath != null) {
                //displaying a progress dialog while upload is going on
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Subiendo");
                progressDialog.show();
                StorageReference riversRef = mStorageRef.child("images/" + imageName.getText().toString().trim() + ".jpg");
                mUploadTask = riversRef.putFile(filePath)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //if the upload is successfull
                                //hiding the progress dialog
                                progressDialog.dismiss();
                                if (taskSnapshot.getMetadata() != null) {
                                    if (taskSnapshot.getMetadata().getReference() != null) {
                                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                        result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String imageUrl = uri.toString();

                                                UploadImage uImage = new UploadImage(imageName.getText().toString().trim(),imageUrl);
                                                String uploadId = mDatabaseRef.push().getKey();
                                                mDatabaseRef.child(uploadId).setValue(uImage);
                                                //and displaying a success toast
                                                Toast.makeText(getApplicationContext(), "Se ha subido el archivo ", Toast.LENGTH_LONG).show();
                                                //createNewPost(imageUrl);
                                            }
                                        });
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //if the upload is not successfull
                                //hiding the progress dialog
                                progressDialog.dismiss();

                                //and displaying error message
                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                //calculating progress percentage
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                                //displaying percentage in progress dialog
                                progressDialog.setMessage("Subiendo " + ((int) progress) + "%...");
                            }
                        });
            }else if(imageBytes!=null){
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Subiendo");
                progressDialog.show();
                StorageReference riversRef = mStorageRef.child("images/" + imageName.getText().toString().trim()+ ".png");
                riversRef.putBytes(imageBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {









                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        //if the upload is successfull
                        //hiding the progress dialog
                        progressDialog.dismiss();



                        if (taskSnapshot.getMetadata() != null) {
                            if (taskSnapshot.getMetadata().getReference() != null) {
                                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUrl = uri.toString();

                                        UploadImage uImage = new UploadImage(imageName.getText().toString().trim(),imageUrl);
                                        String uploadId = mDatabaseRef.push().getKey();
                                        mDatabaseRef.child(uploadId).setValue(uImage);
                                        //and displaying a success toast
                                        Toast.makeText(getApplicationContext(), "Se ha subido el archivo ", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                //if the upload is not successfull
                                //hiding the progress dialog
                                progressDialog.dismiss();

                                //and displaying error message
                                Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                //calculating progress percentage
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                                //displaying percentage in progress dialog
                                progressDialog.setMessage("Subiendo " + ((int) progress) + "%...");
                            }
                        });
            }
            else {
                Toast.makeText(getApplicationContext(), "No hay archivo que subir", Toast.LENGTH_LONG).show();
                //you can display an error toast
            }
        }else{
            Toast.makeText(getApplicationContext(), "Debe ingresar un nombre a la imagen que quiere subir", Toast.LENGTH_SHORT).show();
        }

    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }


    private void openImagesActivity(){
        Intent intent = new Intent(this, ImagesActivity.class);
        startActivity(intent);
    }
}