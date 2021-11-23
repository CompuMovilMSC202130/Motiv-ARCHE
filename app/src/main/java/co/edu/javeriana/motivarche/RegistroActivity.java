package co.edu.javeriana.motivarche;

import static android.service.controls.ControlsProviderService.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import co.edu.javeriana.motivarche.common.ProviderType;

public class RegistroActivity extends AppCompatActivity {

    private EditText mUserName;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mPasswordConfirmation;
    private Button registerButton;
    private FirebaseAuth mAuth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        mAuth = FirebaseAuth.getInstance();
        mUserName = findViewById(R.id.txtNombreUsuario);
        mEmail = findViewById(R.id.txtEmailUsuario);
        mPassword = findViewById(R.id.txtPassword);
        mPasswordConfirmation = findViewById(R.id.txtPasswordConfirm);
        registerButton = findViewById(R.id.btnRegistro);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                        registerUser(mUserName.getText().toString().trim(),mEmail.getText().toString().trim(), mPassword.getText().toString().trim(),mPasswordConfirmation.getText().toString().trim());
            }
        });

    }

    public void irAPantallaPrincipal(View v){
        Intent intent= new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void updateUI(FirebaseUser currentUser, ProviderType provider){
        if(currentUser!=null){
            mAuth.signOut();
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            mEmail.setText("");
            mPassword.setText("");
            mUserName.setText("");
            mPasswordConfirmation.setText("");
        }
    }

    private void registerUser(String username,String email, String password, String passwordConfirmation) {
        if(validateForm()) {
            boolean emailValido = isEmailValid(email);
            boolean passwordValido = checkPassword(password, passwordConfirmation);

            if (emailValido && passwordValido) {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) { //Update user Info
                                        String userId = user.getUid();
                                        reference = FirebaseDatabase.getInstance().getReference("users").child(userId);
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("id",userId);
                                        hashMap.put("username",username);
                                        hashMap.put("email",email);
                                        hashMap.put("imageURL","default");
                                        hashMap.put("provider",ProviderType.BASIC.name());

                                        reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    UserProfileChangeRequest.Builder upcrb = new UserProfileChangeRequest.Builder();
                                                    Log.i("Nombre de usuario",username);
                                                    upcrb.setDisplayName(username);
                                                    upcrb.setPhotoUri(Uri.parse("default"));//fake uri, use Firebase Storage
                                                    user.updateProfile(upcrb.build());
                                                    updateUI(user, ProviderType.BASIC);
                                                }
                                            }
                                        });


                                    }
                                }
                                if (!task.isSuccessful()) {
                                    Toast.makeText(RegistroActivity.this, R.string.auth_failed + task.getException().toString(),
                                            Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, task.getException().getMessage());
                                }
                            }
                        });
            }else if (!emailValido && !passwordValido){
                Toast.makeText(RegistroActivity.this,R.string.emailError+" y "+R.string.passwordError,Toast.LENGTH_SHORT).show();
            }else if(!emailValido){
                Toast.makeText(RegistroActivity.this,R.string.emailError,Toast.LENGTH_SHORT).show();
            }else if(!passwordValido){
                Toast.makeText(RegistroActivity.this,R.string.passwordError,Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateForm() {
        boolean valid = true;
        String username = mUserName.getText().toString().trim();
        if (TextUtils.isEmpty(username)) {
            mUserName.setError("Este campo es obligatorio");
            valid = false;
        } else {
            mUserName.setError(null);
        }
        String password = mPassword.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Este campo es obligatorio");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        String passwordConfirmation = mPasswordConfirmation.getText().toString().trim();
        if (TextUtils.isEmpty(passwordConfirmation)) {
            mPasswordConfirmation.setError("Este campo es obligatorio");
            valid = false;
        } else {
            mPasswordConfirmation.setError(null);
        }

        String emailUser = mEmail.getText().toString().trim();
        if (TextUtils.isEmpty(emailUser)) {
            mEmail.setError("Este campo es obligatorio");
            valid = false;
        } else {
            mEmail.setError(null);
        }
        return valid;
    }

    private boolean checkPassword(String password,String passwordConfirmation){
        if(password.equals(passwordConfirmation)){
            return true;
        }
        return false;
    }

    private boolean isEmailValid(String email) {
        if (!email.contains("@") ||
                !email.contains(".") ||
                email.length() < 5)
            return false;
        return true;
    }
}