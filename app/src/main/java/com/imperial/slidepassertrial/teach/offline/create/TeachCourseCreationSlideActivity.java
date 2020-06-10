package com.imperial.slidepassertrial.teach.offline.create;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import com.imperial.slidepassertrial.R;
import com.imperial.slidepassertrial.shared.FileReader;
import com.imperial.slidepassertrial.teach.offline.DirectoryHandler;
import com.imperial.slidepassertrial.teach.offline.create.audio.AudioRecorder;
import com.imperial.slidepassertrial.teach.offline.create.video.ImageDialog;

import java.io.File;

public class TeachCourseCreationSlideActivity extends AppCompatActivity implements ImageDialog.OnInputListener {

    // General Activity Buttons
    private ImageButton previousSlide;
    private ImageButton nextSlide;

    // Title
    private EditText titleEdit;

    // Instructions
    private EditText instructionsEdit;

    // Video
    private VideoView videoView;
    private ImageButton videoPreview;
    private  Uri video = null;
    // Video Choice
    public final int CAMERA_ROLL_SELECTION = 0;
    public final int GALLERY_SELECTION = 1;
    private final int MAX_DURATION_VIDEO = 10;


    // Audio
    private ImageButton recordAudioButton;
    private ImageButton playAudioButton;
    private AudioRecorder recorder;
    private MediaPlayer audioPlayer;
    private Uri audio = null;

    // Model Variables
    private int slideCounter = 0;
    private int totalNumberSlides = 0;
    private String coursePath;

    // File Management
    private File currentSlideDirectory = null;
    private File titleFile = null;
    private File videoFile = null;
    private File audioFile = null;
    private File instructionsFile = null;
    // Requests for File Management
    public static final int TITLE = 100;
    public static final int VIDEO = 101;
    public static final int INSTRUCTIONS = 102;
    public static final int AUDIO = 103;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_course_slide_creation);

        // get Extras
        coursePath = (String) getIntent().getExtras().get("course directory path");

        // Forward and Backward Buttons
        configurePreviousButton();
        configureNextButton();

        // Title
        configureSideTitleEdit();

        // Instructions
        configureInstructionsEdit();

        // Video
        configureVideoView();
        configureVideoPreview();

        // Audio
        configureAudio();

        // First slide
        initialSetUp();
        retrieveSavedSlide();
    }

    private void configureInstructionsEdit() {
        instructionsEdit = findViewById(R.id.instructions);
    }

    private void configureSideTitleEdit() {
        titleEdit = findViewById(R.id.slide_title_edit);
    }

    private void configurePreviousButton() {
        previousSlide = findViewById(R.id.button_previous);
        previousSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            updateFileContent();
            slideCounter--;
            // reach first slide
            if (slideCounter < 0) {
                Toast.makeText(TeachCourseCreationSlideActivity.this, "First Slide", Toast.LENGTH_SHORT).show();
                slideCounter++;
            }
            // retrieve previously saved file data
            else {
                Toast.makeText(TeachCourseCreationSlideActivity.this, "Retrieve Previous Slide", Toast.LENGTH_SHORT).show();
                retrieveSavedSlide();
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
                updateFileContent();
                slideCounter++;
                // creating a new Slide
                if(totalNumberSlides < slideCounter) {
                    Toast.makeText(TeachCourseCreationSlideActivity.this, "Create New Slide", Toast.LENGTH_SHORT).show();
                    createBlankSlide();
                }
                // retrieve previously saved file data
                else {
                    Toast.makeText(TeachCourseCreationSlideActivity.this, "Retrieve Next Slide", Toast.LENGTH_SHORT).show();
                    retrieveSavedSlide();
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
            videoPreview.setVisibility(View.VISIBLE);
        } else {
            videoPreview.setVisibility(View.INVISIBLE   );
        }

        if (audio != null) {
            playAudioButton.setVisibility(View.VISIBLE);
        } else {
            playAudioButton.setVisibility(View.INVISIBLE);
        }

    }

    private void createBlankSlide() {
        // file
        currentSlideDirectory = null;
        // model
        totalNumberSlides++;

        // title
        titleEdit.setText(null);

        // instructions
        instructionsEdit.setText(null);

        // audio
        audio = null;
        playAudioButton.setVisibility(View.INVISIBLE);

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
            String text = FileReader.readTextFromFile(currentSlideDirectory.getPath() + "/title.txt");
            if (text.isEmpty()) {
                titleEdit.setText("");
            } else {
                titleEdit.setText(text);
                titleEdit.setSelection(instructionsEdit.length());
            }
        }

        // instructions
        instructionsFile = new File(currentSlideDirectory.getPath() + "/instructions.txt");
        if (instructionsFile.exists()) {
            String text = FileReader.readTextFromFile(currentSlideDirectory.getPath() + "/instructions.txt");
            if (text.isEmpty()) {
                instructionsEdit.setText("");
            } else {
                instructionsEdit.setText(text);
                instructionsEdit.setSelection(instructionsEdit.length());
            }
        }

        // Audio
        audioFile = new File(currentSlideDirectory.getPath() + "/audio.3gp");
        if (audioFile.exists()) {
            audio = Uri.parse(currentSlideDirectory.getPath() + "/audio.3gp");
            playAudioButton.setVisibility(View.VISIBLE);
        } else {
            audio = null;
            playAudioButton.setVisibility(View.INVISIBLE);
        }

        // Video
        videoFile = new File(currentSlideDirectory.getPath() + "/video.3gp");
        if (videoFile.exists()) {
            video = Uri.parse(currentSlideDirectory.getPath() + "/video.3gp");
            videoView.setVideoURI(video);
            videoPreview.setVisibility(View.VISIBLE);
        } else {
            videoView.setVideoURI(null);
            video = null;
            videoView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);
            videoPreview.setVisibility(View.INVISIBLE);
        }
    }

    private void initialSetUp() {
        // Saving on Disk
        currentSlideDirectory = DirectoryHandler.createDirectoryForSlideAndReturnIt(coursePath, slideCounter);

        // Audio
        audioFile = DirectoryHandler.createFileForSlideContentAndReturnIt(coursePath + "/" + slideCounter, null, getContentResolver(), null, AUDIO);

        // Video
        if (video != null) {
            videoFile = DirectoryHandler.createFileForSlideContentAndReturnIt(currentSlideDirectory.getPath(), video, getContentResolver(), null, VIDEO);
        }
    }

    private void updateFileContent() {
        // Saving on Disk
        currentSlideDirectory = DirectoryHandler.createDirectoryForSlideAndReturnIt(coursePath, slideCounter);

        // Title
        String title = titleEdit.getText().toString();
        if (title.equals("")) {
            title = "Untitled Slide";
        }
        titleFile = DirectoryHandler.createFileForSlideContentAndReturnIt(currentSlideDirectory.getPath(), null, getContentResolver(), title, TITLE);

        // Instructions
        String instructions = instructionsEdit.getText().toString();
        instructionsFile = DirectoryHandler.createFileForSlideContentAndReturnIt(currentSlideDirectory.getPath(), null, getContentResolver(), instructions, INSTRUCTIONS);

        // Audio
        audioFile = DirectoryHandler.createFileForSlideContentAndReturnIt(coursePath + "/" + slideCounter, null, getContentResolver(), null, AUDIO);

        // Video
        if (video != null) {
            videoFile = DirectoryHandler.createFileForSlideContentAndReturnIt(currentSlideDirectory.getPath(), video, getContentResolver(), null, VIDEO);
        }
    }


    private void configureAudio() {
        recorder = new AudioRecorder();

        // play button
        playAudioButton = findViewById(R.id.button_play_audio);
        // record button
        recordAudioButton = findViewById(R.id.button_audio);
        recordAudioButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Toast.makeText(TeachCourseCreationSlideActivity.this, "Start Recording", Toast.LENGTH_SHORT).show();
                        recorder.startRecording(audioFile.getPath());
                        return true;
                    case MotionEvent.ACTION_UP:
                        Toast.makeText(TeachCourseCreationSlideActivity.this, "Stop Recording", Toast.LENGTH_SHORT).show();
                        recorder.stopRecording();
                        audio = Uri.fromFile(audioFile);
                        playAudioButton.setVisibility(View.VISIBLE);
                        break;
                }
                return false;
            }
        });


        playAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio != null) {
                    audioPlayer = MediaPlayer.create(TeachCourseCreationSlideActivity.this, audio);
                    audioPlayer.start();
                }
            }
        });
    }

    private void configureVideoView() {
        videoView = findViewById(R.id.videoview);
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageDialog imageDialog = new ImageDialog();
                imageDialog.show(getSupportFragmentManager(), "Video Dialog");

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
                    videoView.setBackgroundColor(Color.TRANSPARENT);
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


}