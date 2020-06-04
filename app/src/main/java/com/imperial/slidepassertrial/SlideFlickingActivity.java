package com.imperial.slidepassertrial;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class SlideFlickingActivity extends AppCompatActivity implements VideoDialog.OnInputListener {

    // Video Choice
    public final int CAMERA_ROLL_SELECTION = 0;
    public final int GALLERY_SELECTION = 1;
    private final int MAX_DURATION_VIDEO = 10;

    // General Activity Buttons
    private Button previousSlide;
    private Button nextSlide;

    // Slide Title
    private EditText slideNameEditText;
    private TextView slideNameText;

    // Video
    private VideoView videoView;
    private Button videoPreview;
    private  Uri video = null;

    // Audio
    private Button Audio;

    // Instruction Buttons
    private TextView instructionsEdit;

    // Model Variables
    private int slideCounter = 0;
    private Course course;
    private String coursePath;

    // File Management
    private File currentSlideDirectory = null;
    private Slide currentSlide = null;
    private File videoFile = null;
    private File instructionsFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_flicking);

        // get Extras
        course = new Course((String) getIntent().getExtras().get("course name"));
        coursePath = (String) getIntent().getExtras().get("course directory path");

        previousSlide = findViewById(R.id.button_previous);
        nextSlide = findViewById(R.id.button_next);
        configureSlideNameEditTextAndText();

        configurePreviousButton();
        configureNextButton();

        //Media Set Up
        configureVideoView();
        configureVideoPreview();
        configureAudio();
        configureInstructions();

    }

    private void configureInstructions() {
        instructionsEdit = findViewById(R.id.instructions);
    }

    private void configureSlideNameEditTextAndText() {
        slideNameEditText = findViewById(R.id.slide_title_edit);
        slideNameText = findViewById(R.id.slide_title_text);
    }

    private void configurePreviousButton() {
        previousSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideCounter--;
                // reach first slide
                if (slideCounter < 0) {
                    Toast.makeText(SlideFlickingActivity.this, "First Slide", Toast.LENGTH_SHORT).show();
                    slideCounter++;
                }
                // retrieve previously saved file data
                else {
                    Toast.makeText(SlideFlickingActivity.this, "Retrieve Previous", Toast.LENGTH_SHORT).show();
                    retrieveSavedSlide();
                    updateCurrentView();
                }
            }
        });
    }

    private void configureNextButton() {
        nextSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideCounter++;
                // creating a new Slide
                if(course.size() < slideCounter) {
                    Toast.makeText(SlideFlickingActivity.this, "Create New Slide", Toast.LENGTH_SHORT).show();
                    saveCurrentSlide();
                    createBlankSlide();
                }
                // retrieve previously saved file data
                else {
                    Toast.makeText(SlideFlickingActivity.this, "Retrieve Next Slide", Toast.LENGTH_SHORT).show();
                    retrieveSavedSlide();
                    updateCurrentView();
                }
            }
        });

    }

    private void updateCurrentView() {
        slideNameText.setVisibility(View.VISIBLE);
        slideNameEditText.setVisibility(View.INVISIBLE);
        slideNameText.setText(currentSlide.getSlideName());
        videoView.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        videoView.setVideoURI(video);
        videoPreview.setVisibility(View.VISIBLE);

    }

    private void createBlankSlide() {
        videoPreview.setVisibility(View.INVISIBLE);
        videoView.setVideoURI(null);
        videoView.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        slideNameText.setVisibility(View.INVISIBLE);
        slideNameEditText.setVisibility(View.VISIBLE);
        slideNameEditText.setText("");
        instructionsEdit.setText("");
        currentSlideDirectory = null;
        currentSlide = null;
    }

    private void retrieveSavedSlide() {

        currentSlide = (Slide) course.retrieveByPosition(slideCounter);

        currentSlideDirectory = DirectoryHandler.retrieveSlideDirectoryByNumber(coursePath, slideCounter + 1, currentSlide.getSlideName());

        slideNameEditText.setText(currentSlide.getSlideName());

        String videoPath = currentSlideDirectory.getPath() + "/video.3gp";
        video = Uri.parse(videoPath);

        instructionsFile = new File(currentSlideDirectory.getPath() + "/instructions.txt");

        if (instructionsFile.exists()) {
            instructionsEdit.setText(readFromFile(getApplicationContext(),currentSlideDirectory.getPath() + "/instructions.txt"));

        }
    }

    private void saveCurrentSlide() {

        String slideName =  slideNameEditText.getText().toString();
        if (slideName.isEmpty()) {
            slideName = "Untitled Slide";
        }
        // Saving on Course Object
        currentSlide = new Slide(course.getCourseName(), slideName, slideCounter);
        course.addSlide(currentSlide);

        // Saving on Disk
        currentSlideDirectory = DirectoryHandler.createDirectoryForSlideAndReturnIt(coursePath, slideCounter, slideName, this);
        // Instructions
        String instructions = instructionsEdit.getText().toString();
        instructionsFile = DirectoryHandler.createInstructionFileAndReturnIt(currentSlideDirectory.getPath(), instructions);

        // Video
        try {
            if (video != null) {
                videoFile = DirectoryHandler.createVideoFileAndReturnIt(currentSlideDirectory.getPath(), video, getContentResolver());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void configureAudio() {

    }

    private void configureVideoView() {
        videoView = findViewById(R.id.videoview);
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                VideoDialog videoDialog = new VideoDialog();
                videoDialog.show(getSupportFragmentManager(), "Video Dialog");



//                Intent fetchVideo = new Intent(SlideFlickingActivity.this, VideoSelectionActivity.class);
//                startActivityForResult(fetchVideo, VIDEO_RESULT);
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoPreview.setVisibility(View.VISIBLE);
            }
        });
    }

    private void configureVideoPreview() {
        videoPreview = findViewById(R.id.preview_video);
        videoPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView != null) {
                    videoPreview.setVisibility(View.INVISIBLE);
                    videoView.start();
                }
            }
        });

    }

    private void setPreviewButton() {
        videoPreview.setVisibility(View.VISIBLE);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case GALLERY_SELECTION:
                case CAMERA_ROLL_SELECTION: {
                    Toast.makeText(this, "Video Returned ", Toast.LENGTH_LONG).show();
                    video = data.getData();
                    videoView.setVideoURI(video);
                    setPreviewButton();
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + requestCode);
            }
        }
    }




    @Override
    public void sendInput(int choice) {
        switch (choice) {
            case GALLERY_SELECTION: {
                Toast.makeText(this, "Opening Galleries", Toast.LENGTH_SHORT).show();
                Intent galleryIntent = new Intent();
                galleryIntent.setType("video/*");
                galleryIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                galleryIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, MAX_DURATION_VIDEO);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Video"), GALLERY_SELECTION);
                break;
            }
            case CAMERA_ROLL_SELECTION: {
                Toast.makeText(this, "Opening Camera Roll", Toast.LENGTH_SHORT).show();

                Intent rollIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                rollIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, MAX_DURATION_VIDEO);
                startActivityForResult(rollIntent, CAMERA_ROLL_SELECTION);
                break;
            }
        }
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