package co.edu.javeriana.motivarche;

import static android.content.ContentValues.TAG;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.auth.TwitterAuthCredential;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Executor;

import co.edu.javeriana.motivarche.common.ProviderType;
import co.edu.javeriana.motivarche.ui.scanner.UploadImage;

public class MainActivity extends AppCompatActivity {


    private DatabaseReference mDatabaseRef;
    private final int GOOGLE_SIGN_IN=100;

    Button btnIniciarSesion;
    ImageButton fingerButton;
    ImageButton btnFacebook;
    ImageButton btnGoogle;
    ImageButton btnTwitter;
    private FirebaseAuth mAuth;
    private EditText mEmail;
    private EditText mPassword;
    SharedPreferences sharedPreferences;
    private DatabaseReference referenceUser;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    private CallbackManager callbackManager;

    private OAuthProvider.Builder twitterProvider = OAuthProvider.newBuilder("twitter.com");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(this);


        AppEventsLogger.activateApp(this.getApplication());
        callbackManager= CallbackManager.Factory.create();
        mEmail = findViewById(R.id.editTextTextEmailAddress);
        mPassword = findViewById(R.id.editTextTextPassword);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnFacebook=findViewById(R.id.btnFacebook);
        btnGoogle=findViewById(R.id.btnGoogle);
        btnTwitter=findViewById(R.id.btnTwitter);
        fingerButton = findViewById(R.id.fingerButton);
        sharedPreferences = getSharedPreferences("data",MODE_PRIVATE);

        fingerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                biometricPrompt.authenticate(promptInfo);
                String email = sharedPreferences.getString("email","");
                String password = sharedPreferences.getString("password","");
                mEmail.setText(email);
                mPassword.setText(password);
            }
        });

        boolean isLogin = sharedPreferences.getBoolean("isLogin",false);
        if(isLogin){
            fingerButton.setVisibility(View.VISIBLE);
        }

        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    signInUser(mEmail.getText().toString().trim(), mPassword.getText().toString().trim());
            }
        });

        btnFacebook.setOnClickListener(v -> signInWithFacebook());

        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        btnTwitter.setOnClickListener(v -> signInWithTwitter());


        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                mEmail.setText("");
                mPassword.setText("");
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                String email = sharedPreferences.getString("email","");
                String password = sharedPreferences.getString("password","");
                mEmail.setText(email);
                mPassword.setText(password);
                signInUser(email.trim(), password.trim());
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                mEmail.setText("");
                mPassword.setText("");
                Toast.makeText(getApplicationContext(), "No se autentico correctamente",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Autenticación por huella dactilar")
                .setSubtitle("Inicie sesión utilizando la huella dactilar")
                .setNegativeButtonText("Cancelar")
                .build();

        checkSettingsFingerprint();

    }


    private void showAlert(){
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage("Se produjo un error autenticando al usuario");
        builder.setPositiveButton("Aceptar", null);
        AlertDialog dialog= builder.create();
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        ProviderType providerType = null;

        if (currentUser != null) {
            switch(currentUser.getProviderId()){
                case "google.com":
                    providerType= ProviderType.GOOGLE;
                    break;
                case "facebook.com":
                    providerType= ProviderType.FACEBOOK;
                    break;
                case "twitter.com":
                    providerType= ProviderType.TWITTER;
                    break;
                default:
                    providerType= ProviderType.BASIC;
                    break;
            }
        }
        updateUI(currentUser, providerType);
    }

    private void updateUI(FirebaseUser currentUser, ProviderType provider){
        if(currentUser!=null){
            referenceUser = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

            referenceUser.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.exists()){
                        HashMap<String,String> hashMap = new HashMap<>();
                        hashMap.put("id",currentUser.getUid());
                        hashMap.put("username",currentUser.getDisplayName());
                        hashMap.put("email",currentUser.getEmail());
                        hashMap.put("imageURL",currentUser.getPhotoUrl().toString());
                        hashMap.put("provider",provider.name());
                        referenceUser.setValue(hashMap);
                        Intent intent = new Intent(getBaseContext(), PrincipalMenu.class);
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(getBaseContext(), PrincipalMenu.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            /*
            if(referenceUser == null){
                String userId = currentUser.getUid();
                reference = FirebaseDatabase.getInstance().getReference("users").child(userId);
                HashMap<String,String> hashMap = new HashMap<>();
                hashMap.put("id",userId);
                hashMap.put("username",currentUser.getDisplayName());
                hashMap.put("email",currentUser.getEmail());
                hashMap.put("imageURL",currentUser.getPhotoUrl().toString());
                hashMap.put("provider", provider.name());
                reference.setValue(hashMap);
            }


            */

        } else {
            mEmail.setText("");
            mPassword.setText("");
        }
    }

    private boolean validateForm() {
        boolean valid = true;
        String email = mEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Este campo es obligatorio");
            valid = false;
        } else {
            mEmail.setError(null);
        }
        String password = mPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Este campo es obligatorio");
            valid = false;
        } else {
            mPassword.setError(null);
        }
        return valid;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") &&
                email.contains(".") &&
                email.length() >= 5;
    }

    private void signInWithGoogle(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        GoogleSignInClient gsic= GoogleSignIn.getClient(this, gso);
        gsic.signOut();
        startActivityForResult(gsic.getSignInIntent(), GOOGLE_SIGN_IN);

    }

    private void signInWithFacebook(){
        LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Collections.singletonList("email"));
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        if(loginResult!=null){
                            AccessToken token= loginResult.getAccessToken();
                            AuthCredential credencial= FacebookAuthProvider.getCredential(token.getToken());

                            FirebaseAuth.getInstance().signInWithCredential(credencial).addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                                    editor.putString("token", token.getToken());
                                    editor.putBoolean("isLogin", true);
                                    editor.apply();

                                    Log.d(TAG, "signInWithFacebook:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user, ProviderType.FACEBOOK);
                                }else{
                                    SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                                    editor.putString("token", "");
                                    editor.putBoolean("isLogin", false);
                                    editor.apply();

                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(MainActivity.this, R.string.auth_failed + Objects.requireNonNull(task.getException()).toString(),
                                            Toast.LENGTH_SHORT).show();
                                    updateUI(null, null);
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(@NonNull FacebookException e) {
                        showAlert();
                    }
                });
    }

    private void signInWithTwitter() {
        twitterProvider.addCustomParameter("lang", "es");
        Task<AuthResult> pendingResultTask = mAuth.getPendingAuthResult();
        if (pendingResultTask != null) {
            // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {

                                    updateUI(authResult.getUser(), ProviderType.TWITTER);
                                   // startActivity(new Intent(MainActivity.this, PrincipalMenu.class));
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("enttro aca y falo");
                                    // Handle failure.
                                }
                            });
        } else {
            mAuth
                    .startActivityForSignInWithProvider(/* activity= */ this, twitterProvider.build())
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    updateUI(authResult.getUser(), ProviderType.TWITTER);
                                   //startActivity(new Intent(MainActivity.this, PrincipalMenu.class));
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("enttro aca y falo2");
                                    // Handle failure.
                                }
                            });
        }
    }

    private void signInUser(String email, String password) {
        if (validateForm()) {
            if(isEmailValid(email)) {

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI

                                    SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                                    editor.putString("email",email);
                                    editor.putString("password",password);
                                    editor.putBoolean("isLogin",true);
                                    editor.apply();

                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user, ProviderType.BASIC);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(MainActivity.this, R.string.auth_failed + Objects.requireNonNull(task.getException()).toString(),
                                            Toast.LENGTH_SHORT).show();

                                    SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                                    editor.putString("email","");
                                    editor.putString("password","");
                                    editor.putBoolean("isLogin",false);
                                    editor.apply();

                                    updateUI(null, null);
                                }
                            }
                        });
            }else{
                Toast.makeText(MainActivity.this, R.string.emailError, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void irAlRegistro(View v){
        Intent intent= new Intent(getApplicationContext(), RegistroActivity.class);
        startActivity(intent);
    }

    public void checkSettingsFingerprint(){
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("MY_APP_TAG", "App can authenticate using biometrics.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "El sensor de huella dactilar no existe", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "El sensor de huella dactilar no esta disponible", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // Prompts the user to create credentials that your app accepts.
                final Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                startActivityForResult(enrollIntent, Utils.FINGERPRINT_REQUEST_CODE);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GOOGLE_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account= task.getResult(ApiException.class);
                if(account!=null){
                    AuthCredential authCredential= GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    FirebaseAuth.getInstance().signInWithCredential(authCredential).addOnCompleteListener(this, task1 -> {
                        if(task1.isSuccessful()){
                            SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                            editor.putString("email",account.getEmail());
                            editor.putString("token",account.getIdToken());
                            editor.putBoolean("isLogin",true);
                            editor.apply();

                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user, ProviderType.GOOGLE);
                        }else{
                            Log.w(TAG, "signInWithGoogle:failure", task1.getException());
                            Toast.makeText(MainActivity.this, R.string.auth_failed + Objects.requireNonNull(task1.getException()).toString(),
                                    Toast.LENGTH_SHORT).show();

                            SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                            editor.putString("email","");
                            editor.putString("token","");
                            editor.putBoolean("isLogin",false);
                            editor.apply();

                            updateUI(null, null);
                        }
                    });
                }
            }catch (ApiException ae){
                Log.w(TAG, "Falló el login de google", ae);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }



}