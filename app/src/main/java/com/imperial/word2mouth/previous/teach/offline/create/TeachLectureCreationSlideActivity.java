package com.imperial.word2mouth.previous.teach.offline.create;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.VideoView;

import com.imperial.word2mouth.IntentNames;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.previous.shared.FileReaderHelper;
import com.imperial.word2mouth.previous.shared.FileHandler;
import com.imperial.word2mouth.create.AudioRecorder;
import com.imperial.word2mouth.previous.teach.offline.create.video.ImageDialog;

import java.io.File;
import java.util.Locale;

public class    TeachLectureCreationSlideActivity extends AppCompatActivity implements ImageDialog.OnInputListener {

    // Permissions
    private final int CAMERA_PERMISSION = 1;
    private final int AUDIO_RECORDING_PERMISSION = 2;
    private final int READ_WRITE_PERMISSION = 3;

    private boolean hasInternetAccess = false;
    private boolean hasReadWriteStorageAccess = false;
    private boolean hasAudioRecordingPermission = false;
    private boolean hasCameraPermission = false;


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
    private TextToSpeech textToSpeech;
    private boolean recording = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_course_slide_creation);

        // get Intents
        coursePath = (String) getIntent().getExtras().get(IntentNames.COURSE_PATH);
        totalNumberSlides = (int) getIntent().getExtras().get("number of slides");
        try {
            slideCounter = (int) getIntent().getExtras().get("slide number");
        } catch (Exception e) {

        }

        getPermissions();

        // Text
        if (hasReadWriteStorageAccess) {

            // Forward and Backward Buttons
            configurePreviousButton();
            configureNextButton();
            // Title
            configureSideTitleEdit();

            // Instructions
            configureInstructionsEdit();
        } else {
            finish();
        }

        // Video
        configureVideoView();
        configureVideoPreview();

        // Audio
        configureAudio();



        // First slide
        if (slideCounter == 0) {
            initialSetUp();
        }

        retrieveSavedSlide();
        configureTextToSpeech();
        configureLongClicks();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Permissions

    private void getPermissions() {
        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED )) {
            Toast.makeText(this, R.string.accessStorage, Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, READ_WRITE_PERMISSION);
        } else{
            hasReadWriteStorageAccess = true;
        }

        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, R.string.accesAudio, Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_RECORDING_PERMISSION);
        } else{
            hasAudioRecordingPermission = true;
        }

        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) ){
            Toast.makeText(this, R.string.accessCamera, Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        } else{
            hasCameraPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_WRITE_PERMISSION) {
            if(permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    permissions[1].equals(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasReadWriteStorageAccess = true;
            }
        }

        if (requestCode == AUDIO_RECORDING_PERMISSION) {
            if(permissions[0].equals(Manifest.permission.RECORD_AUDIO) && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                hasAudioRecordingPermission = true;
            }
        }

        if (requestCode == CAMERA_PERMISSION) {
            if(permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasCameraPermission = true;
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////


    // UI

    private void configurePreviousButton() {
        previousSlide = findViewById(R.id.button_previous);
        previousSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            updateFileContent();
            slideCounter--;
            // reach first slide
            if (slideCounter < 0) {
                Toast.makeText(TeachLectureCreationSlideActivity.this, R.string.firstSlide, Toast.LENGTH_SHORT).show();
                slideCounter++;
            }
            // retrieve previously saved file data
            else {
                Toast.makeText(TeachLectureCreationSlideActivity.this, R.string.retrievingPreviousSlide, Toast.LENGTH_SHORT).show();
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
                if(totalNumberSlides <= slideCounter) {
                    Toast.makeText(TeachLectureCreationSlideActivity.this, R.string.createNewSlide, Toast.LENGTH_SHORT).show();
                    createBlankSlide();
                }
                // retrieve previously saved file data
                else {
                    Toast.makeText(TeachLectureCreationSlideActivity.this, R.string.retrievingNextSlide, Toast.LENGTH_SHORT).show();
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

        currentSlideDirectory = FileHandler.retrieveSlideDirectoryByNumber(coursePath, slideCounter);

        // title
        titleFile = new File(currentSlideDirectory.getPath() + "/title.txt");
        if(titleFile.exists()) {
            String text = FileReaderHelper.readTextFromFile(currentSlideDirectory.getPath() + "/title.txt");
            if (text.isEmpty()) {
                titleEdit.setText("");
            } else {
                titleEdit.setText(text);
                titleEdit.setSelection(titleEdit.length());
            }
        }

        // instructions
        instructionsFile = new File(currentSlideDirectory.getPath() + "/instructions.txt");
        if (instructionsFile.exists()) {
            String text = FileReaderHelper.readTextFromFile(currentSlideDirectory.getPath() + "/instructions.txt");
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



    // Text Title
    private void configureSideTitleEdit() {
        titleEdit = findViewById(R.id.slide_title_edit);
    }

    // Text Instructions
    private void configureInstructionsEdit() {
        instructionsEdit = findViewById(R.id.instructions);
    }


    // Audio
    private void configureAudio() {
        recorder = new AudioRecorder();

        // play button
        playAudioButton = findViewById(R.id.button_play_audio);
        // record button
        recordAudioButton = findViewById(R.id.button_audio);

        recordAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasAudioRecordingPermission) {
                    if (!recording) {
                        recordAudioButton.setColorFilter(Color.RED);
                        Toast.makeText(TeachLectureCreationSlideActivity.this, R.string.startRecording, Toast.LENGTH_SHORT).show();
                        recorder.startRecording(audioFile.getPath());
                        recording = true;
                    } else {
                        Toast.makeText(TeachLectureCreationSlideActivity.this, R.string.stopRecording, Toast.LENGTH_SHORT).show();
                        recorder.stopRecording();
                        recordAudioButton.setColorFilter(null);

                        audio = Uri.fromFile(audioFile);
                        playAudioButton.setVisibility(View.VISIBLE);
                        recording = false;
                    }
                } else {
                    Toast.makeText(TeachLectureCreationSlideActivity.this, R.string.accesAudio, Toast.LENGTH_SHORT).show();
                }

            }
        });


        playAudioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio != null) {
                    audioPlayer = MediaPlayer.create(TeachLectureCreationSlideActivity.this, audio);
                    audioPlayer.start();
                }
            }
        });
    }



    // Video
    private void configureVideoView() {
        videoView = findViewById(R.id.videoview);
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hasCameraPermission) {
                    ImageDialog imageDialog = new ImageDialog(ImageDialog.SLIDE);
                    imageDialog.show(getSupportFragmentManager(), "Video Dialog");
                } else {
                    Toast.makeText(TeachLectureCreationSlideActivity.this, R.string.accessCamera, Toast.LENGTH_SHORT).show();

                }

            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoPreview.setVisibility(View.VISIBLE);
            }
        });
    }

    // Video preview
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
                Intent galleryIntent = new Intent();
                galleryIntent.setType("video/*");
                galleryIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                galleryIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, MAX_DURATION_VIDEO);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Video"), GALLERY_SELECTION);
                break;
            }
            case CAMERA_ROLL_SELECTION: {
                Intent rollIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                rollIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, MAX_DURATION_VIDEO);
                startActivityForResult(rollIntent, CAMERA_ROLL_SELECTION);
                break;
            }
        }
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Model
    private void initialSetUp() {
        // Saving on Disk
        currentSlideDirectory = FileHandler.createDirectoryForSlideAndReturnIt(coursePath, slideCounter);

        // Audio
        audioFile = FileHandler.createFileForSlideContentAndReturnIt(currentSlideDirectory.getPath(), null, getContentResolver(), null, AUDIO);

        // Video
        if (video != null) {
            videoFile = FileHandler.createFileForSlideContentAndReturnIt(currentSlideDirectory.getPath(), video, getContentResolver(), null, VIDEO);
        }
    }

    private void updateFileContent() {
        // Saving on Disk
        currentSlideDirectory = FileHandler.createDirectoryForSlideAndReturnIt(coursePath, slideCounter);

        // Title
        String title = titleEdit.getText().toString();
        if (title.equals("")) {
            title = "Untitled Slide";
        }
        titleFile = FileHandler.createFileForSlideContentAndReturnIt(currentSlideDirectory.getPath(), null, getContentResolver(), title, TITLE);

        // Instructions
        String instructions = instructionsEdit.getText().toString();
        instructionsFile = FileHandler.createFileForSlideContentAndReturnIt(currentSlideDirectory.getPath(), null, getContentResolver(), instructions, INSTRUCTIONS);

        // Audio
        audioFile = FileHandler.createFileForSlideContentAndReturnIt(currentSlideDirectory.getPath(), null, getContentResolver(), null, AUDIO);

        // Video
        if (video != null) {
            videoFile = FileHandler.createFileForSlideContentAndReturnIt(currentSlideDirectory.getPath(), video, getContentResolver(), null, VIDEO);
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////


    public void speak(String string) {
        textToSpeech.speak(string, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void configureTextToSpeech() {
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(TeachLectureCreationSlideActivity.this, R.string.languageNotSupported, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TeachLectureCreationSlideActivity.this, R.string.initializationFailedSST, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////


    public void configureLongClicks() {
        nextSlide.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.nextSlide));
                return true;
            }
        });

        previousSlide.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.previousSLide));
                return true;
            }
        });
        recordAudioButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.recordWithMicrophone));
                return true;
            }
        });


    }



}