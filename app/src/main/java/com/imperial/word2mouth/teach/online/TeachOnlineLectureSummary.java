package com.imperial.word2mouth.teach.online;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.model.Document;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.shared.CourseItem;
import com.imperial.word2mouth.shared.LectureItem;

import java.util.concurrent.atomic.AtomicMarkableReference;

public class TeachOnlineLectureSummary extends AppCompatActivity {

    private ImageView thumbnail;
    private ImageButton sound;
    private TextView lectureName;
    private ListView slidesView;

    private TextView downloadCounter;
    private TextView likeCounter;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private Uri audioUri;
    private MediaPlayer player;
    private LectureItem lecture;
    private FirebaseStorage storage = FirebaseStorage.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_online_summary);

        getExtras();

        setUpUI();

        configureAudio();

    }

    private void getExtras() {
        lecture = (LectureItem) getIntent().getExtras().get("lecture");
    }

    private void setUpUI() {
        // Thumbnail
        thumbnail= findViewById(R.id.list_item_thumbnail);
        // Sound
        sound = findViewById(R.id.audio_button);

        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioUri != null) {
                    player = MediaPlayer.create(TeachOnlineLectureSummary.this, audioUri);
                    player.start();
                }
            }
        });


        fetchThumbnailCourse();


        // Title Course
        lectureName = findViewById(R.id.list_item_text);
        lectureName.setText(lecture.getLectureName());

        downloadCounter = findViewById(R.id.number_downloads);
//        likeCounter = findViewById(R.id.number_likes);

        // TODO LIST OF SLIDES?

        setUpData();
    }


    private void setUpData() {
        db.collection("content").document(lecture.getLectureIdentification()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                downloadCounter.setText(documentSnapshot.get("downloadCounter").toString());
            }
        });
    }

    private void fetchThumbnailCourse() {
        StorageReference soundRef = storage.getReference().child("content").child(lecture.getLectureIdentification()).child("Sound Thumbnail");

        soundRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                audioUri  = uri;
            }
        });


        StorageReference imageRef = storage.getReference().child("content").child(lecture.getLectureIdentification()).child("Photo Thumbnail");

        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                thumbnail.setImageURI(uri);
            }
        });

    }




    private void configureAudio() {
        sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioUri != null) {
                    sound.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    player = MediaPlayer.create(TeachOnlineLectureSummary.this, audioUri);
                    if (player != null) {
                        player.start();

                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                sound.setColorFilter(null);
                            }
                        });
                    } else {
                        Toast.makeText(TeachOnlineLectureSummary.this, "No audio File", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }


}