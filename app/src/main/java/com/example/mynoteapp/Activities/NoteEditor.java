package com.example.mynoteapp.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

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
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityNoteEditorBinding = ActivityNoteEditorBinding.inflate(getLayoutInflater());
        setContentView(activityNoteEditorBinding.getRoot());
        setSupportActionBar(activityNoteEditorBinding.appBar.appToolbar, getResources().getString(R.string.edit_notes));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        validator = InputValidator.getInstance();



        storageReference = FirebaseStorage.getInstance().getReference("images/");
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
                sweetAlertDialog.setContentText("Data not Fetched Successfully");
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
                            email = note.getEmail();
                            image = note.getImage();
                            Log.e("key1",id);
                            validateInput(id,email,image);
                        }

                    }
                }
            });

    }

    private void validateInput(String id,String email,String image) {
        if(validator.validateRequired(activityNoteEditorBinding.titleLayout,activityNoteEditorBinding.titleInput) &&
                validator.validateRequired(activityNoteEditorBinding.noteLayout,activityNoteEditorBinding.noteInput)){

            // fetch the text in the autocomplete text and edittext
            String title = activityNoteEditorBinding.titleInput.getText().toString();
            String note =  activityNoteEditorBinding.noteInput.getText().toString();

            updateNote(title,note,id,email,image);
        }


    }
    private void updateNote(String title, String note, String key,String email, String image) {
        sweetAlertDialog = new SweetAlertDialog(NoteEditor.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#41c300"));
        sweetAlertDialog.setTitleText( "Loading...");
        sweetAlertDialog.show();

        //Note notes = new Note(noteId, title, note, imageId, email);
        HashMap<String, Object> noteMap = new HashMap<>();
        noteMap.put("noteID",key);
        noteMap.put("title",title);
        noteMap.put("note",note);
        noteMap.put("image", image);
        noteMap.put("email", email);
        databaseReference.child(key).updateChildren(noteMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    sweetAlertDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    sweetAlertDialog.setTitleText("Success");
                    sweetAlertDialog.setContentText("Note Updated Successfully!");
                    sweetAlertDialog.setOnDismissListener(null);
                    Intent intent = new Intent(NoteEditor.this, MainActivity.class);
                    intent.putExtra("email", email);
                    startActivity( intent);


                }
                else{
                    sweetAlertDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    sweetAlertDialog.setTitleText( "Oops");
                    sweetAlertDialog.setContentText("Failed!, Please Retry");
                    sweetAlertDialog.setOnDismissListener(null);
                }
            }
        });


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