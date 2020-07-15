package com.imperial.word2mouth.learn.main.online;

import android.app.MediaRouteButton;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.shared.CourseItem;
import com.imperial.word2mouth.shared.FileHandler;
import com.imperial.word2mouth.shared.IntentNames;
import com.imperial.word2mouth.shared.LectureItem;
import com.imperial.word2mouth.shared.adapters.ArrayAdapterLectureOnline;
import com.imperial.word2mouth.teach.offline.create.audio.AudioRecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class LearnOnlineCourseSummary extends AppCompatActivity {

    //////////////////////////////////////////////////////////////////////////////////////////////

    private File metaDirectory = null;
    private File lecturesDirectory = null;

    //////////////////////////////////////////////////////////////////////////////////////////////

    // Intents
    private CourseItem course;

    //////////////////////////////////////////////////////////////////////////////////////////////

    /////// Thumbnail
    // View
    private ImageView thumbnail;
    // Model
    // Controller
    private Uri imageUri = null;


    //////// Audio Thumbnail
    // View
    private ImageButton audioPreview;
    private ImageButton audioButton;
    // Model
    private File audioFile = null;
    // Controller
    private Uri audioUri = null;
    private AudioRecorder recorder;
    private MediaPlayer player;


    ////// Title Course
    // View
    private TextView courseNameView;

    ////// List View of Lectures
    // View
    private ListView lecturesView;
    // Model
    private ArrayList<LectureItem> onlineLectures;
    private int lectureNumber = -1;

    // Controller
    private ArrayAdapterLectureOnline adapter;
    private boolean selectedLecture = false;


    /////// Delete Button
    // View
    private ImageButton deleteButton;
    // Model
    private int numberLectures = 0;
    // Controller

    /////// Learn Button
    // View
    private ImageButton learnButton;
    // Model
    private String lectureName;
    private ArrayList<LectureItem> lectures;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private LectureItem lecture;
    private ImageButton downloadButton;
    private ProgressBar progress;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    // Controller



    //////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_online_course_summary);

        // Get Intents
        getIntents();

        // Retrieve Files

        // List of Lectures
        configureListViewLectures();

        // Download Button
        configureDownloadButton();
        configureProgressBar();
        // Name
        configureCourseName();
        // Thumbnail
        configureCourseThumbnail();
        // Audio
        configureAudio();

        fetchThumbnailCourse();

    }
    private void configureProgressBar() {
        progress = findViewById(R.id.progress_download);
        progress.bringToFront();
        progress.setVisibility(View.INVISIBLE);
    }


    private void configureDownloadButton() {
        downloadButton = findViewById(R.id.download_button);

        downloadButton.setVisibility(View.INVISIBLE);

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lectureNumber > -1) {
                    DownloadProcedure downloadProcedure = new DownloadProcedure(course, lectures.get(lectureNumber), LearnOnlineCourseSummary.this, LearnOnlineCourseSummary.this);
                    downloadProcedure.download();
                    progress.setVisibility(View.VISIBLE);
                }

            }
        });
    }


    private void getIntents() {
        course= (CourseItem) getIntent().getExtras().get(IntentNames.COURSE);
    }


    private void fetchThumbnailCourse() {

        StorageReference soundRef = storage.getReference().child("content").child(course.getCourseOnlineIdentification()).child("Sound Thumbnail");

        soundRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                audioUri  = uri;
            }
        });


        StorageReference imageRef = storage.getReference().child("content").child(course.getCourseOnlineIdentification()).child("Photo Thumbnail");

        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                thumbnail.setImageURI(uri);
            }
        });

    }


    private void configureAudio() {
        audioButton = findViewById(R.id.audio_button);

        StorageReference soundRef = FirebaseStorage.getInstance().getReference().child("content").child(course.getCourseName() + course.getCourseOnlineIdentification()).child("Sound Thumbnail");

        soundRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                audioUri = uri;
            }
        });



        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioUri != null) {
                    audioButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    player = MediaPlayer.create(LearnOnlineCourseSummary.this, audioUri);
                    if (player != null) {
                        player.start();

                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                audioButton.setColorFilter(null);
                            }
                        });
                    } else {
                        Toast.makeText(LearnOnlineCourseSummary.this, "No audio File", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }

    private void configureCourseThumbnail() {
        thumbnail = findViewById(R.id.list_item_thumbnail);

        StorageReference thumbnailRef = FirebaseStorage.getInstance().getReference().child("content").child(course.getCourseName() + course.getCourseOnlineIdentification()).child("Photo Thumbnail");

        thumbnailRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                thumbnail.setImageURI(imageUri);

            }
        });
    }

    private void configureCourseName() {
        courseNameView = findViewById(R.id.list_item_text);
        courseNameView.setText(course.getCourseName());
    }


    private void configureListViewLectures() {
        lecturesView = findViewById(R.id.lecture_list_view);

        db.collection("content").whereEqualTo("type", "Lecture").whereEqualTo("courseUID", course.getCourseOnlineIdentification()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                lectures = retrieveCourses(queryDocumentSnapshots.getDocuments());
                updateListView();
            }
        });

    }



    private ArrayList<LectureItem> retrieveCourses(List<DocumentSnapshot> documents) {
        ArrayList<LectureItem> items = new ArrayList<>();

        for (DocumentSnapshot doc : documents) {
            LectureItem lecture = new LectureItem((String) doc.get("lectureName"), (String) doc.get("lectureUID"), true);
            lecture.setCourseIdentification(course.getCourseOnlineIdentification());
            lecture.setLanguage(course.getLanguage());
            lecture.setCategory(course.getCategory());
            lecture.setAuthorID((String) doc.get("authorUID"));
            lecture.setCourseName(course.getCourseName());
            items.add(lecture);
        }
        return items;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updateListView() {
        if (lectures.size() > 0) {
            adapter = new ArrayAdapterLectureOnline(LearnOnlineCourseSummary.this, R.layout.list_lectures, lectures);
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
                        if (downloadButton != null) {
                            downloadButton.setVisibility(View.INVISIBLE);
                        }
                        lectureNumber = -1;
                    } else {
                        selectedLecture = true;
                        view.setBackgroundColor(Color.LTGRAY);
                        if (downloadButton != null) {
                            downloadButton.setVisibility(View.VISIBLE);
                        }
                        lectureNumber = position;
                    }
                }
            });

        }
    }

    public void signalCompleteDownload() {
        Toast.makeText(LearnOnlineCourseSummary.this, "Download Completed", Toast.LENGTH_SHORT).show();

        progress.setVisibility(View.INVISIBLE);
        lecture = null;
        downloadButton.setVisibility(View.INVISIBLE);
    }

}