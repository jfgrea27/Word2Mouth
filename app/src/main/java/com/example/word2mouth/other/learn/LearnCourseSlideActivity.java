package com.example.word2mouth.other.learn;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.word2mouth.R;
import com.example.word2mouth.other.CourseSlidePageActivity;

public class LearnCourseSlideActivity extends CourseSlidePageActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_slide_page);

        configureNextSlideButton();
        configurePreviousSlideButton();

        //TODO
        configureAudioButton();
        configureVideoButton();
    }

    @Override
    protected void configureAudioButton() {
        audioButton = findViewById(R.id.button_audio);
        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                Toast.makeText(getApplicationContext(), "Playing sound", Toast.LENGTH_SHORT ).show();
            }
        });
    }

    @Override
    protected void configureNextSlideButton() {
        nextSlide = findViewById(R.id.button_next);
        nextSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LearnCourseSlideActivity.this,
                        LearnCourseSlideActivity.class));
            }
        });
    }

    @Override
    protected void configureVideoButton() {
        videoView = findViewById(R.id.videoView);
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Playing Video", Toast.LENGTH_SHORT ).show();
            }
        });

    }
}
