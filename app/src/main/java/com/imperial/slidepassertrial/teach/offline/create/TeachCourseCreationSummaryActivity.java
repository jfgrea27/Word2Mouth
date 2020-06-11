package com.imperial.slidepassertrial.teach.offline.create;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.imperial.slidepassertrial.R;
import com.imperial.slidepassertrial.shared.FileReader;
import com.imperial.slidepassertrial.teach.offline.FileHandler;
import com.imperial.slidepassertrial.teach.offline.create.audio.AudioRecorder;
import com.imperial.slidepassertrial.teach.offline.create.video.ImageDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class TeachCourseCreationSummaryActivity extends AppCompatActivity implements ImageDialog.OnInputListener  {

    // Video Choice
    public final int CAMERA_ROLL_SELECTION = 0;
    public final int GALLERY_SELECTION = 1;

    // Thumbnail Image
    private ImageButton thumbnail = null;

    // Audio
    private ImageButton audioButton = null;
    private ImageButton audioPreview = null;
    // Player and Recorder
    private MediaPlayer player = null;
    private AudioRecorder recorder = null;

    // Text Course Name
    private TextView name = null;
    private String courseName = null;

    // List View of Slides
    private ListView slides = null;

    // Create Button
    private ImageButton create = null;

    // File
    private File metaDirectory = null;
    private File audioFile = null;
    private String courseDirectoryPath = null;
    private int numberOfSlides = 0;

    private Uri imageUri = null;
    private Uri audioUri = null;


    public static final int AUDIO = 103;
    private static final int IMAGE = 104;
    private static final int TITLE = 100;



    private ArrayList<String> localSlides;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_course_summary_creation);

        // get Intents
        courseName = (String) getIntent().getExtras().get("course name");
        courseDirectoryPath = (String) getIntent().getExtras().get("course directory path");

        // File
        metaDirectory = FileHandler.createDirectoryForMetaData(courseDirectoryPath);
        // Creating audioFile
        audioFile = FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), null, getContentResolver(), null, AUDIO );
        FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), null, getContentResolver(), courseName, TITLE);

        // Name
        configureCourseName();

        // Thumbnail
        configureCourseThumbnail();

        // Audio
        configureAudio();

        // List of Slides
        configureListViewSlides();

        configureCreateButton();
    }

    private void configureListViewSlides() {
        slides = findViewById(R.id.slide_list_view);

        localSlides = retrieveLocalSlides();

        slides.setAdapter(new ArrayAdapterSlideName(TeachCourseCreationSummaryActivity.this, R.layout.list_slide, localSlides));
        slides.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(TeachCourseCreationSummaryActivity.this, "Slide number" + position, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private ArrayList<String> retrieveLocalSlides() {

        ArrayList<String> slideNames = new ArrayList<>();

        File directory = new File(courseDirectoryPath);

        File[] slidesFiles = directory.listFiles();
        numberOfSlides = slidesFiles.length;

        for (File f : slidesFiles) {
            String slideName;
            if (f.getName().equals("meta")) {
                slideName = "Meta Files";
            } else {
                slideName = FileReader.readTextFromFile(f.getPath()+ "/title.txt");
            }

            slideNames.add(slideName);
        }
        return slideNames;

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
    }

    private void configureCourseName() {
        name = findViewById(R.id.list_item_text);
        name.setText(courseName);
    }


    private void configureAudio() {
        recorder = new AudioRecorder();
        // play button
        audioPreview = findViewById(R.id.course_audio_play);
        audioButton = findViewById(R.id.list_audio_button);
        audioButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Toast.makeText(TeachCourseCreationSummaryActivity.this, "Start Recording", Toast.LENGTH_SHORT).show();
                        recorder.startRecording(audioFile.getPath());
                        return true;
                    case MotionEvent.ACTION_UP:
                        Toast.makeText(TeachCourseCreationSummaryActivity.this, "Stop Recording", Toast.LENGTH_SHORT).show();
                        recorder.stopRecording();
                        audioUri = Uri.fromFile(audioFile);
                        audioPreview.setVisibility(View.VISIBLE);
                        break;
                }
                return false;
            }
        });

        audioPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioUri != null) {
                     player = MediaPlayer.create(TeachCourseCreationSummaryActivity.this, audioUri);
                    player.start();
                }
            }
        });


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveMetaData();
    }

    private void configureCourseThumbnail() {
        thumbnail = findViewById(R.id.list_item_thumbnail);
        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageDialog imageDialog = new ImageDialog();
                imageDialog.show(getSupportFragmentManager(), "Video Dialog");

            }
        });
    }

    private void configureCreateButton() {
        create = findViewById(R.id.course_create_button);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMetaData();
                Intent createFirstSlideIntent = new Intent(TeachCourseCreationSummaryActivity.this, TeachCourseCreationSlideActivity.class);
                createFirstSlideIntent.putExtra("course directory path", courseDirectoryPath);
                // take into account the meta file
                createFirstSlideIntent.putExtra("number of slides", numberOfSlides - 1);
                startActivity(createFirstSlideIntent);
            }
        });
    }

    private void saveMetaData() {
        if (imageUri != null) {
            FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), imageUri, getContentResolver(), null, IMAGE);
        }

        if (audioUri != null) {
            FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), audioUri, getContentResolver(), null, AUDIO);
        }

        FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), null, null, courseName, TITLE);

    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitMapImage = null;
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case GALLERY_SELECTION:
                    Toast.makeText(this, "Image Returned ", Toast.LENGTH_SHORT).show();
                    imageUri= data.getData();
                    if (imageUri != null) {
                        InputStream imageStream = null;
                        try {
                            imageStream = getContentResolver().openInputStream(imageUri);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        bitMapImage = BitmapFactory.decodeStream(imageStream);

                        FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), imageUri, getContentResolver(), null, IMAGE);
                    }
                    if (bitMapImage != null) {
                        thumbnail.setImageBitmap(bitMapImage);
                    }
                    break;

                case CAMERA_ROLL_SELECTION: {
                    imageUri = data.getData();
                    bitMapImage = null;
                    if (imageUri != null) {
                        InputStream imageStream = null;
                        try {
                            imageStream = getContentResolver().openInputStream(imageUri);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        bitMapImage = BitmapFactory.decodeStream(imageStream);

                        FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), imageUri, getContentResolver(), null, IMAGE);
                    }
                    if (bitMapImage != null) {
                        thumbnail.setImageBitmap(bitMapImage);
                    }
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
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"), GALLERY_SELECTION);
                break;
            }
            case CAMERA_ROLL_SELECTION: {
                Toast.makeText(this, "Opening Camera Roll", Toast.LENGTH_SHORT).show();
                Intent rollIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(rollIntent, CAMERA_ROLL_SELECTION);
                break;
            }
        }
    }


}