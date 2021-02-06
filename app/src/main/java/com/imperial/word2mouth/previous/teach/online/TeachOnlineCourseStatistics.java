package com.imperial.word2mouth.previous.teach.online;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.previous.shared.CourseItem;
import com.imperial.word2mouth.previous.shared.DirectoryConstants;
import com.imperial.word2mouth.previous.shared.IntentNames;
import com.imperial.word2mouth.previous.teach.online.courseData.CourseTrackingData;
import com.imperial.word2mouth.previous.teach.online.lectureData.LectureTrackingData;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class TeachOnlineCourseStatistics extends AppCompatActivity {

    private TableLayout legend;
    private TextView followersCounter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CourseItem courseItem;
    private ArrayList<Pair<String, Integer>> colors = new ArrayList<>();
    private TextView leastPopularLecture;
    private TextView mostPopularLecture;
    private DataExtractor extractor;
    private ImageButton speakStatitics;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_online_course_statistics);

        getExtras();

        configureTimeGraphView();
        configureFollowersCounter();
        configurePopularityCounter();
        configureTextToSpeech();
        configureSpeakStatistics();
        configureLegend();
    }

    private void configureLegend() {
        legend = findViewById(R.id.legend);
        TableRow bigRow = new TableRow(TeachOnlineCourseStatistics.this);
        bigRow.setGravity(Gravity.CENTER);
        TextView bigText = new TextView(TeachOnlineCourseStatistics.this);
        bigText.setText(getString(R.string.legend));
        bigText.setTextSize(20);
        bigText.setTextColor(Color.BLACK);
        bigRow.addView(bigText);
        bigText.setGravity(Gravity.CENTER);
        legend.addView(bigRow);
        TableRow titleRow = new TableRow(TeachOnlineCourseStatistics.this);
        TextView titleCol = new TextView(TeachOnlineCourseStatistics.this);
        TextView titleLec = new TextView(TeachOnlineCourseStatistics.this);
        titleCol.setText(getString(R.string.color));
        titleCol.setTextSize(17);
        titleLec.setTextSize(17);
        titleCol.setTextColor(Color.BLACK);
        titleLec.setTextColor(Color.BLACK);

        titleLec.setText(getString(R.string.lectureName));
        titleCol.setGravity(Gravity.CENTER);
        titleLec.setGravity(Gravity.CENTER);
        titleRow.addView(titleCol);
        titleRow.addView(titleLec);
        legend.addView(titleRow);
    }

    private void configureSpeakStatistics() {
        speakStatitics = findViewById(R.id.speakStatistics);
        speakStatitics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = getString(R.string.numberFollowers) + ": " +  followersCounter.getText().toString() + ".";
                s += getString(R.string.most_viewed_lecture) + ": " +  mostPopularLecture.getText().toString() + ".";
                s += getString(R.string.least_viewed_lecture) + ": " +  leastPopularLecture.getText().toString() + ".";
                speak(s);
            }
        });

        speakStatitics.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.speakStatisticsInformationCourse));
                return false;
            }
        });

    }

    private void configurePopularityCounter() {
        leastPopularLecture = findViewById(R.id.leastPopularLecture);
        mostPopularLecture = findViewById(R.id.mostPopularLecture);
    }

    private void configureTimeGraphView() {
        GraphView graphView = (GraphView) findViewById(R.id.timeGraph);
        GraphView sevenGraphView = (GraphView) findViewById(R.id.time7Graph);


        // get the data
        db.collection("content").whereEqualTo("courseUID", courseItem.getCourseOnlineIdentification()).whereEqualTo("type", "Lecture").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {


            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                ArrayList<String> versionLectures = new ArrayList<>();
                for (DocumentSnapshot doc : docs) {
                    versionLectures.add((String) doc.get("versionUID"));
                }
                CourseTrackingData courseTrackingData = getDataForAllLectures(versionLectures);
                configureTimeGraph(docs, courseTrackingData);
                configure7DayAverage(docs, courseTrackingData);

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            private void configureTimeGraph(List<DocumentSnapshot> docs, CourseTrackingData courseTrackingData) {
                // get the Time For each course
                extractor = new DataExtractor(courseTrackingData);
                ArrayList<LineGraphSeries<DataPoint>> seriesSet = extractor.getCourseTotalTime();

                leastPopularLecture.setText((String) docs.get(extractor.indexOfLeastPopularCourse()).get("lectureName"));
                mostPopularLecture.setText((String) docs.get(extractor.indexOfMostPopularCourse()).get("lectureName")) ;

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");

                double maxX = 0;
                int counter = 0;


                for (LineGraphSeries<DataPoint> series : seriesSet) {

                    Random rand = new Random();
                    float r = rand.nextFloat();
                    float g = rand.nextFloat();
                    float b = rand.nextFloat();
                    colors.add(new Pair<String, Integer>((String) docs.get(counter).get("lectureName"), Color.rgb(r, g, b)));
                    series.setColor(Color.rgb(r, g, b));
                    series.setTitle((String) docs.get(counter).get("lectureName"));

                    // Adding to Legend
                    TableRow row = new TableRow(TeachOnlineCourseStatistics.this);
                    TextView col = new TextView(TeachOnlineCourseStatistics.this);
                    col.setTextColor(Color.rgb(r, g, b));
                    TextView lec = new TextView(TeachOnlineCourseStatistics.this);
                    lec.setText((String) docs.get(counter).get("lectureName"));
                    col.setBackgroundColor(Color.rgb(r, g, b));
                    col.setGravity(Gravity.CENTER);
                    lec.setGravity(Gravity.CENTER);
                    row.addView(col);
                    row.addView(lec);

                    legend.addView(row);


                    graphView.addSeries(series);
                    if (series.getHighestValueX() > maxX) {
                        maxX = series.getHighestValueX();
                    }
                    counter++;
                }

                // creating space
                TableRow r = new TableRow(TeachOnlineCourseStatistics.this);
                TextView t = new TextView(TeachOnlineCourseStatistics.this);
                t.setText(" ");
                t.setTextSize(20);
                legend.addView(t);

                graphView.setTitle(getString(R.string.totalTimeGraphTitle));

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
                gridLabel.setHorizontalAxisTitle(getString(R.string.instance));
                gridLabel.setVerticalAxisTitle(getString(R.string.timeInSeconds));

                gridLabel.setPadding(50);
                graphView.getViewport().setMaxX(maxX);
            }
            @RequiresApi(api = Build.VERSION_CODES.O)
            private void configure7DayAverage(List<DocumentSnapshot> docs, CourseTrackingData courseTrackingData) {
                // get the Time For each course
                ArrayList<LineGraphSeries<DataPoint>> seriesSet = extractor.getCourseSevenRollingAverageTime();


                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");

                double maxX = 0;
                int counter = 0;

                for (LineGraphSeries<DataPoint> series : seriesSet) {
                    Random rand = new Random();

                    series.setColor(colors.get(counter).second);
                    series.setTitle(String.valueOf(colors.get(counter).first));
                    sevenGraphView.addSeries(series);
                    if (series.getHighestValueX() > maxX) {
                        maxX = series.getHighestValueX();
                    }
                    counter++;
                }
                sevenGraphView.setTitle(getString(R.string.sevenRollingAverageTitle));

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
                gridLabel.setHorizontalAxisTitle(getString(R.string.instance));
                gridLabel.setVerticalAxisTitle(getString(R.string.last7Days));

                gridLabel.setPadding(50);
                sevenGraphView.getViewport().setMaxX(maxX);
            }
        });


    }

    private CourseTrackingData getDataForAllLectures(ArrayList<String> versionLectures) {
        // Get all the data from the current lecture tracking
        CourseTrackingData courseTrackingData = new CourseTrackingData();
        for (String v : versionLectures) {
            File f = new File(getExternalFilesDir(null) + DirectoryConstants.lecturerTracking + v + ".txt");
            if (f.exists()) {
                courseTrackingData.getLectureGeneralData().add(new LectureTrackingData(f));
            }
        }
        return courseTrackingData;
    }




    private void getExtras() {
        courseItem = (CourseItem) getIntent().getExtras().get(IntentNames.COURSE);

    }


    private void configureFollowersCounter() {
        followersCounter = findViewById(R.id.number_followers);

        db.collection("content").document(courseItem.getCourseOnlineIdentification()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.get("followersCounter") != null) {
                    followersCounter.setText(documentSnapshot.get("followersCounter").toString());
                }
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
                        Toast.makeText(TeachOnlineCourseStatistics.this, R.string.languageNotSupported, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TeachOnlineCourseStatistics.this, R.string.initializationFailedSST, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



}