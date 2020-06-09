package com.imperial.slidepassertrial.learn.main.offline;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.imperial.slidepassertrial.R;
import com.imperial.slidepassertrial.teach.offline.DirectoryHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

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
        setContentView(R.layout.activity_slide_learning);

        coursePath = (String) getIntent().getExtras().get("course directory path");

        courseFolder = new File(coursePath);
        totalNumberSlides = courseFolder.listFiles().length;


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
                if(totalNumberSlides == slideCounter) {
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
        currentSlideDirectory = DirectoryHandler.retrieveSlideDirectoryByNumber(coursePath, slideCounter);

        // title
        titleFile = new File(currentSlideDirectory.getPath() + "/title.txt");
        if(titleFile.exists()) {
            String string = readFromFile(getApplicationContext(),currentSlideDirectory.getPath() + "/title.txt");
            if (!string.isEmpty()) {
                if (string.charAt(0) == '\n' && string.length() > 1) {
                    string = string.substring(1);
                }
            }
            titleView.setText(string);
        }

        // instructions
        instructionsFile = new File(currentSlideDirectory.getPath() + "/instructions.txt");
        if (instructionsFile.exists()) {
            String string = readFromFile(getApplicationContext(),currentSlideDirectory.getPath() + "/instructions.txt");
            if (!string.isEmpty()) {
                if (string.charAt(0) == '\n' && string.length() > 1) {
                    string = string.substring(1);
                }
            }
            instructionsView.setText(string);
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

    private String readFromFile(Context context, String filePath) {

        String ret = "";

        try {
            File file = new File(filePath);
            FileInputStream inputStream = new FileInputStream(file);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}