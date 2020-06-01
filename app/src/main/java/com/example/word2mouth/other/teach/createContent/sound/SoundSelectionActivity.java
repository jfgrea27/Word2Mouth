package com.example.word2mouth.other.teach.createContent.sound;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.example.word2mouth.R;
import com.example.word2mouth.other.CourseSlidePageActivity;
import com.example.word2mouth.other.SelectionMediaActivity;

import java.io.IOException;

public class SoundSelectionActivity extends SelectionMediaActivity {

    private static final int RESULT_CHOOSE_SOUND_GALLERY = 1;
    private static final int RESULT_CAPTURE_SOUND = 2;
    private final int MAX_DURATION_VIDEO = 100;
    private boolean recording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_selection);
        configureBackButton();

        configureCaptureButton();
        configureGalleryButton();
    }

    @Override
    protected void configureCaptureButton() {
        captureButton = findViewById(R.id.button_mic);
        final MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_2_TS);
        recorder.setOutputFile("Recording 1");
        recorder.setMaxDuration(MAX_DURATION_VIDEO);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recording) {
                    recorder.start();
                    recording = true;
                } else {
                    recorder.stop();
                }

            }
        });
    }

    @Override
    protected void configureGalleryButton() {
        galleryButton.findViewById(R.id.button_gallery_sounds);
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("audio/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.putExtra(Intent.EXTRA_DURATION_MILLIS, MAX_DURATION_VIDEO);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Audio"), RESULT_CHOOSE_SOUND_GALLERY);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Intent intentSelectedImage = new Intent();
            switch (requestCode) {
                case (RESULT_CAPTURE_SOUND):
                case (RESULT_CHOOSE_SOUND_GALLERY):
                    intentSelectedImage.putExtra("audio d", data.getData());
                    setResult(RESULT_OK, intentSelectedImage);
                    finish();
                    break;

            }
        }
    }
}

