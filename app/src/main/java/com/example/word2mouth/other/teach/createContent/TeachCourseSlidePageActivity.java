package com.example.word2mouth.other.teach.createContent;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.word2mouth.R;
import com.example.word2mouth.other.CourseSlidePageActivity;
import com.example.word2mouth.other.teach.createContent.sound.AudioRecorder;


public class TeachCourseSlidePageActivity extends CourseSlidePageActivity {

    private static final int RESULT_CHOOSE_IMAGE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_slide_page);


        configureBackButton();
        configureNextSlideButton();
        configurePreviousSlideButton();
        configureAudioButton();
        configureVideoButton();
    }


    @Override
    protected void configureAudioButton() {

    }


//        protected void configureAudioButton() {
//        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
//
//        audioButton = findViewById(R.id.button_audio);
//        audioButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //TODO
//                audioFileName = getExternalCacheDir().getAbsolutePath();
//                audioFileName += "/audiorecordtest.3gp";
//
//                Toast.makeText(getApplicationContext(), "Recording sound", Toast.LENGTH_SHORT ).show();
//            }
//        });



    @Override
    protected void configureNextSlideButton() {
        nextSlide = findViewById(R.id.button_next);
        nextSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TeachCourseSlidePageActivity.this,
                        TeachCourseSlidePageActivity.class));
            }
        });
    }

    @Override
    protected void configureVideoButton() {
        videoView = findViewById(R.id.videoView);
        videoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent choiceVideo = new Intent(TeachCourseSlidePageActivity.this, MediaSelectionActivity.class);
                Uri selectedVideo = null;
                choiceVideo.putExtra("video", selectedVideo);
                startActivityForResult(choiceVideo, RESULT_CHOOSE_IMAGE);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_CHOOSE_IMAGE && data != null && resultCode == RESULT_OK) {
            Toast.makeText(this, "Test Ok", Toast.LENGTH_LONG).show();
            Uri returnedVideo = data.getData();
            Uri file = Uri.parse(data.getExtras().get("video").toString());
            videoView.setVideoURI(file);
            videoView.start();
        }
    }
}
