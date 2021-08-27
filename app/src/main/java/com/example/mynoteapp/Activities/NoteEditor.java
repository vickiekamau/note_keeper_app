package com.example.mynoteapp.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mynoteapp.R;
import com.example.mynoteapp.Support.InputValidator;
import com.example.mynoteapp.databinding.ActivityNoteEditorBinding;
import com.example.mynoteapp.model.Note;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class NoteEditor extends AppCompatActivity {

    private ActivityNoteEditorBinding activityNoteEditorBinding;
    private String id,title,note,image,email;
    StorageReference storageReference;
    private SweetAlertDialog sweetAlertDialog;
    private Uri imageFilePath;
    private Bitmap imageToStore;
    private InputValidator validator;
    private static final int PICK_IMAGE_REQUEST = 100;
    private DatabaseReference databaseReference;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityNoteEditorBinding = ActivityNoteEditorBinding.inflate(getLayoutInflater());
        setContentView(activityNoteEditorBinding.getRoot());
        setSupportActionBar(activityNoteEditorBinding.appBar.appToolbar, getResources().getString(R.string.edit_notes));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        validator = InputValidator.getInstance();


        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("Notes");
            Note note = (Note) getIntent().getSerializableExtra("Edit");
            if(note!=null) {
                activityNoteEditorBinding.titleInput.setText(note.getTitle());
                activityNoteEditorBinding.noteInput.setText(note.getNote());
                storageReference = FirebaseStorage.getInstance().getReference().child(note.getImage());
                try{
                    sweetAlertDialog = new SweetAlertDialog(NoteEditor.this, SweetAlertDialog.PROGRESS_TYPE);
                    sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#41c300"));
                    sweetAlertDialog.setTitleText( "Loading...");
                    sweetAlertDialog.show();
                    final File localFile = File.createTempFile("image","jpg");
                    storageReference.getFile(localFile)
                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    sweetAlertDialog.cancel();
                                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                    activityNoteEditorBinding.iDImageView.setImageBitmap(bitmap);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull  Exception e) {
                            Toast.makeText(NoteEditor.this,
                                    "Failed " + e.getMessage(),
                                    Toast.LENGTH_SHORT)
                                    .show();

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            else{
                sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                sweetAlertDialog.setTitleText( "Oops");
                sweetAlertDialog.setContentText("Property not Saved!, Please Retry");
                sweetAlertDialog.setOnDismissListener(null);
            }
            
            activityNoteEditorBinding.updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(uploadTask !=null && uploadTask.isInProgress()){
                        sweetAlertDialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
                        sweetAlertDialog.setTitle("Info");
                        sweetAlertDialog.setContentText("Upload in Progress");
                        sweetAlertDialog.setOnDismissListener(null);
                    } else{
                        Note note = (Note) getIntent().getSerializableExtra("Edit");
                        if(note!=null) {
                            id = note.getKey();
                            Log.e("key1",id);
                            validateInput(id);
                        }

                    }
                }
            });

    }

    private void validateInput(String id) {
        if(validator.validateRequired(activityNoteEditorBinding.titleLayout,activityNoteEditorBinding.titleInput) &&
                validator.validateRequired(activityNoteEditorBinding.noteLayout,activityNoteEditorBinding.noteInput) &&
                activityNoteEditorBinding.iDImageView.getDrawable()!=null && imageToStore != null){

            // fetch the text in the autocomplete text and edittext
            String title = activityNoteEditorBinding.titleInput.getText().toString();
            String note =  activityNoteEditorBinding.noteInput.getText().toString();

            updateNote(title,note,imageToStore,imageFilePath,id);
        }


    }
    private void updateNote(String title, String note, Bitmap imageToStore, Uri imageFilePath, String key) {
        sweetAlertDialog = new SweetAlertDialog(NoteEditor.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#41c300"));
        sweetAlertDialog.setTitleText( "Loading...");
        sweetAlertDialog.show();
        String email = "victorkamau60@gmail.com";
        String imageId = UUID.randomUUID().toString();

        //Note notes = new Note(noteId, title, note, imageId, email);
        HashMap<String, Object> noteMap = new HashMap<>();
        noteMap.put("noteID",key);
        noteMap.put("title",title);
        noteMap.put("note",note);
        noteMap.put("image",imageId);
        noteMap.put("email",email);
        databaseReference.child(key).updateChildren(noteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    sweetAlertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    sweetAlertDialog.setTitleText("Success");
                    sweetAlertDialog.setContentText("Note Updated Successfully!");
                    sweetAlertDialog.setOnDismissListener(dialogInterface ->
                            startActivity(new Intent(NoteEditor.this, MainActivity.class)));

                }
                else{
                    sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    sweetAlertDialog.setTitleText( "Oops");
                    sweetAlertDialog.setContentText("Property not Saved!, Please Retry");
                    sweetAlertDialog.setOnDismissListener(null);
                }
            }
        });
        StorageReference ref = storageReference.child(imageId);
        ref.putFile(imageFilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(NoteEditor.this,
                        "Image Uploaded!!",
                        Toast.LENGTH_SHORT)
                        .show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                sweetAlertDialog.cancel();
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
                activityNoteEditorBinding.iDImageView.setImageBitmap(imageToStore);
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