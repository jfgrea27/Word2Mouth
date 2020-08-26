package com.imperial.word2mouth.background;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.learn.main.online.DownloadProcedure;
import com.imperial.word2mouth.shared.CourseItem;
import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.shared.LectureItem;
import com.imperial.word2mouth.shared.adapters.ArrayAdapterLectureOnline;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class LearnOnlineNewCourseSummary extends AppCompatActivity {


    private ArrayList<String> newLectures;
    private ListView lecturesView;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ProgressBar progress;
    private ImageButton downloadButton;
    private TextView courseNameView;
    private CourseItem course;
    private int lectureNumber = -1;
    private ArrayList<LectureItem> lectures;
    private ImageView thumbnail;
    private Uri imageUri;
    private ImageButton audioButton;
    private Uri audioUri;
    private MediaPlayer player;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private ArrayAdapterLectureOnline adapter;
    private boolean selectedLecture = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_online_new_course_summary);



        getExtras();

        configureListViewLectures();

        configureProgressBar();
        configureDownloadButton();
        configureCourseThumbnail();
        configureCourseName();

        configureAudio();

        fetchThumbnailCourse();

    }

    private void getExtras() {
        newLectures = (ArrayList<String>) getIntent().getExtras().get("newLectures");
        course = (CourseItem) getIntent().getExtras().get("course");
    }


    private void configureListViewLectures() {
        lecturesView = findViewById(R.id.lecture_list_view);

        retrieveLectures();

    }

    private void retrieveLectures() {

        ArrayList<LectureItem> temp = new ArrayList<>();
        int counter = 0;
        int totalNumberOfCourses = newLectures.size();

        for (String lecture : newLectures) {
            counter++;
            Query query = db.collection("content").whereEqualTo("type", "Lecture").whereEqualTo("lectureUID", lecture);

            int finalCounter = counter;
            query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    LectureItem lectureItem = null;
                    for (DocumentSnapshot lecture: queryDocumentSnapshots.getDocuments()) {
                        lectureItem = new LectureItem((String) lecture.get("lectureName"), (String) lecture.get("lectureUID"), true);
                        lectureItem.setCourseIdentification(course.getCourseOnlineIdentification());
                        lectureItem.setLanguage(course.getLanguage());
                        lectureItem.setCategory(course.getCategory());
                        lectureItem.setAuthorID((String) lecture.get("authorUID"));
                        lectureItem.setCourseName(course.getCourseName());
                    }

                    temp.add(lectureItem);

                    if (finalCounter == totalNumberOfCourses) {
                         lectures = temp;
                        updateListView();
                    }

                }
            });
        }
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
                    DownloadProcedure downloadProcedure = new DownloadProcedure(course, lectures.get(lectureNumber), LearnOnlineNewCourseSummary.this, LearnOnlineNewCourseSummary.this, DownloadProcedure.NEW);
                    downloadProcedure.download();
                    progress.setVisibility(View.VISIBLE);
                }

            }
        });
    }



    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updateListView() {
        if (lectures.size() > 0) {
            adapter = new ArrayAdapterLectureOnline(LearnOnlineNewCourseSummary.this, R.layout.list_lectures, lectures);
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


    private void configureCourseName() {
        courseNameView = findViewById(R.id.list_item_text);
        courseNameView.setText(course.getCourseName());
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
                    player = MediaPlayer.create(LearnOnlineNewCourseSummary.this, audioUri);
                    if (player != null) {
                        player.start();

                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                audioButton.setColorFilter(null);
                            }
                        });
                    } else {
                        Toast.makeText(LearnOnlineNewCourseSummary.this, getString(R.string.noAudioFile), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updateFollowing(String lectureIdentification) {
        File f = new File(getExternalFilesDir(null) + DirectoryConstants.followFoder  + course.getCourseOnlineIdentification() + ".txt");

        try {
            Files.write(Paths.get(String.valueOf(f)), lectureIdentification.getBytes(), StandardOpenOption.APPEND);
        }catch (IOException e) {
            //exception handling left as an exercise for the reader
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void signalCompleteDownload(String lectureIdentification) {
        Toast.makeText(LearnOnlineNewCourseSummary.this, getString(R.string.downloadCompleted), Toast.LENGTH_SHORT).show();

        updateFollowing(lectureIdentification);
        progress.setVisibility(View.INVISIBLE);
        downloadButton.setVisibility(View.INVISIBLE);
    }



}