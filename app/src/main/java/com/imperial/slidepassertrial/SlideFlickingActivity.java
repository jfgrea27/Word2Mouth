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
    private EditText titleEdit;

    // Instruction Buttons
    private EditText instructionsEdit;

    // Video
    private VideoView videoView;
    private Button videoPreview;
    private  Uri video = null;

    // Audio
    private Button Audio;


    // Model Variables
    private int slideCounter = 0;
    private int totalNumberSlides = 0;
    private String coursePath;

    // File Management
    private File currentSlideDirectory = null;
    private File titleFile = null;
    private File videoFile = null;
    private File instructionsFile = null;
    // Requests for File Management
    public static final int TITLE = 100;
    public static final int VIDEO = 101;
    public static final int INSTRUCTIONS = 102;
    public static final int AUDIO = 103;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_flicking);

        // get Extras
        coursePath = (String) getIntent().getExtras().get("course directory path");

        previousSlide = findViewById(R.id.button_previous);
        nextSlide = findViewById(R.id.button_next);
        configureSideTitleEdit();

        configurePreviousButton();
        configureNextButton();

        //Media Set Up
        configureVideoView();
        configureVideoPreview();
        configureAudio();
        configureInstructionsEdit();

    }

    private void configureInstructionsEdit() {
        instructionsEdit = findViewById(R.id.instructions);
    }

    private void configureSideTitleEdit() {
        titleEdit = findViewById(R.id.slide_title_edit);
    }

    private void configurePreviousButton() {
        previousSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            updateFileContent();
            slideCounter--;
            // reach first slide
            if (slideCounter < 0) {
                Toast.makeText(SlideFlickingActivity.this, "First Slide", Toast.LENGTH_SHORT).show();
                slideCounter++;
            }
            // retrieve previously saved file data
            else {
                Toast.makeText(SlideFlickingActivity.this, "Retrieve Previous Slide", Toast.LENGTH_SHORT).show();
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
                updateFileContent();
                slideCounter++;
                // creating a new Slide
                if(totalNumberSlides < slideCounter) {
                    Toast.makeText(SlideFlickingActivity.this, "Create New Slide", Toast.LENGTH_SHORT).show();
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
        // refresh video view
        videoView.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);

        if (video != null) {
            videoView.setVideoURI(video);
            videoPreview.setVisibility(View.VISIBLE);
        } else {
            videoPreview.setVisibility(View.INVISIBLE   );
        }

    }

    private void createBlankSlide() {
        // file
        currentSlideDirectory = null;
        // model
        totalNumberSlides++;

        // title
        titleEdit.setText("");

        // instructions
        instructionsEdit.setText("");

        // video
        video = null;
        videoPreview.setVisibility(View.INVISIBLE);
        videoView.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
    }

    private void retrieveSavedSlide() {

        currentSlideDirectory = DirectoryHandler.retrieveSlideDirectoryByNumber(coursePath, slideCounter);

        // title
        titleFile = new File(currentSlideDirectory.getPath() + "/title.txt");
        if(titleFile.exists()) {
            titleEdit.setText(readFromFile(getApplicationContext(),currentSlideDirectory.getPath() + "/title.txt"));

        }

        // instructions
        instructionsFile = new File(currentSlideDirectory.getPath() + "/instructions.txt");
        if (instructionsFile.exists()) {
            instructionsEdit.setText(readFromFile(getApplicationContext(),currentSlideDirectory.getPath() + "/instructions.txt"));
        }

        // video
        videoFile = new File(currentSlideDirectory.getPath() + "/video.3gp");
        if (videoFile.exists()) {
            video = Uri.parse(currentSlideDirectory.getPath() + "/video.3gp");
        } else {
            videoView.setVideoURI(null);
            video = null;
            videoView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            videoPreview.setVisibility(View.INVISIBLE);
        }
    }


    private void updateFileContent() {
        // Saving on Disk
        currentSlideDirectory = DirectoryHandler.createDirectoryForSlideAndReturnIt(coursePath, slideCounter);

        // Title
        String title = titleEdit.getText().toString();
        titleFile = DirectoryHandler.createFileForSlideContentAndReturnIt(currentSlideDirectory.getPath(), null, getContentResolver(), title, TITLE);

        // Instructions
        String instructions = instructionsEdit.getText().toString();
        instructionsFile = DirectoryHandler.createFileForSlideContentAndReturnIt(currentSlideDirectory.getPath(), null, getContentResolver(), instructions, INSTRUCTIONS);

        // Video
        if (video != null) {
            videoFile = DirectoryHandler.createFileForSlideContentAndReturnIt(currentSlideDirectory.getPath(), video, getContentResolver(), null, VIDEO);
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