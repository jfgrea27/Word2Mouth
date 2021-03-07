package com.imperial.word2mouth.create;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.ImageButton;

import com.imperial.word2mouth.R;
import com.imperial.word2mouth.common.dialog.CourseNameDialog;
import com.imperial.word2mouth.common.adapters.CourseItemAdapter;
import com.imperial.word2mouth.common.tts.SpeakIcon;
import com.imperial.word2mouth.helpers.CourseLectureItemBuilder;
import com.imperial.word2mouth.helpers.FileSystemConstants;
import com.imperial.word2mouth.helpers.FileSystemHelper;
import com.imperial.word2mouth.model.CourseItem;
import com.imperial.word2mouth.common.tags.IntentNames;

import org.json.JSONException;

import java.util.ArrayList;

public class CreateContentActivity extends AppCompatActivity {

    /////////////////////// Create Button //////////////////////
    private ImageButton createButton;

    /////////////////////// TTS //////////////////////
    private SpeakIcon textToSpeech;

    /////////////////////// RecycleView  //////////////////////
    private int selectedContent = -1;
    private RecyclerView courseRecycleView;
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

        configureCreateButton();
        configureRecycleView();

        configureTTS();
    }

    @Override
    protected void onResume() {
        super.onResume();
        courseItemAdapter.notifyDataSetChanged();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Create Button
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private void configureCreateButton() {
        createButton = findViewById(R.id.create_button);
        createButton.setOnClickListener(v -> dialogCourseCreation());
        // TTS
        createButton.setOnLongClickListener(v -> {
            CreateContentActivity.this.textToSpeech.speak(getString(R.string.create_button));
            return true;
        });
    }
    private void dialogCourseCreation() {

        // Chaining of Course Name, Course Language and Course Category in Dialog Fragments
        CourseNameDialog courseNameDialog = new CourseNameDialog(this);
        courseNameDialog.show(getSupportFragmentManager(), getString(R.string.course_name));
    }
    public void createCourse() {
        courseItem = new CourseItem(this.courseName, this.courseLanguage, this.courseTopic);
        courseItem = FileSystemHelper.createCourseFileSystem(courseItem, getApplicationContext());

        // Updating the RecycleView
        courseItems.add(this.courseItem);
        intentToCourseSummaryActivity();
    }

    public void enterCourse(CourseItem courseItem) {
        this.courseItem = courseItem;
        intentToCourseSummaryActivity();
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Intent to CourseSummaryActivity
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private void intentToCourseSummaryActivity() {
        Intent createIntent = new Intent(getApplicationContext(), CourseSummaryCreateActivity.class);
        createIntent.putExtra(IntentNames.COURSE, (Parcelable) this.courseItem);
        startActivity(createIntent);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Course Items RecycleView
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void configureRecycleView() {
        courseRecycleView = findViewById(R.id.recycleCourseView);

        courseItems = getFileCourseItems();
        courseItemAdapter = new CourseItemAdapter(courseItems, this);

        courseRecycleView.setAdapter(courseItemAdapter);
        courseRecycleView.setLayoutManager(new LinearLayoutManager(CreateContentActivity.this));
        courseRecycleView.scrollToPosition(0);

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

    public void setCourseLanguage(String courseLanguage) {
        this.courseLanguage = courseLanguage;
    }

    public void setCourseTopic(String courseTopic) {
        this.courseTopic = courseTopic;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Text To Speech
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void configureTTS() {
        textToSpeech = new SpeakIcon(this);
    }

}