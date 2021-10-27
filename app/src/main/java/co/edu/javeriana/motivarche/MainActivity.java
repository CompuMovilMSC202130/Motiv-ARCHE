package co.edu.javeriana.motivarche;

import static android.content.ContentValues.TAG;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    Button btnIniciarSesion;
    ImageButton fingerButton;
    private FirebaseAuth mAuth;
    private EditText mEmail;
    private EditText mPassword;
    SharedPreferences sharedPreferences;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.editTextTextEmailAddress);
        mPassword = findViewById(R.id.editTextTextPassword);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
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

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    private void updateUI(FirebaseUser currentUser){
        if(currentUser!=null){
            Intent intent = new Intent(getBaseContext(), PrincipalMenu.class);
            intent.putExtra("email", currentUser.getEmail());
            intent.putExtra("username",currentUser.getDisplayName());
            intent.putExtra("isLogin",true);
            startActivity(intent);
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
        if (!email.contains("@") ||
                !email.contains(".") ||
                email.length() < 5)
            return false;
        return true;
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
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(MainActivity.this, R.string.auth_failed + task.getException().toString(),
                                            Toast.LENGTH_SHORT).show();

                                    SharedPreferences.Editor editor = getSharedPreferences("data",MODE_PRIVATE).edit();
                                    editor.putString("email","");
                                    editor.putString("password","");
                                    editor.putBoolean("isLogin",false);
                                    editor.apply();

                                    updateUI(null);
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




}