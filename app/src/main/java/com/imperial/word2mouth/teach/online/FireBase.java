package com.imperial.word2mouth.teach.online;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class FireBase {

    private final Context context;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    public FireBase(Context context) {
        this.context = context;
    }
    public DatabaseReference createReference(String name) {

        DatabaseReference myRef = database.getReference(name);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Toast.makeText(context, dataSnapshot.getValue(String.class), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Could not Access the Database", Toast.LENGTH_SHORT).show();

            }
        });

        return myRef;
    }


}
