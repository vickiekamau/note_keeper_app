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
import com.example.mynoteapp.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.content.ContentValues.TAG;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding signUpBinding;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private SweetAlertDialog sweetAlertDialog;
    private InputValidator validator;
    private  FirebaseAuth mFirebaseAuth;
    private String firebaseUserId = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        signUpBinding= signUpBinding.inflate(getLayoutInflater());
        setContentView(signUpBinding.getRoot());
        validator = InputValidator.getInstance();

        mFirebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        signUpBinding.signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, Login.class));
            }
        });
                signUpBinding.signUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        inputValidator();
                    }
                });
    }

    private void inputValidator() {
        if (validator.validateRequired(
                signUpBinding.emailLayout,
                signUpBinding.emailInput
        ) &&
                validator.validateRequired(
                        signUpBinding.userNameLayout,
                        signUpBinding.userNameInput
                )
                && validator.validateConfirmPassword(
                signUpBinding.passwordLayout,
                signUpBinding.passwordInput,
                signUpBinding.confirmPasswordInput
        ) && validator.validatePassword(
                signUpBinding.passwordLayout,
                signUpBinding.passwordInput
        )
        ) {
            signUp(signUpBinding.userNameInput.getText().toString(),
                    signUpBinding.emailInput.getText().toString(),
                    signUpBinding.passwordInput.getText().toString());
        }
    }

    private void signUp(String name, String email, String password) {
        sweetAlertDialog = new SweetAlertDialog(SignUpActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#41c300"));
        sweetAlertDialog.setTitleText( "Loading...");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull  Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            // val user = mFirebaseAuth.currentUser
                            // updateUI(user)
                            firebaseUserId = mFirebaseAuth.getCurrentUser().getUid();
                            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUserId);

                            HashMap<String , Object> userHashMap = new HashMap();
                            userHashMap.put("uid",name);
                            userHashMap.put("username",firebaseUserId);
                            userHashMap.put("email",email);
                            userHashMap.put("password",password);


                            databaseReference.updateChildren(userHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull  Task<Void> task) {
                                    sweetAlertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                    sweetAlertDialog.setTitleText("Success");
                                    sweetAlertDialog.setContentText("User Saved Successfully!");
                                    sweetAlertDialog.setOnDismissListener(dialogInterface ->
                                            startActivity(new Intent(SignUpActivity.this, MainActivity.class)));
                                           finish();
                                }
                            });

                            }
                        else {
                            // If sign in fails, display a message to the user.
                            sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                            sweetAlertDialog.setTitleText( "Oops");
                            sweetAlertDialog.setContentText("signInWithEmail:failure"+task.getException());
                            sweetAlertDialog.setOnDismissListener(null);
                            //Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }

                });

    }

    private void updateUI(FirebaseUser user) {
            if(user != null){
               signUpBinding.userNameInput.setText(user.getDisplayName());
               signUpBinding.emailInput.setText(user.getEmail());
            }else {
               signUpBinding.userNameInput.setText("");
               signUpBinding.emailInput.setText("");
            }
        }
    }


