package com.example.word2mouth.other;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.word2mouth.R;

public abstract class CourseSlidePageActivity extends OtherActivity {

    protected Button nextSlide;
    protected Button previousSlide;
    protected Button audioButton;
    protected VideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected abstract void configureAudioButton();

    protected void configurePreviousSlideButton() {
        previousSlide = findViewById(R.id.button_previous);
        previousSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected abstract void configureVideoButton();

    protected abstract void configureNextSlideButton();
}
