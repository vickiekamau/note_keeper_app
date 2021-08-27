package com.example.mynoteapp.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Note implements Serializable {
    @Exclude
    private String key;

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private String noteId;
   private String title;
   private String note;
   private String image;
    private String email;

    public Note(){

    }



    public String getNoteId() {
        return noteId;
    }

    public String getTitle() {
        return title;
    }

    public String getNote() {
        return note;
    }

    public String getImage() {
        return image;
    }

    public String getEmail() {
        return email;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
