package com.imperial.word2mouth.learn.main;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import android.speech.tts.TextToSpeech.OnInitListener;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.imperial.word2mouth.Word2Mouth;
import com.imperial.word2mouth.background.ConnectivityReceiver;
import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.learn.main.ui.SectionsPagerAdapter;
import com.imperial.word2mouth.shared.FileHandler;
import com.imperial.word2mouth.shared.FileReaderHelper;
import com.imperial.word2mouth.teach.TeachActivityMain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class LearnActivityMain extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_main_tabbed);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);


        setToolBar();

        fileManagement();


        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        tabs.getTabAt(0).setIcon(R.drawable.ic_learn_offline_online_0);
        tabs.getTabAt(1).setIcon(R.drawable.ic_learn_offline_online_1);
    }


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


        f = new File(getExternalFilesDir(null).getPath() + DirectoryConstants.cache + DirectoryConstants.following);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void setToolBar() {
        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.learn_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Word 2 Mouth");
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
        Word2Mouth.getInstance().setConnectivityListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_learn, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.teach:
                Intent intent = new Intent(LearnActivityMain.this, TeachActivityMain.class);
                startActivity(intent);
                return true;
            case R.id.setting:
                Toast.makeText(this, "Setting - TO DO", Toast.LENGTH_SHORT).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////





    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if(isConnected){

            File followingFolder = new File(getExternalFilesDir(null) + DirectoryConstants.followFoder);
            File[] followingCourses = followingFolder.listFiles();
            for (File course : followingCourses) {
                // check if course exists on the database:
                String courseUID = course.getName().substring(0, -4);
                db.collection("content").whereEqualTo("type", "Course").whereEqualTo("courseUID", courseUID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (checkCourseExists(queryDocumentSnapshots.getDocuments())) {
                            // get all the names of the lectures already downloaded inside the course following file
                            ArrayList<String> downloadedLectures = getLecturesAlreadyDownloaded(course);

                            // Get Courses that are online under that course
                            getOnlineLectures(downloadedLectures, courseUID);


                        } else {
                            // No course of that name in the database - can delete
                            course.delete();
                        }
                    }


                    private ArrayList<String> getLecturesAlreadyDownloaded(File course) {
                        ArrayList<String> lectures = new ArrayList<>();

                        try {
                            Scanner scanner = new Scanner(course);
                            while (scanner.hasNextLine()) {
                                lectures.add(scanner.nextLine())
                            }
                            scanner.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        return lectures;
                    }

                    private void getOnlineLectures(ArrayList<String> downloadedLectures, String courseUID) {
                        db.collection("content").whereEqualTo("type", "Lecture").whereEqualTo("courseUID", courseUID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                ArrayList<String> onlineLectures =  getOnlineLectures(queryDocumentSnapshots.getDocuments());

                                ArrayList<String> outdated = getOutDatedLectures(downloadedLectures, onlineLectures);

                                ArrayList<String> newLectures = getNewLectures(downloadedLectures, onlineLectures);

                                // Remove outdated from file
                                removeLecturesFromFollowingFile(courseUID, outdated);

                                // Add new Lectures to 'to DOWNLOAD'
                                //TODO
                            }

                            private void removeLecturesFromFollowingFile(String courseUID, ArrayList<String> outdated) {
                                File f = new File(LearnActivityMain.this.getExternalFilesDir(null) + DirectoryConstants.followFoder + courseUID + ".txt");
                                for (String lectureCode : outdated) {
                                    try {
                                        FileReaderHelper.removesAnyLineMatchingPatternInFile(LearnActivityMain.this, f,lectureCode );
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            private ArrayList<String> getNewLectures(ArrayList<String> downloadedLectures, ArrayList<String> onlineLectures) {

                                ArrayList<String> newLectures = new ArrayList<>();

                                for (String lec : onlineLectures) {
                                    if (!downloadedLectures.contains(lec)) {
                                        newLectures.add(lec);
                                    }
                                }
                                return newLectures;
                            }

                            private ArrayList<String> getOutDatedLectures(ArrayList<String> downloadedLectures, ArrayList<String> onlineLectures) {
                                ArrayList<String> outdatedLectures = new ArrayList<>();

                                for (String lec : downloadedLectures) {
                                    if (!onlineLectures.contains(lec)) {
                                        outdatedLectures.add(lec);
                                    }
                                }
                                return outdatedLectures;
                            }

                            private ArrayList<String> getOnlineLectures(List<DocumentSnapshot> documents) {
                                ArrayList<String> lectures = new ArrayList<>();

                                for (DocumentSnapshot doc : documents) {
                                    lectures.add((String) doc.get("lectureUID"));
                                }
                                return lectures;
                            }
                        });

                    }

                    private boolean checkCourseExists(List<DocumentSnapshot> documents) {
                        if (documents.isEmpty()) {
                            return false;
                        }
                        return true;
                    }
                });


            }
            // Check all files inside the following folder
            // Check if there is a course online of that identification
            // If not delete the file
            // if there is, go into the file and get all the lines that are duplicates
            // compare the ones that are in the database
            // The extra ones in the database will be added to some list 'to Download'
            // The extrao ones in the file will be delete from the file -> no longer part of the course


        } else {
            Toast.makeText(LearnActivityMain.this, "Internet not Connected", Toast.LENGTH_SHORT).show();
        }
    }

}