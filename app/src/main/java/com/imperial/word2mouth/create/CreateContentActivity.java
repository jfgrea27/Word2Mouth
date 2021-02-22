package com.imperial.word2mouth.create;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.imperial.word2mouth.R;
import com.imperial.word2mouth.common.dialog.CourseNameDialog;
import com.imperial.word2mouth.common.adapters.CourseItemAdapter;
import com.imperial.word2mouth.helpers.CourseLectureItemBuilder;
import com.imperial.word2mouth.helpers.FileSystemConstants;
import com.imperial.word2mouth.helpers.FileSystemHelper;
import com.imperial.word2mouth.model.CourseItem;
import com.imperial.word2mouth.IntentNames;

import org.json.JSONException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;

public class CreateContentActivity extends AppCompatActivity {

    /////////////////////// UI //////////////////////
    private ImageButton createButton;
    private ImageButton deleteButton;


    /////////////////////// TTS //////////////////////
    private TextToSpeech textToSpeech;


    /////////////////////// ListView //////////////////////
    private int selectedContent = -1;
    private RecyclerView courseListView;
    private CourseItemAdapter courseItemAdapter;
    private ArrayList<CourseItem> courseItems;

    ////////////////////////Sound Thumbnail//////////////////
    private MediaPlayer player;

    //////////////////////////////////////////////////////
    // Creation of course Model
    private String courseName;
    private String courseLanguage;
    private String courseTopic;
    private CourseItem courseItem;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_content);

        configureUI();
        configureTTS();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Configure UI
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void configureUI() {
        this.deleteButton = findViewById(R.id.delete_button);
        this.deleteButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);

        this.createButton = findViewById(R.id.create_button);

        this.courseListView = findViewById(R.id.recycleCourseView);

        configureOnClick();
        configureRecycleView();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void configureRecycleView() {
        courseListView = findViewById(R.id.recycleCourseView);

        courseItems = getFileCourseItems();
        courseItemAdapter = new CourseItemAdapter(courseItems, this);

        courseListView.setAdapter(courseItemAdapter);
        courseListView.setLayoutManager(new LinearLayoutManager(CreateContentActivity.this));
        courseListView.scrollToPosition(0);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ArrayList<CourseItem> getFileCourseItems() {
        ArrayList<CourseItem> courseItems = new ArrayList<>();
        try {
            courseItems = CourseLectureItemBuilder.getCourseItemFromDirectory(FileSystemConstants.offline, getApplicationContext());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return courseItems;
    }



    private void configureOnClick() {
        this.createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
////                Intent createIntent = new Intent(getApplicationContext(), TeachOfflineCourseSummary.class);
//
//                if (selectedContent > -1) {
//                    startActivity(createIntent);
//                } else {
//                    ();
////                    CourseItem newCourse = new CourseItem(courseName, courseLanguage, courseTopic);
////                    createIntent.putExtra(IntentNames.COURSE, newCourse);
//                }
//                startActivity(createIntent);
                dialogCourseCreation();
            }
        });
        this.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CreateContentActivity.this,
                        "Delete content",
                        Toast.LENGTH_LONG);
            }
        });
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Pop Up Dialogs
    ///////////////////////////////////////////////////////////////////////////////////////////////


    private void dialogCourseCreation() {

        // Chaining of Course Name, Course Language and Course Category in Dialog Fragments
        CourseNameDialog courseNameDialog = new CourseNameDialog(this);
        courseNameDialog.show(getSupportFragmentManager(), getString(R.string.course_name));
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Model
    ///////////////////////////////////////////////////////////////////////////////////////////////


    public void setCourseLanguage(String courseLanguage) {
        this.courseLanguage = courseLanguage;
    }

    public void setCourseTopic(String courseTopic) {
        this.courseTopic = courseTopic;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Create New Course
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void createCourse() {
        this.courseItem = new CourseItem(this.courseName, this.courseLanguage, this.courseTopic);
        this.courseItem = FileSystemHelper.createCourseFileSystem(courseItem, getApplicationContext());
        intentToCreateCourseAndStartActivity();
    }

    private void intentToCreateCourseAndStartActivity() {
        Intent createIntent = new Intent(getApplicationContext(), CourseSummaryCreateActivity.class);


        createIntent.putExtra(IntentNames.COURSE, (Parcelable) this.courseItem);
        startActivity(createIntent);

        // TODO Complete this when you have lectures
//        if (courseNumber > -1) {
//            createIntent.putExtra(IntentNames.COURSE, localCourses.get(courseNumber));
//            startActivity(createIntent);
//        } else {
//            createIntent.putExtra(IntentNames.COURSE, newCourse);
//            startActivity(createIntent);
//        }


    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Text To Speech
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void configureTTS() {
        this.textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(CreateContentActivity.this,
                                R.string.languageNotSupported, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CreateContentActivity.this,
                            R.string.initializationFailedSST, Toast.LENGTH_SHORT).show();
                }
            }
        });

        configureOnLongClick();
    }

    private void configureOnLongClick() {
        this.deleteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CreateContentActivity.this.textToSpeech.speak(
                        // TODO Check whether text TTS suitable
                        getString(R.string.delete),
                        TextToSpeech.QUEUE_FLUSH,
                        null);
                return true;
            }
        });
        this.createButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CreateContentActivity.this.textToSpeech.speak(
                        // TODO Check whether text TTS suitable
                        getString(R.string.create_button),
                        TextToSpeech.QUEUE_FLUSH,
                        null);
                return true;
            }
        });
    }



//    public void createCourse() {
//        courseDirectory = FileHandler.createDirectoryForCourseAndReturnIt(courseName, getView().getContext());
//        coursePath = courseDirectory.getPath();
//        FileHandler.createDirectoryAndReturnIt(courseDirectory.getPath(), FileHandler.META);
//        FileHandler.createFileForSlideContentAndReturnIt(coursePath + DirectoryConstants.meta , null, null, selectedLanguage, FileHandler.LANGUAGE_SELECTION);
//        FileHandler.createFileForSlideContentAndReturnIt(coursePath + DirectoryConstants.meta , null, null,selectedCategory, FileHandler.CATEGORY_SELECTION);
//        intentToCreateCourseAndStartActivity();
//    }



}