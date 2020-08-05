package com.imperial.word2mouth.teach.online;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.shared.CourseItem;
import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.shared.IntentNames;
import com.imperial.word2mouth.teach.online.courseData.CourseTrackingData;
import com.imperial.word2mouth.teach.online.lectureData.LectureTrackingData;
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
import java.util.Random;

public class TeachOnlineCourseStatistics extends AppCompatActivity {

    private TextView followersCounter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CourseItem courseItem;
    private ArrayList<Pair<String, Integer>> colors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_online_course_statistics);

        getExtras();

        configureTimeGraphView();
        configureFollowersCounter();
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
                DataExtractor extractor = new DataExtractor(courseTrackingData);
                ArrayList<LineGraphSeries<DataPoint>> seriesSet = extractor.getCourseTotalTime();


                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM");

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
                    graphView.addSeries(series);
                    if (series.getHighestValueX() > maxX) {
                        maxX = series.getHighestValueX();
                    }
                    counter++;
                }
                graphView.setTitle("Total Time Spent on Course");

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
                gridLabel.setHorizontalAxisTitle("Time Instances of Checking");
                gridLabel.setVerticalAxisTitle("Amount of Time spent in seconds");

                gridLabel.setPadding(32);
                graphView.getViewport().setMaxX(maxX);
                graphView.getLegendRenderer().setVisible(true);
            }
            @RequiresApi(api = Build.VERSION_CODES.O)
            private void configure7DayAverage(List<DocumentSnapshot> docs, CourseTrackingData courseTrackingData) {
                // get the Time For each course
                DataExtractor extractor = new DataExtractor(courseTrackingData);
                ArrayList<LineGraphSeries<DataPoint>> seriesSet = extractor.getCourseSevenRollingAverageTime();


                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM");

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
                sevenGraphView.setTitle("7 Rolling Average For Time Spent on Course");

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
                gridLabel.setHorizontalAxisTitle("Time Instances of Checking");
                gridLabel.setVerticalAxisTitle("Amount of Time spent in seconds");

                gridLabel.setPadding(32);
                sevenGraphView.getViewport().setMaxX(maxX);
                sevenGraphView.getLegendRenderer().setVisible(true);
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


}