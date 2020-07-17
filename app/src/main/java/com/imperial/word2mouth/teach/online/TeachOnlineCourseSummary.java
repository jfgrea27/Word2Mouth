package com.imperial.word2mouth.teach.online;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.learn.main.online.LearnOnlineCourseSummary;
import com.imperial.word2mouth.shared.CourseItem;
import com.imperial.word2mouth.shared.IntentNames;
import com.imperial.word2mouth.shared.LectureItem;
import com.imperial.word2mouth.shared.adapters.ArrayAdapterLectureOnline;
import com.imperial.word2mouth.teach.offline.TeachOfflineCourseSummary;

import java.util.ArrayList;
import java.util.List;

public class TeachOnlineCourseSummary extends AppCompatActivity {


    private ImageView thumbnail;
    private ImageButton sound;
    private TextView title;
    private ListView lecturesView;
    private ImageButton delete;
    private CourseItem courseItem;

    private ImageButton lectureSummaryButton;

    private Uri audioUri;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private MediaPlayer player;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<LectureItem> lectures = new ArrayList<>();
    private ArrayAdapterLectureOnline adapter;
    private LectureItem lecture;
    private boolean selectedLecture = false;
    private int lectureNumber = -1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_online_course_summary);

        getExtras();

        setUpUI();


        configureListLectures();
        configureDeleteButton();
        configureLectureSummaryButton();
    }

    private void configureLectureSummaryButton() {
        lectureSummaryButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        lectureSummaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lectureNumber > -1) {
                    Intent intent = new Intent(TeachOnlineCourseSummary.this, TeachOnlineLectureSummary.class);
                    intent.putExtra("lecture", lectures.get(lectureNumber));
                    startActivity(intent);
                }
            }
        });
    }

    private void configureDeleteButton() {
        delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (lectureNumber > -1) {
                    // Delete Item from Firebase Storage
                    StorageReference lectureRef = FirebaseStorage.getInstance().getReference().child("content").child(lectures.get(lectureNumber).getLectureIdentification());
                    lectureRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            deleteItemsInLecture(listResult.getItems());
                        }
                    });
                    lectureRef.delete();

                    // Delete from Firebase FireStore
                    db.collection("content").document(lectures.get(lectureNumber).getLectureIdentification()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(TeachOnlineCourseSummary.this, "Finished Deleting Lecture", Toast.LENGTH_SHORT).show();
                        }
                    });

                    // delete from the list adapter
                    adapter.remove(lectures.get(lectureNumber));
                    adapter.notifyDataSetChanged();
                    adapter.notifyDataSetInvalidated();
                    lectureNumber = -1;
                }
            }

            private void deleteItemsInLecture(List<StorageReference> items) {
                for (StorageReference ref : items) {
                    ref.delete();
                }
            }
        });
    }


    private void getExtras() {
        courseItem = (CourseItem) getIntent().getExtras().get(IntentNames.COURSE);
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
                    player = MediaPlayer.create(TeachOnlineCourseSummary.this, audioUri);
                    player.start();
                }
            }
        });

        lectureSummaryButton = findViewById(R.id.lecture_summary_button);

        fetchThumbnailCourse();


        // Title Course
        title = findViewById(R.id.list_item_text);
        title.setText(courseItem.getCourseName());

        lecturesView = findViewById(R.id.lecture_list_view);
        delete = findViewById(R.id.delete_button);
    }

    private void fetchThumbnailCourse() {

        StorageReference soundRef = storage.getReference().child("content").child(courseItem.getCourseOnlineIdentification()).child("Sound Thumbnail");

        soundRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                audioUri  = uri;
            }
        });


        StorageReference imageRef = storage.getReference().child("content").child(courseItem.getCourseOnlineIdentification()).child("Photo Thumbnail");

        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                thumbnail.setImageURI(uri);
            }
        });

    }


    private void configureListLectures() {
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            db.collection("content").whereEqualTo("type", "Lecture").whereEqualTo("courseUID", courseItem.getCourseOnlineIdentification()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    lectures = retrieveCourses(queryDocumentSnapshots.getDocuments());

                    updateListView();
                }
            });

        } else {
            Toast.makeText(TeachOnlineCourseSummary.this, "Teacher must sign-in to retrieve their courses", Toast.LENGTH_SHORT).show();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updateListView() {
        if (lectures.size() > 0) {

            adapter = new ArrayAdapterLectureOnline(TeachOnlineCourseSummary.this, R.layout.list_lectures, lectures);
            adapter.loadThumbnails();
            lecturesView.setAdapter(adapter);

            lecturesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    for (int i = 0; i < adapter.getCount(); i++) {
                        View item = lecturesView.getChildAt(i);
                        if (item != null) {
                            item.setBackgroundColor(Color.WHITE);
                        }
                    }

                    if (selectedLecture) {
                        view.setBackgroundColor(Color.WHITE);
                        selectedLecture = false;
                        if (delete != null) {
                            delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                        }
                        if (lectureSummaryButton != null) {
                            lectureSummaryButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                        }
                        lectureNumber = -1;

                    } else {
                        selectedLecture = true;
                        view.setBackgroundColor(Color.LTGRAY);
                        if (delete != null) {
                            delete.setColorFilter(null);
                        }
                        if (lectureSummaryButton != null) {
                            lectureSummaryButton.setColorFilter(null);
                        }
                        lectureNumber = position;
                    }
                }
            });

        }
    }



    private ArrayList<LectureItem> retrieveCourses(List<DocumentSnapshot> documents) {
        ArrayList<LectureItem> items = new ArrayList<>();

        for (DocumentSnapshot doc : documents) {
            LectureItem lecture = new LectureItem((String) doc.get("lectureName"), (String) doc.get("lectureUID"), true);
            items.add(lecture);
        }
        return items;
    }



}