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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.shared.FileHandler;
import com.imperial.word2mouth.shared.LectureItem;
import com.imperial.word2mouth.teach.online.lectureData.LectureTrackingData;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.imperial.word2mouth.teach.online.DataExtractor.LEAST;
import static com.imperial.word2mouth.teach.online.DataExtractor.MOST;

public class TeachOnlineLectureSummary extends AppCompatActivity {

    private ImageView thumbnail;
    private ImageButton sound;
    private TextView lectureName;
    private ListView slidesView;

    private TextView downloadCounter;
    private TextView mostPopularSlide;
    private TextView leastPopularSlide;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user;
    private Uri audioUri;
    private MediaPlayer player;
    private LectureItem lecture;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    private String lectureVersion = null;
    private LectureTrackingData ltd;
    private DataExtractor extractor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_online_summary);

        getExtras();

        setUpUI();

        configureAudio();

        retrieveMetrics();
    }

    private void configureTimeGraph() {
        GraphView graphView = (GraphView) findViewById(R.id.timeGraph);

        extractor = new DataExtractor(ltd);

        LineGraphSeries<DataPoint> timeSeries = extractor.getTimeSeries();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM");
        graphView.addSeries(timeSeries);
        graphView.setTitle("Total Time Spent on Lecture");

        GridLabelRenderer gridLabel = graphView.getGridLabelRenderer();
        gridLabel.setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                     return sdf.format(new Date((long) value));
                } else {
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        gridLabel.setPadding(32);
        graphView.getViewport().setMaxX(timeSeries.getHighestValueX());
        gridLabel.setHorizontalAxisTitle("Time Instances of Checking");
        gridLabel.setVerticalAxisTitle("Amount of Time spent in seconds");

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void configureAudioGraph() {
        GraphView graphView = (GraphView) findViewById(R.id.soundGraph);
        BarGraphSeries<DataPoint> audioSeries = extractor.getAudioSeries();

        graphView.addSeries(audioSeries);
        graphView.setTitle("Counter of Audio Clicks Per Slide on Lecture");
        GridLabelRenderer gridLabel = graphView.getGridLabelRenderer();
        gridLabel.setPadding(32);
        graphView.getViewport().setMaxX(audioSeries.getHighestValueX());

        gridLabel.setHorizontalAxisTitle("Slide Number");
        gridLabel.setVerticalAxisTitle("Amount of Audio Clicks");



    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void configureVideoGraph() {
        GraphView graphView = (GraphView) findViewById(R.id.videoGraph);
        BarGraphSeries<DataPoint> videoSeries = extractor.getVideoSeries();

        graphView.addSeries(videoSeries);
        graphView.setTitle("Counter of Video Clicks Per Slide on Lecture");
        GridLabelRenderer gridLabel = graphView.getGridLabelRenderer();
        gridLabel.setPadding(32);
        graphView.getViewport().setMaxX(videoSeries.getHighestValueX());

        gridLabel.setHorizontalAxisTitle("Slide Number");
        gridLabel.setVerticalAxisTitle("Amount of Video Clicks");
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    private void configureMostAndLeastPopularSlides() {
        mostPopularSlide = findViewById(R.id.mostPopularSlide);
        leastPopularSlide = findViewById(R.id.leastPopularSlide);
        mostPopularSlide.setText(extractor.getPopularSlideNumber(MOST));
        leastPopularSlide.setText(extractor.getPopularSlideNumber(LEAST));
    }


    private void retrieveMetrics() {

        // retrieve the data from the firebase
        db.collection("content").whereEqualTo("lectureUID", lecture.getLectureIdentification()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                if (docs.size() == 1) {
                    DocumentSnapshot doc = docs.get(0);
                    String version = (String) doc.get("versionUID");

                    lectureVersion = version;

                    File trackingData = new File(getExternalFilesDir(null) + DirectoryConstants.lecturerTracking + version + ".txt");
                    if (!trackingData.exists()) {
                        try {
                            trackingData.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    db.collection("track").whereEqualTo("version", version).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                            if (docs.size() == 1) {
                                DocumentSnapshot doc = docs.get(0);

                                List<Long> time = (List<Long>) doc.get("time");
                                List<Long> audio = (List<Long>) doc.get("audio");
                                List<Long> video = (List<Long>) doc.get("video");

                                // update the tracking file for that lecture
                                FileHandler.updateTeacherTracker(trackingData, String.valueOf(System.currentTimeMillis()), time, audio, video);

                                int size = time.size();

                                time = new ArrayList<>(Collections.nCopies(size, Long.parseLong(String.valueOf(0))));
                                audio = new ArrayList<>(Collections.nCopies(size, Long.parseLong(String.valueOf(0))));
                                video = new ArrayList<>(Collections.nCopies(size, Long.parseLong(String.valueOf(0))));

                                // clear the firebase
                                doc.getReference().update("audio",audio);
                                doc.getReference().update("time", time);
                                doc.getReference().update("video", video);

                                ltd = new LectureTrackingData(new File(getExternalFilesDir(null) + DirectoryConstants.lecturerTracking + lectureVersion + ".txt"));


                                // time
                                configureTimeGraph();
                                configureMostAndLeastPopularSlides();
                                configure7DayAverageTimeSpent();

                                // video
                                configureVideoGraph();
                                // audio
                                configureAudioGraph();

                            }
                        }
                    });


                }
            }
        });


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void configure7DayAverageTimeSpent() {
        GraphView sevenGraphView = (GraphView) findViewById(R.id.time7Graph);

        LineGraphSeries<DataPoint> timeSeries = extractor.get7DayRollingAverage(null);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM");
        sevenGraphView.addSeries(timeSeries);
        sevenGraphView.setTitle("7 Day Rolling Average on Time Spent on Lecture");

        GridLabelRenderer gridLabel = sevenGraphView.getGridLabelRenderer();
        gridLabel.setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    return sdf.format(new Date((long) value));
                } else {
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        gridLabel.setPadding(32);
        sevenGraphView.getViewport().setMaxX(timeSeries.getHighestValueX());
        gridLabel.setHorizontalAxisTitle("Last 7 Days");
        gridLabel.setVerticalAxisTitle("Amount of Time spent in seconds");

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