package com.example.mynoteapp.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mynoteapp.R;
import com.example.mynoteapp.Support.InputValidator;
import com.example.mynoteapp.databinding.ActivityAddNoteBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class AddNote extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 100;
    private ActivityAddNoteBinding activityAddNoteBinding;
    private Uri imageFilePath;
    private Bitmap imageToStore;
    private InputValidator validator;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private SweetAlertDialog sweetAlertDialog;
    private StorageTask uploadTask;
    private String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        validator = InputValidator.getInstance();

        activityAddNoteBinding = ActivityAddNoteBinding.inflate(getLayoutInflater());
        setContentView(activityAddNoteBinding.getRoot());
        setSupportActionBar(activityAddNoteBinding.appBar.appToolbar, getResources().getString(R.string.add_notes));
        email = getIntent().getStringExtra("email");

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("Notes");




        activityAddNoteBinding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(uploadTask !=null && uploadTask.isInProgress()){
                    sweetAlertDialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
                    sweetAlertDialog.setTitle("Info");
                    sweetAlertDialog.setContentText("Upload in Progress");
                    sweetAlertDialog.setOnDismissListener(null);
                } else{
                    validateInput(email);
                }

            }
        });
    }

    private void validateInput(String email) {
        if(validator.validateRequired(activityAddNoteBinding.titleLayout,activityAddNoteBinding.titleInput) &&
                validator.validateRequired(activityAddNoteBinding.noteLayout,activityAddNoteBinding.noteInput) &&
                activityAddNoteBinding.iDImageView.getDrawable()!=null && imageToStore != null){

            // fetch the text in the autocomplete text and edittext
            String title = activityAddNoteBinding.titleInput.getText().toString();
            String note =  activityAddNoteBinding.noteInput.getText().toString();

            SaveNote(title,note,imageToStore,imageFilePath,email);
        }


    }

    private void SaveNote(String title, String note, Bitmap imageToStore, Uri imageFilePath,String email) {
         sweetAlertDialog = new SweetAlertDialog(AddNote.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#41c300"));
        sweetAlertDialog.setTitleText( "Loading...");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();
        String imageId = "images/"
                + UUID.randomUUID().toString();
        String noteId = databaseReference.push().getKey();
        //Note notes = new Note(noteId, title, note, imageId, email);
        HashMap<String, String> noteMap = new HashMap<>();
        noteMap.put("noteID",noteId);
        noteMap.put("title",title);
        noteMap.put("note",note);
        noteMap.put("image",imageId);
        noteMap.put("email",email);
        databaseReference.child(noteId).setValue(noteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
              if(task.isSuccessful()){
                  StorageReference ref = storageReference.child(imageId);
                  ref.putFile(imageFilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                      @Override
                      public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                          Toast.makeText(AddNote.this,
                                  "Image Uploaded!!",
                                  Toast.LENGTH_SHORT)
                                  .show();

                      }
                     }).addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                          sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                          sweetAlertDialog.setTitleText( "Oops");
                          sweetAlertDialog.setContentText(e.getMessage());
                          sweetAlertDialog.setOnDismissListener(null);

                      }
                  });

                  sweetAlertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                  sweetAlertDialog.setTitleText("Success");
                  sweetAlertDialog.setContentText("Note Saved Successfully!");
                  sweetAlertDialog.setOnDismissListener(dialogInterface ->
                          startActivity(new Intent(AddNote.this, MainActivity.class)));

              }
              else{
                  sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                  sweetAlertDialog.setTitleText( "Oops");
                  sweetAlertDialog.setContentText("Property not Saved!, Please Retry");
                  sweetAlertDialog.setOnDismissListener(null);
              }
            }
        });

            }


    public void chooseImage(View view){
        try {
            Intent intent = new Intent();
            intent.setType("image/*");

            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,PICK_IMAGE_REQUEST);
        }catch (Exception E){
            Toast.makeText(this,E.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if(requestCode == PICK_IMAGE_REQUEST && data != null){
                imageFilePath = data.getData();
                imageToStore = MediaStore.Images.Media.getBitmap(getContentResolver(),imageFilePath);
                activityAddNoteBinding.iDImageView.setImageBitmap(imageToStore);
            }
        }
        catch (Exception E){
            Toast.makeText(this,E.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    protected void setSupportActionBar(@Nullable Toolbar toolbar, @Nullable String title) {
        super.setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (title != null)
                getSupportActionBar().setTitle(title);
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}