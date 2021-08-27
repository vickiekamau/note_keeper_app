package com.example.mynoteapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mynoteapp.Activities.NoteEditor;
import com.example.mynoteapp.R;
import com.example.mynoteapp.model.DaoNote;
import com.example.mynoteapp.model.Note;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.MyViewHolder> {

    Context context;
    ArrayList<Note> list;
    private DatabaseReference databaseReference;
   //ItemClickListener clickListener;

    public NotesAdapter(Context context,ArrayList<Note> list){
        this.context = context;
        this.list = list;
        // this.clickListener = itemClickListener1;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View v = LayoutInflater.from(context).inflate(R.layout.item_note,parent,false);
       return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.MyViewHolder holder, int position) {
        Note note = list.get(position);
        holder.title.setText(note.getTitle());
        Log.d("title",note.getTitle());
        holder.note.setText(note.getNote());
        holder.txt_option.setOnClickListener((view -> {
            /**Intent intent = new Intent(view.getContext(), NoteEditor.class);
            intent.putExtra("notes",list.get(position));
            view.getContext().startActivity(intent);*/
            PopupMenu popupMenu = new PopupMenu(context,holder.txt_option);
            popupMenu.inflate(R.menu.options_menu);
            popupMenu.setOnMenuItemClickListener(item ->{
                switch (item.getItemId()){
                    case R.id.menu_edit:
                        Intent intent = new Intent(context, NoteEditor.class);
                        intent.putExtra("Edit", note);
                        context.startActivity(intent);
                        break;
                    case R.id.menu_remove:
                        DaoNote dao=new DaoNote();
                        databaseReference = FirebaseDatabase.getInstance().getReference("Notes");
                        databaseReference.child(note.getKey()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull  Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(context, "Record is removed", Toast.LENGTH_SHORT).show();
                                    notifyItemRemoved(position);
                                    list.remove(note);
                                }
                                else{
                                    Toast.makeText(context, "Failed to Remove Data", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });

                        break;

                }
                return false;
            });
         popupMenu.show();

        }));


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView title,note,txt_option;
        CardView mainLayout;

        public MyViewHolder(@NonNull  View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            note = itemView.findViewById(R.id.note);
           // mainLayout = itemView.findViewById(R.id.card_item);
            txt_option = itemView.findViewById(R.id.txt_option);

        }


    }


}
