package com.example.mynoteapp.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mynoteapp.R;
import com.example.mynoteapp.Support.InputValidator;
import com.example.mynoteapp.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.content.ContentValues.TAG;

public class Login extends AppCompatActivity {

    private ActivityLoginBinding activityLoginBinding;
    private DatabaseReference databaseReference;
    private SweetAlertDialog sweetAlertDialog;
    private InputValidator validator;
    private FirebaseAuth mFirebaseAuth;
    private String firebaseUserId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        activityLoginBinding= ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(activityLoginBinding.getRoot());
        validator = InputValidator.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        activityLoginBinding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, SignUpActivity.class));
            }
        });
        activityLoginBinding.signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputValidator();
            }
        });
    }

    private void inputValidator() {
        if (validator.validateRequired(
                activityLoginBinding.emailLayout,
                activityLoginBinding.emailInput
        ) && validator.validatePassword(
                activityLoginBinding.passwordLayout,
                activityLoginBinding.passwordInput
        )
        ) {
            signIn(
                    activityLoginBinding.emailInput.getText().toString(),
                    activityLoginBinding.passwordInput.getText().toString());
        }
    }

    private void signIn(String email, String password) {
        sweetAlertDialog = new SweetAlertDialog(Login.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#41c300"));
        sweetAlertDialog.setTitleText( "Loading...");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
        mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success");

                    Intent intent = new Intent(Login.this, MainActivity.class);
                    intent.putExtra("email",email);
                    startActivity(intent);
                    finish();
                    sweetAlertDialog.cancel();

                } else {
                    // If sign in fails, display a message to the user.
                    sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    sweetAlertDialog.setTitleText( "Oops");
                    sweetAlertDialog.setContentText("signInWithEmail:failure"+ task.getException());
                    sweetAlertDialog.setOnDismissListener(null);
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    Toast.makeText(Login.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });



        }
    }
