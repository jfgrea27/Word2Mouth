package com.imperial.word2mouth.teach.offline.create;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
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

import com.imperial.word2mouth.shared.IntentNames;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.shared.FileReaderHelper;
import com.imperial.word2mouth.shared.FileHandler;
import com.imperial.word2mouth.teach.offline.create.audio.AudioRecorder;
import com.imperial.word2mouth.teach.offline.create.video.ImageDialog;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class TeachCourseCreationSummaryActivity extends AppCompatActivity implements ImageDialog.OnInputListener  {

    // Permissions
    private final int CAMERA_PERMISSION = 1;
    private final int AUDIO_RECORDING_PERMISSION = 2;
    private final int READ_WRITE_PERMISSION = 3;

    private boolean hasInternetAccess = false;
    private boolean hasReadWriteStorageAccess = false;
    private boolean hasAudioRecordingPermission = false;
    private boolean hasCameraPermission = false;


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

    private ArrayList<String> localSlides = null;
    private ArrayAdapterSlideName adapter = null;
    private boolean selectedSlide = false;

    // File
    private static final int TITLE = 100;
    private File metaDirectory = null;
    private File slideDirectory = null;
    private File audioFile = null;
    private String courseDirectoryPath = null;
    private int numberOfSlides = 0;

    private Uri imageUri = null;
    private Uri audioUri = null;

    public static final int AUDIO = 103;

    private static final int IMAGE = 104;

    private int slideNumber = -1;

    // Bottom View Button
    private ImageButton delete = null;
    private ImageButton create = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_course_summary_creation);

        // get Intents
        courseName = (String) getIntent().getExtras().get("course name");
        courseDirectoryPath = (String) getIntent().getExtras().get("course directory path");

        // Permissions
        getPermissions();

        if (hasReadWriteStorageAccess) {
            // File
            metaDirectory = FileHandler.createDirectoryAndReturnIt(courseDirectoryPath, FileHandler.META);
            slideDirectory = FileHandler.createDirectoryAndReturnIt(courseDirectoryPath, FileHandler.SLIDES);
            // Creating audioFile
            audioFile = FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), null, getContentResolver(), null, AUDIO );
            FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), null, getContentResolver(), courseName, TITLE);


            // List of Slides
            configureListViewSlides();
            configureCreateButton();
            configureDeleteButton();

        } else {
            finish();
        }


        // Name
        configureCourseName();

        // Thumbnail
        configureCourseThumbnail();

        // Audio
        configureAudio();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveMetaData();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Permissions

    private void getPermissions() {
        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED )) {
            Toast.makeText(this, "Please allow access to Storage", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, READ_WRITE_PERMISSION);
        } else{
            hasReadWriteStorageAccess = true;
        }

        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)){
            Toast.makeText(this, "Please allow access to Audio", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_RECORDING_PERMISSION);
        } else{
            hasAudioRecordingPermission = true;
        }

        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "Please allow access to Camera", Toast.LENGTH_SHORT).show();
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

    // List of Slides
    private void configureListViewSlides() {
        slides = findViewById(R.id.slide_list_view);

        localSlides = retrieveLocalSlides();

        if (localSlides.size() > 0) {
            adapter = new ArrayAdapterSlideName(TeachCourseCreationSummaryActivity.this, R.layout.list_slide, localSlides);
            slides.setAdapter(adapter);
        }

        slides.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                for (int i = 0; i < adapter.getCount(); i++) {
                    View item = slides.getChildAt(i);
                        if (item != null) {
                            item.setBackgroundColor(Color.WHITE);
                        }
                }

                if (selectedSlide) {
                    view.setBackgroundColor(Color.WHITE);
                    selectedSlide = false;
                    if (delete != null) {
                        delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    }
                    slideNumber = -1;

                } else {
                    selectedSlide = true;

                    slideNumber = position;
                    view.setBackgroundColor(Color.LTGRAY);
                    if (delete != null) {
                        delete.setColorFilter(null);
                    }
                 }
            }
        });

    }

    private ArrayList<String> retrieveLocalSlides() {

        ArrayList<String> slideNames = new ArrayList<>();

        File[] slidesFiles = slideDirectory.listFiles();
        numberOfSlides = slidesFiles.length;

        for (File f : slidesFiles) {

            String slideName;
            slideName = FileReaderHelper.readTextFromFile(f.getPath()+ "/title.txt");

            slideNames.add(slideName);
        }
        return slideNames;

    }
    // Audio Button

    private void configureAudio() {
        recorder = new AudioRecorder();
        // play button
        audioPreview = findViewById(R.id.course_audio_play);
        audioButton = findViewById(R.id.list_audio_button);

        audioButton.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (hasAudioRecordingPermission) {
                    switch (event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            Toast.makeText(TeachCourseCreationSummaryActivity.this, "Start Recording", Toast.LENGTH_SHORT).show();
                            audioButton.setColorFilter(Color.RED);
                            recorder.startRecording(audioFile.getPath());
                            return true;
                        case MotionEvent.ACTION_UP:
                            Toast.makeText(TeachCourseCreationSummaryActivity.this, "Stop Recording", Toast.LENGTH_SHORT).show();
                            recorder.stopRecording();
                            audioButton.setColorFilter(Color.BLACK);
                            audioUri = Uri.fromFile(audioFile);
                            audioPreview.setVisibility(View.VISIBLE);
                            break;
                    }
                } else {
                    Toast.makeText(TeachCourseCreationSummaryActivity.this, "Need the Microphone Permission", Toast.LENGTH_SHORT).show();
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

    // Thumbnail
    private void configureCourseThumbnail() {
        thumbnail = findViewById(R.id.list_item_thumbnail);

        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasCameraPermission) {
                    ImageDialog imageDialog = new ImageDialog(ImageDialog.THUMBNAIL);
                    imageDialog.show(getSupportFragmentManager(), "Video Dialog");
                } else {
                    Toast.makeText(TeachCourseCreationSummaryActivity.this, "Need the Camera Permission", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case GALLERY_SELECTION:
                    imageUri= data.getData();
                    if (imageUri != null) {
                        FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), imageUri, getContentResolver(), null, IMAGE);
                        thumbnail.setImageURI(imageUri);
                    }
                    break;

                case CAMERA_ROLL_SELECTION: {
                    Bitmap bitMapImage = null;
                    bitMapImage = (Bitmap) data.getExtras().get("data");

                    imageUri = Uri.fromFile(new File(metaDirectory + "/thumbnail.jpg"));

                    if (imageUri != null) {
                        try {
                            FileOutputStream out = new FileOutputStream(metaDirectory + "/thumbnail.jpg");
                            bitMapImage.compress(Bitmap.CompressFormat.JPEG, 100, out);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        if (bitMapImage != null) {
                            thumbnail.setImageBitmap(bitMapImage);
                        }
                        break;
                    }
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


    // Create Button

    private void configureCreateButton() {
        create = findViewById(R.id.edit_button);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMetaData();

                Intent createEditIntent = new Intent(TeachCourseCreationSummaryActivity.this, TeachCourseCreationSlideActivity.class);
                // starts at 0
                if (slideNumber > -1) {
                    createEditIntent.putExtra("slide number", slideNumber);
                } else {
                    createEditIntent.putExtra("slide number", 0);

                }
                // meta not included
                createEditIntent.putExtra(IntentNames.COURSE_PATH, courseDirectoryPath);
                createEditIntent.putExtra("number of slides", numberOfSlides);
                startActivity(createEditIntent);
            }
        });
    }


    // Delete Button
    private void configureDeleteButton() {
        delete = findViewById(R.id.delete_button);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectedSlide) {
                    if (slideNumber > -1) {
                        int currentItem = slideNumber;
                        int nextItem = currentItem + 1;


                        adapter.remove(adapter.getItem(currentItem));
                        adapter.notifyDataSetChanged();

                        // remove from File System
                        File currentItemFile = null;
                        File nextItemFile = null;

                        while (nextItem < numberOfSlides) {

                            currentItemFile = new File(slideDirectory.getPath() + "/" + currentItem);
                            nextItemFile = new File(slideDirectory.getPath() + "/" + nextItem);



                            try {
                                FileUtils.copyDirectory(nextItemFile, currentItemFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

//                        localSlides.set(currentItem, localSlides.get(nextItem));

                            currentItem++;
                            nextItem++;
                        }

                        currentItemFile = new File(slideDirectory.getPath() + "/" + currentItem);

                        if (currentItemFile.exists()) {
                            FileHandler.deleteRecursive(currentItemFile);

                        }
                    }

                    selectedSlide = false;
                    slideNumber = -1;
                    delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                }

            }
        });


        if (delete != null) {
            delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        }
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////

    // Model

    private void configureCourseName() {
        name = findViewById(R.id.list_item_text);
        name.setText(courseName);
    }


    // Saving Data for Course Selection
    private void saveMetaData() {
        if (imageUri != null) {
            FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), imageUri, getContentResolver(), null, IMAGE);
        }

        if (audioUri != null) {
            FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), audioUri, getContentResolver(), null, AUDIO);
        }

        FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), null, null, courseName, TITLE);

    }







}