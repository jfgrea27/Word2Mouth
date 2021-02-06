package com.imperial.word2mouth.previous.teach.online;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
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
import com.imperial.word2mouth.previous.shared.CourseItem;
import com.imperial.word2mouth.previous.shared.DirectoryConstants;
import com.imperial.word2mouth.previous.shared.IntentNames;
import com.imperial.word2mouth.previous.shared.LectureItem;
import com.imperial.word2mouth.previous.shared.adapters.ArrayAdapterLectureOnline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TeachOnlineCourseSummary extends AppCompatActivity {


    private ImageView thumbnail;
    private ImageButton sound;
    private TextView title;
    private ListView lecturesView;
    private ImageButton delete;
    private CourseItem courseItem;
    private ImageButton statistics;

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
    private TextToSpeech textToSpeech;
    private TextView followersCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_online_course_summary);

        getExtras();

        setUpUI();


        configureListLectures();
        configureDeleteButton();
        configureLectureSummaryButton();
        configureTextToSpeech();
        configureLongClicks();
        configureStatisticsButton();
    }

    private void configureStatisticsButton() {
        statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent courseStat = new Intent(TeachOnlineCourseSummary.this, TeachOnlineCourseStatistics.class);
                courseStat.putExtra(IntentNames.COURSE, courseItem);
                startActivity(courseStat);
            }
        });
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

                    db.collection("content").document(lectures.get(lectureNumber).getLectureIdentification()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String version = (String) documentSnapshot.get("versionUID");

                            // Delete from Firebase FireStore
                            db.collection("content").document(lectures.get(lectureNumber).getLectureIdentification()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // delete from the list adapter
                                    adapter.remove(lectures.get(lectureNumber));
                                    adapter.notifyDataSetChanged();
                                    adapter.notifyDataSetInvalidated();
                                    lectureNumber = -1;
                                }
                            });

                            db.collection("track").whereEqualTo("version", version).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                                    List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                                    if (docs.size() == 1) {
                                        DocumentSnapshot doc = docs.get(0);
                                        doc.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                File f = new File(getExternalFilesDir(null) + DirectoryConstants.lecturerTracking + version + ".txt");
                                                if (f.exists()) {
                                                    f.delete();
                                                }
                                            }
                                        });
                                    }
                                }
                            });

                            db.collection("track").document(version).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                        }
                    });



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

        // Statsitics
        statistics = findViewById(R.id.statistics);
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
            Toast.makeText(TeachOnlineCourseSummary.this, R.string.mustCreateAccount, Toast.LENGTH_SHORT).show();
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



    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }


/////////////////////////////////////////////////////////////////////////////////////////////


    public void speak(String string) {
        textToSpeech.speak(string, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void configureTextToSpeech() {
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(TeachOnlineCourseSummary.this, R.string.languageNotSupported, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TeachOnlineCourseSummary.this, R.string.initializationFailedSST, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public void configureLongClicks() {
        delete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.delete));
                return true;
            }
        });
        lectureSummaryButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.lectureSummary));
                return true;
            }
        });

        statistics.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.statistics));
                return true;
            }
        });

    }


}