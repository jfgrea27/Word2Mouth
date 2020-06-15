package com.imperial.slidepassertrial.learn.main.offline;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.imperial.slidepassertrial.R;
import com.imperial.slidepassertrial.shared.FileReader;
import com.imperial.slidepassertrial.shared.FileHandler;

import java.io.File;

public class SlideLearningActivity extends AppCompatActivity {

    // General Activity Buttons
    private ImageButton previousSlide = null;
    private ImageButton nextSlide = null;


    // Title
    private TextView titleView =null;
    private String title = null;

    // Instructions
    private TextView instructionsView = null;
    private String instructions = null;


    // Video
    private ImageButton videoButton = null;
    private VideoView videoView = null;
    private Uri video = null;

    // Audio
    private ImageButton audioButton = null;
    private Uri audio = null;
    private MediaPlayer audioPlayer = null;


    // Model Variables
    private int slideCounter = 0;
    private int totalNumberSlides = 0;
    private String coursePath;


    // File Management
    private File courseFolder = null;
    private File currentSlideDirectory = null;
    private File titleFile = null;
    private File videoFile = null;
    private File audioFile = null;
    private File instructionsFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_slide);

        coursePath = (String) getIntent().getExtras().get("course directory path");

        courseFolder = new File(coursePath);
        // meta
        totalNumberSlides = courseFolder.listFiles().length - 1;


        // Forward and Backward Buttons
        configurePreviousButton();
        configureNextButton();

        // Title
        configureSideTitleEdit();

        // instructions
        configureInstructionsEdit();

        // Video
        configureWatchVideoButton();
        configureVideoView();

        // Audio
        configureAudio();

        retrieveSlide();
        updateCurrentView();
    }

    private void configureSideTitleEdit() {
        titleView = findViewById(R.id.slide_title_text_learn);
    }

    private void configureInstructionsEdit() {
        instructionsView = findViewById(R.id.instructions);
    }

    private void configurePreviousButton() {
        previousSlide = findViewById(R.id.button_previous);
        previousSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideCounter--;
                // reach first slide
                if(slideCounter < 0) {
                    Toast.makeText(SlideLearningActivity.this, "First Slide", Toast.LENGTH_SHORT).show();
                    slideCounter++;
                }
                // retrieve previous slide
                else {
                    retrieveSlide();
                    updateCurrentView();
                }
            }
        });
    }

    private void configureNextButton() {
        nextSlide = findViewById(R.id.button_next);
        nextSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideCounter++;
                // creating a new Slide
                if(totalNumberSlides <= slideCounter) {
                    Toast.makeText(SlideLearningActivity.this, "End Of Slide Show", Toast.LENGTH_SHORT).show();
                    slideCounter--;
                }
                // retrieve previously saved file data
                else {
                    Toast.makeText(SlideLearningActivity.this, "Retrieve Next Slide", Toast.LENGTH_SHORT).show();
                    retrieveSlide();
                    updateCurrentView();
                }
            }
        });

    }

    private void updateCurrentView() {
        // refresh video view
        if (videoView != null) {
            videoView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
        } else{
            videoView.setVisibility(View.INVISIBLE);
        }

        if (video != null) {
            videoView.setVideoURI(video);
            videoButton.setVisibility(View.VISIBLE);
        } else {
            videoButton.setVisibility(View.INVISIBLE   );
        }

        if (audio != null) {
            audioButton.setVisibility(View.VISIBLE);
        } else {
            audioButton.setVisibility(View.INVISIBLE);
        }
    }

    private void retrieveSlide() {
        currentSlideDirectory = FileHandler.retrieveSlideDirectoryByNumber(coursePath, slideCounter);

        if (currentSlideDirectory != null) {
            // title
            titleFile = new File(currentSlideDirectory.getPath() + "/title.txt");
            if(titleFile.exists()) {
                String text = FileReader.readTextFromFile(currentSlideDirectory.getPath() + "/title.txt");
                if (text.isEmpty()) {
                    titleView.setText("");
                } else {
                    titleView.setText(text);
                }
            }

            // instructions
            instructionsFile = new File(currentSlideDirectory.getPath() + "/instructions.txt");
            if (instructionsFile.exists()) {
                String text = FileReader.readTextFromFile(currentSlideDirectory.getPath() + "/instructions.txt");
                if (text.isEmpty()) {
                    instructionsView.setText("");
                } else {
                    instructionsView.setText(text);
                }
            }

            // Audio
            audioFile = new File(currentSlideDirectory.getPath() + "/audio.3gp");
            if (audioFile.exists()) {
                audio = Uri.parse(currentSlideDirectory.getPath() + "/audio.3gp");
            } else {
                audio = null;
                audioButton.setVisibility(View.INVISIBLE);
            }

            // Video
            videoFile = new File(currentSlideDirectory.getPath() + "/video.3gp");
            if (videoFile.exists()) {
                video = Uri.parse(currentSlideDirectory.getPath() + "/video.3gp");
            } else {
                videoView.setVideoURI(null);
                video = null;
                videoView.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                videoButton.setVisibility(View.INVISIBLE);
            }
        }

    }

    private void configureAudio() {
        // play button
        audioButton = findViewById(R.id.button_audio_learn);
        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio != null) {
                    audioPlayer = MediaPlayer.create(SlideLearningActivity.this, audio);
                    audioPlayer.start();
                }
            }
        });
    }


    private void configureVideoView() {
        videoView = findViewById(R.id.video_view_learn);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoButton.setVisibility(View.VISIBLE);
            }
        });
    }

    private void configureWatchVideoButton() {
        videoButton = findViewById(R.id.watch_video);
        videoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView != null) {
                    videoButton.setVisibility(View.INVISIBLE);
                    videoView.start();
                    videoView.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });
    }
}