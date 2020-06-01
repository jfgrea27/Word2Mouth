package com.example.word2mouth.other.learn;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.example.word2mouth.R;
import com.example.word2mouth.other.CourseSlidePageActivity;

import java.io.File;

public class LearnCourseSlideActivity extends CourseSlidePageActivity {

    private String courseName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_slide_page);

        configureNextSlideButton();

        if (savedInstanceState != null) {

        }
        Intent intent = getIntent();
        courseName = intent.getStringExtra("course");

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
                File f = new File(getApplicationContext().getExternalFilesDir(null), "/" + courseName);
                File f2 = new File(String.valueOf(f.listFiles()[0]));
                videoView.setVideoURI(Uri.parse(f2.getPath() + "/video.3gp"));
                videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setLooping(true);
                        videoView.start();
                    }
                });
            }
        });

    }
}
