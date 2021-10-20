package co.edu.javeriana.motivarche;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    Button btnIniciarSesion;
    private FirebaseAuth mAuth;
    private EditText mEmail;
    private EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.editTextTextEmailAddress);
        mPassword = findViewById(R.id.editTextTextPassword);

        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);


        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                        signInUser(mEmail.getText().toString().trim(), mPassword.getText().toString().trim());
            }
        });
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
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(MainActivity.this, R.string.auth_failed + task.getException().toString(),
                                            Toast.LENGTH_SHORT).show();
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




}