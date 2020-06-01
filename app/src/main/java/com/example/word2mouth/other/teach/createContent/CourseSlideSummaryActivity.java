package com.example.word2mouth.other.teach.createContent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.word2mouth.R;
import com.example.word2mouth.other.CourseSlidePageActivity;
import com.example.word2mouth.other.OtherActivity;
import com.example.word2mouth.other.teach.createContent.data.CourseContent;

import java.io.File;
import java.util.ArrayList;

import static android.view.View.VISIBLE;

public class CourseSlideSummaryActivity extends OtherActivity {

    private Button createNewSlideButton;
    private ListView slides;
    private CourseContent courseContent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_folder_summary);

        courseContent = getIntent().getParcelableExtra("contents");

        configureCreateNewSlide();
        configureListView();

    }

    private void configureCreateNewSlide() {
        createNewSlideButton = findViewById(R.id.button_new_slide);
        createNewSlideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent slideIntent = (new Intent(CourseSlideSummaryActivity.this, TeachCourseSlidePageActivity.class));
                slideIntent.putExtra("contents", courseContent);
                startActivity(slideIntent);
            }
        });
    }

    private void configureListView() {
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.course_folder_summary, getListSlides());

        slides = findViewById(R.id.created_slides);
        slides.setAdapter(adapter);

    }

    private ArrayList<String> getListSlides() {
        File course = new File(courseContent.getCoursePath());
        File[] files = course.listFiles();
        ArrayList<String> directoryNames = new ArrayList<>();
        for (File file : files) {
            directoryNames.add(file.getName());
        }
        return directoryNames;
    }


}