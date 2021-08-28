package com.example.mynoteapp.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynoteapp.R;
import com.example.mynoteapp.adapter.NotesAdapter;
import com.example.mynoteapp.databinding.ActivityMainBinding;
import com.example.mynoteapp.model.Note;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity  {

    private ActivityMainBinding activityMainBinding;
    RecyclerView recyclerView;
    NotesAdapter adapter;
    ArrayList<Note> list;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private SweetAlertDialog sweetAlertDialog;
    private  String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        setSupportActionBar(activityMainBinding.appBar.appToolbar, getResources().getString(R.string.app_name));

        activityMainBinding.recyclerview.setHasFixedSize(true);
        databaseReference = FirebaseDatabase.getInstance().getReference("Notes");
        activityMainBinding.recyclerview.setLayoutManager(new LinearLayoutManager(this));

        email = getIntent().getStringExtra("email");

        list = new ArrayList<>();
        adapter = new NotesAdapter(this,list);
        Log.d("list",list.toString());
        activityMainBinding.recyclerview.setAdapter(adapter);
        sweetAlertDialog = new SweetAlertDialog(MainActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#41c300"));
        sweetAlertDialog.setTitleText( "Loading...");
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();


        Log.e("email",email);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                            Note notes = dataSnapshot.getValue(Note.class);
                            assert notes != null;
                            notes.setKey(dataSnapshot.getKey());
                            String emailQuery = notes.getEmail();
                            Log.e("emails",emailQuery);
                            Log.e("key",dataSnapshot.getKey());
                            if(emailQuery.equals(email)){
                                list.add(notes);
                            }


                        }

                        adapter.notifyDataSetChanged();
                        sweetAlertDialog.cancel();

                    }


            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });


        activityMainBinding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, AddNote.class);
                i.putExtra("email",email);
                Log.e("email logged in", email);
                startActivity(i);

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
            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
            sweetAlertDialog.setTitle(getResources().getString(R.string.confirm));
            sweetAlertDialog.setContentText("You want to close app?");
            sweetAlertDialog.setConfirmText("Yes");
            sweetAlertDialog.setCancelText("No");
            sweetAlertDialog.setConfirmClickListener(sweetAlertDialog1 -> {
                finishAffinity();
                sweetAlertDialog1.dismissWithAnimation();
            });

            sweetAlertDialog.setCancelClickListener(SweetAlertDialog::dismissWithAnimation);
            sweetAlertDialog.show();
        }


    private void closeApp(){
        super.onBackPressed();
    }



}
