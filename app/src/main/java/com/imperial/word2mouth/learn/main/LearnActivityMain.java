package com.imperial.word2mouth.learn.main;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.imperial.word2mouth.Word2Mouth;
import com.imperial.word2mouth.background.ConnectivityReceiver;
import com.imperial.word2mouth.background.NewLecturesDialog;
import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.learn.main.ui.SectionsPagerAdapter;
import com.imperial.word2mouth.shared.FileHandler;
import com.imperial.word2mouth.shared.FileReaderHelper;
import com.imperial.word2mouth.teach.SignInDialog;
import com.imperial.word2mouth.teach.TeachActivityMain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class LearnActivityMain extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private SectionsPagerAdapter sectionsPagerAdapter;
    private TextToSpeech textToSpeech;
    private TabLayout tabs;
    private Toolbar toolbar;
    private Menu menu;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_main_tabbed);
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);


        setToolBar();

        fileManagement();

        configureTextToSpeech();


        configureTabs(viewPager);

        configureOnLongClicks();
        configureCacheFolderSend();

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void configureCacheFolderSend() {
        if (isNetworkConnected()) {
            uploadTrackingData();
        }
    }

    private void configureOnLongClicks() {
        toolbar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.learner));
                return true;
            }
        });

        tabs.getTabAt(0).view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.onDevice));
                return false;
            }
        });

        tabs.getTabAt(1).view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.onInternet));
                return false;
            }
        });



    }

    private void configureTabs(ViewPager viewPager) {
        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        tabs.getTabAt(0).setIcon(R.drawable.ic_learn_offline_online_0);
        tabs.getTabAt(1).setIcon(R.drawable.ic_learn_offline_online_1);
    }


    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void fileManagement() {
        File f = new File(getExternalFilesDir(null).getPath() + DirectoryConstants.zip);
        if (!f.exists()) {
            f.mkdirs();
        }

        f = new File(getExternalFilesDir(null).getPath() + DirectoryConstants.offline);
        if (!f.exists()) {
            f.mkdirs();
        }

        f = new File(getExternalFilesDir(null).getPath() + DirectoryConstants.online);
        if (!f.exists()) {
            f.mkdirs();
        }

        f = new File(getExternalFilesDir(null).getPath() + DirectoryConstants.cache);
        if (!f.exists()) {
            f.mkdirs();
        }

        f = new File(getExternalFilesDir(null).getPath() + DirectoryConstants.followFoder);
        if (!f.exists()) {
            f.mkdirs();
        }

        f = new File(getExternalFilesDir(null).getPath() + DirectoryConstants.lecturerTracking);
        if (!f.exists()) {
            f.mkdirs();
        }

    }


    private void setToolBar() {
        toolbar = (Toolbar) findViewById(R.id.learn_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.learner));

    }


    @Override
    protected void onRestart() {
        super.onRestart();
       recreate();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // register connection status listener
        Word2Mouth.activityResumed();
        Word2Mouth.getInstance().setConnectivityListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Word2Mouth.activityPaused();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        this.menu = menu;
        inflater.inflate(R.menu.menu_learn, this.menu);
//        configureMenuOnLongClicks();
        return true;
    }

    private void configureMenuOnLongClicks() {
        menu.getItem(0).getActionView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.teacher));
                return false;
            }
        });

        menu.getItem(1).getActionView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.settings));
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.teach:
                Intent intent = new Intent(LearnActivityMain.this, TeachActivityMain.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
                        Toast.makeText(LearnActivityMain.this, "Language not supported", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LearnActivityMain.this, "Initialization failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////


    private Map<String, ArrayList<String>> downloadedLectures = new HashMap<>();
    private Map<String, ArrayList<String>> onlineLectures = new HashMap<>();

    private Map<String, ArrayList<String>> outdatedLectures = new HashMap<>();
    private Map<String, ArrayList<String>> newLectures = new HashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if(isConnected){

            followingFetch();
            uploadTrackingData();
        } else {

            outdatedLectures.clear();
            newLectures.clear();
            onlineLectures.clear();
            downloadedLectures.clear();
            Toast.makeText(LearnActivityMain.this, getString(R.string.internetNotConnected), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadTrackingData() {
        File trackingFolder = new File(getExternalFilesDir(null) + DirectoryConstants.cache);
        File[] trackingLectures = trackingFolder.listFiles();

        for (File trackLecture : trackingLectures) {

            String version = null;

            FileInputStream fis = null;
            try {
                fis = new FileInputStream(trackLecture);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // Retrieve data and Update Lecture Tracker
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            try {
                version = br.readLine();

                String finalVersion = version;
                db.collection("track").whereEqualTo("version", version).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        if (docs.size() == 1) {
                            DocumentSnapshot doc = docs.get(0);


                            List<Long> audioFirebase = (List<Long>) doc.get("audio");
                            List<Long> videoFirebase = (List<Long>) doc.get("video");
                            List<Long> timeFirebase = (List<Long>) doc.get("time");

                            try {
                                String entry;
                                entry = br.readLine();
                                int counter = 0;
                                while (entry != null && !entry.isEmpty()) {



                                    // Update within the LectureTracker
                                    String[] data = entry.split("\\s*,\\s*");
                                    long time = Long.parseLong(data[0]);
                                    int videoCounter = Integer.parseInt(data[1]);
                                    int soundCounter = Integer.parseInt(data[2]);

                                    audioFirebase.set(counter, new Long(audioFirebase.get(counter) + soundCounter));
                                    videoFirebase.set(counter, new Long(videoCounter + videoFirebase.get(counter)));
                                    timeFirebase.set(counter, new Long(time + timeFirebase.get(counter)));

                                    counter++;
                                    entry = br.readLine();
                                }
                                br.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            // Update Firebase
                            doc.getReference().update("audio", audioFirebase);
                            doc.getReference().update("time", timeFirebase);
                            doc.getReference().update("video", videoFirebase);


                            // clear the File on Learner Track
                            trackLecture.delete();
                            FileHandler.createFileForLectureTracking(finalVersion, audioFirebase.size(), LearnActivityMain.this);

                        } else {
                            // delete the folder since old version
                            trackLecture.delete();
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


    private void followingFetch() {
        File followingFolder = new File(getExternalFilesDir(null) + DirectoryConstants.followFoder);
        File[] followingCourses = followingFolder.listFiles();

        int numberCourseFollowing = followingCourses.length;
        ArrayList<String> courseUIDs = new ArrayList<>();

        for (File course : followingCourses) {
            // check if course exists on the database:
            int fileNameLength = course.getName().length();
            String courseUID = course.getName().substring(0, fileNameLength - 4);
            courseUIDs.add(courseUID);
        }

        int counter = 0;
        for (File course : followingCourses) {
            String courseUID = courseUIDs.get(counter);
            counter++;

            int finalCounter = counter;

            db.collection("content").whereEqualTo("type", "Course").whereEqualTo("courseUID", courseUID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    if (checkCourseExists(queryDocumentSnapshots.getDocuments())) {
                        // get all the names of the lectures already downloaded inside the course following file
                        getLecturesAlreadyDownloaded(course, courseUID);

                        // Get Courses that are online under that course

                        if (finalCounter == numberCourseFollowing) {
                            getOnlineLectures(courseUIDs);
                        }


                    } else {
                        // No course of that name in the database - can delete

                        int fileNameLength = course.getName().length();
                        String courseUID = course.getName().substring(0, fileNameLength - 4);
                        courseUIDs.remove(courseUID);

                        course.delete();

                        if (finalCounter == numberCourseFollowing - 1) {
                            getOnlineLectures(courseUIDs);
                        }

                    }
                }


                private void getLecturesAlreadyDownloaded(File course, String coureUID) {
                    ArrayList<String> lectures = new ArrayList<>();

                    try {
                        Scanner scanner = new Scanner(course);
                        while (scanner.hasNextLine()) {
                            lectures.add(scanner.nextLine());
                        }
                        scanner.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    downloadedLectures.put(coureUID, lectures);
                }

                private void getOnlineLectures(ArrayList<String> courseUIDs) {
                    int numberCourses = courseUIDs.size();
                    int counter = 0;
                    for (String courseUID: courseUIDs) {

                        counter++;

                        int finalCounter1 = counter;
                        db.collection("content").whereEqualTo("type", "Lecture").whereEqualTo("courseUID", courseUID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                getOnlineLectures(queryDocumentSnapshots.getDocuments(), courseUID);

                                getOutDatedLectures(courseUID);

                                getNewLectures(courseUID);

                                if (finalCounter1 == numberCourses) {
                                    // Remove outdated from file
                                    removeLecturesFromFollowingFile();

                                    // Add new Lectures to 'to DOWNLOAD'

                                    @SuppressLint("ResourceType") TabLayout tabhost = (TabLayout) findViewById(R.id.tabs);
                                    tabhost.getTabAt(1).select();

                                    ArrayList<String> keysToDelete = new ArrayList<>();
                                    for (Map.Entry<String, ArrayList<String>> entry : newLectures.entrySet()) {
                                        if (entry.getValue().size() == 0) {
                                            keysToDelete.add(entry.getKey());
                                        }
                                    }

                                    for (String k : keysToDelete) {
                                        newLectures.remove(k);
                                    }

                                    if (Word2Mouth.isActivityVisible()) {
                                        NewLecturesDialog newLecturesDialog = new NewLecturesDialog(newLectures);
                                        newLecturesDialog.show(getSupportFragmentManager().beginTransaction(), "newLectures");
                                    } else {
                                        @SuppressLint("WrongConstant") Notification.Builder builder = new Notification.Builder(LearnActivityMain.this, "Channel").setSmallIcon(R.drawable.category_academic)
                                                .setContentTitle("Test")
                                                .setContentText("Test2")
                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                                    }


                                }

                            }

                            private void removeLecturesFromFollowingFile() {
                                for (Map.Entry<String, ArrayList<String>> entry : outdatedLectures.entrySet()) {
                                    String courseUID = entry.getKey();
                                    ArrayList<String> toDelete = entry.getValue();

                                    File f = new File(LearnActivityMain.this.getExternalFilesDir(null) + DirectoryConstants.followFoder + courseUID + ".txt");
                                    for (String lectureCode : toDelete) {
                                        try {
                                            FileReaderHelper.removesAnyLineMatchingPatternInFile(LearnActivityMain.this, f,lectureCode );
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                }

                            }

                            private void getNewLectures(String courseUID) {

                                ArrayList<String> temp = new ArrayList<>();

                                for (String lec : onlineLectures.get(courseUID)) {
                                    if (!downloadedLectures.get(courseUID).contains(lec)) {
                                        temp.add(lec);
                                    }
                                }
                                newLectures.put(courseUID, temp);
                            }

                            private void getOutDatedLectures(String courseUID) {
                                ArrayList<String> temp = new ArrayList<>();

                                for (String lec : downloadedLectures.get(courseUID)) {
                                    if (!onlineLectures.get(courseUID).contains(lec)) {
                                        temp.add(lec);
                                    }
                                }
                                outdatedLectures.put(courseUID, temp);
                            }

                            private void getOnlineLectures(List<DocumentSnapshot> documents, String courseUID) {
                                ArrayList<String> lectures = new ArrayList<>();

                                for (DocumentSnapshot doc : documents) {
                                    lectures.add((String) doc.get("lectureUID"));
                                }
                                onlineLectures.put(courseUID, lectures);
                            }
                        });

                    }


                }

                private boolean checkCourseExists(List<DocumentSnapshot> documents) {
                    if (documents.isEmpty()) {
                        return false;
                    }
                    return true;
                }
            });


    }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

}