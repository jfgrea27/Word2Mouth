package com.imperial.word2mouth.previous.teach.online;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.previous.shared.DirectoryConstants;
import com.imperial.word2mouth.previous.shared.FileHandler;
import com.imperial.word2mouth.previous.shared.LectureItem;
import com.imperial.word2mouth.previous.teach.online.fakeDataProducer.FakeDataProducer;
import com.imperial.word2mouth.previous.teach.online.lectureData.LectureTrackingData;
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
import java.util.Locale;

import static com.imperial.word2mouth.previous.teach.online.DataExtractor.LEAST;
import static com.imperial.word2mouth.previous.teach.online.DataExtractor.MOST;

public class TeachOnlineLectureSummary extends AppCompatActivity {


    private TextView downloadCounter;
    private TextView mostPopularSlide;
    private TextView leastPopularSlide;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private LectureItem lecture;

    private String lectureVersion = null;
    private LectureTrackingData ltd;
    private DataExtractor extractor;
    private ImageButton speakStatistics;
    private Button fakeButton;
    private TextToSpeech textToSpeech;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_online_summary);

        getExtras();

        setUpUI();



        configureSpeakStatistics();
        configureTextToSpeech();
        retrieveMetrics();
        configureFakeDataButton();
    }

    private void configureFakeDataButton() {
        fakeButton = findViewById(R.id.fakeButton);

        fakeButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                FakeDataProducer fdp = new FakeDataProducer(TeachOnlineLectureSummary.this, ltd.getNumberSlides());

                fdp.addDataToFile(lectureVersion);
            }
        });
    }

    private void configureTimeGraph() {
        GraphView graphView = findViewById(R.id.timeGraph);

        extractor = new DataExtractor(ltd);

        LineGraphSeries<DataPoint> timeSeries = extractor.getTimeSeries();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
        graphView.addSeries(timeSeries);
        graphView.setTitle(getString(R.string.totalTimeGraphLectureTitle));

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

        gridLabel.setPadding(50);
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMaxX(timeSeries.getHighestValueX());

        gridLabel.setHorizontalAxisTitle(getString(R.string.instance));
        gridLabel.setVerticalAxisTitle(getString(R.string.time));

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void configureAudioGraph() {
        GraphView graphView = (GraphView) findViewById(R.id.soundGraph);
        BarGraphSeries<DataPoint> audioSeries = extractor.getAudioSeries();

        graphView.addSeries(audioSeries);
        graphView.setTitle(getString(R.string.clicksAudioTitleLecture));
        GridLabelRenderer gridLabel = graphView.getGridLabelRenderer();
        gridLabel.setPadding(50);

        graphView.getViewport().setMinX(audioSeries.getLowestValueX() - 1);
        graphView.getViewport().setMaxX(audioSeries.getHighestValueX() + 1);
        graphView.getViewport().setXAxisBoundsManual(true);

        gridLabel.setHorizontalAxisTitle(getString(R.string.slideNumber));
        gridLabel.setVerticalAxisTitle(getString(R.string.viewAudio));



    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void configureVideoGraph() {
        GraphView graphView = (GraphView) findViewById(R.id.videoGraph);
        BarGraphSeries<DataPoint> videoSeries = extractor.getVideoSeries();

        graphView.addSeries(videoSeries);
        graphView.setTitle(getString(R.string.videoViewsTitleLecture));
        GridLabelRenderer gridLabel = graphView.getGridLabelRenderer();
        gridLabel.setPadding(50);
        graphView.getViewport().setMinX(videoSeries.getLowestValueX() - 1);
        graphView.getViewport().setMaxX(videoSeries.getHighestValueX() + 1);
        graphView.getViewport().setXAxisBoundsManual(true);
        gridLabel.setHorizontalAxisTitle(getString(R.string.slideNumber));
        gridLabel.setVerticalAxisTitle(getString(R.string.videoViews));
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
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
        sevenGraphView.addSeries(timeSeries);
        sevenGraphView.setTitle(getString(R.string.sevenRollingAverageLectureTitle));

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

        sevenGraphView.getViewport().setMinX(timeSeries.getLowestValueX());

        gridLabel.setPadding(50);
        sevenGraphView.getViewport().setMinX(timeSeries.getLowestValueX());
        sevenGraphView.getViewport().setMaxX(timeSeries.getHighestValueX());
        sevenGraphView.getViewport().setXAxisBoundsManual(true);
        gridLabel.setHorizontalAxisTitle(getString(R.string.last7Days));
        gridLabel.setVerticalAxisTitle(getString(R.string.time));

    }


    private void getExtras() {
        lecture = (LectureItem) getIntent().getExtras().get("lecture");
    }

    private void setUpUI() {
        downloadCounter = findViewById(R.id.number_downloads);
        speakStatistics = findViewById(R.id.speakStatistics);

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


    private void configureSpeakStatistics() {
        speakStatistics.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.speakStatisticsInformation));
                return false;
            }
        });

        speakStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = getString(R.string.numberDownloads) + ": " +  downloadCounter.getText().toString() + ".";
                s += getString(R.string.most_viewed_slide) + ": " +  mostPopularSlide.getText().toString() + ".";
                s += getString(R.string.least_viewed_slide) + ": " +  leastPopularSlide.getText().toString() + ".";
                speak(s);
            }
        });


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
                        Toast.makeText(TeachOnlineLectureSummary.this, R.string.languageNotSupported, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TeachOnlineLectureSummary.this, R.string.initializationFailedSST, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}