
package com.imperial.word2mouth.create;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.imperial.word2mouth.common.tags.AppCodes;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.common.adapters.SlideItemAdapter;
import com.imperial.word2mouth.common.audio.AudioRecorder;
import com.imperial.word2mouth.common.tts.SpeakIcon;
import com.imperial.word2mouth.model.LectureItem;
import com.imperial.word2mouth.model.SlideItem;
import com.imperial.word2mouth.previous.shared.FileHandler;
import com.imperial.word2mouth.previous.shared.FileReaderHelper;
import com.imperial.word2mouth.helpers.FileSystemConstants;
import com.imperial.word2mouth.common.tags.IntentNames;
import com.imperial.word2mouth.previous.shared.TopicItem;
import com.imperial.word2mouth.previous.shared.adapters.ArrayAdapterSlideName;
import com.imperial.word2mouth.previous.teach.offline.create.TeachLectureCreationSlideActivity;
import com.imperial.word2mouth.previous.teach.offline.create.video.ImageDialog;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class LectureSummaryCreateActivity extends AppCompatActivity implements ImageDialog.OnInputListener  {


    //////////////////////////////////////////////////////////////////////////////////////////////
    // Data Members
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////// Create Buttons //////////////////////
    private ImageButton createButton;

    /////////////////////// TTS //////////////////////
    private SpeakIcon textToSpeech;

    /////////////////////// RecycleView  //////////////////////
    private int selectedContent = -1;
    private RecyclerView slideRecycleView;
    private SlideItemAdapter slideItemAdapter;
    private ArrayList<LectureItem> lectureItems;

    //////////////////Image Thumbnail////////////////////////////
    private ImageView photoThumbnail;
    private Uri imageUri = null;

    //////////////////Sound Thumbnail////////////////////////////
    // View
    private ImageButton audioPreview;
    private ImageButton audioButton;
    // Model
    private File audioFile;
    // Controller
    private Uri audioUri = null;
    private AudioRecorder recorder;
    private MediaPlayer player;
    private boolean recording = false;

    //////////////////Lecture Title////////////////////////////
    private TextView lectureTitleView;

    //////////////////Model////////////////////////////
    // Extras
    private LectureItem lectureItem;
    // Create Slide


    // Thumbnail Image
    private ImageButton thumbnail = null;


    // Text Course Name
    private TextView name = null;
    private String lectureName = null;

    // List View of Slides
    private ListView slides = null;

    //private ArrayList<SlideItem> slides = null;
    private ArrayAdapterSlideName adapter = null;
    private boolean selectedSlide = false;

    // File
    private static final int TITLE = 100;
    private File metaDirectory = null;
    private File slideDirectory = null;
    private String lecturePath = null;
    private int numberOfSlides = 0;

//    private Uri imageUri = null;
//    private Uri audioUri = null;

    public static final int AUDIO = 103;

    private static final int IMAGE = 104;

    private int slideNumber = -1;

    // Bottom View Button
    private ImageButton delete = null;
    private ImageButton create = null;
    private String courseUID;
    private TopicItem topicItem;
    private String lectureBluetoothUUID;
    private String courseBluetoothUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_lecture_summary);

        getExtras();

        configureCreateButton();
        //            configureListViewSlides();
//            configureDeleteButton();
//            configureLectureName();
//            configureLectureThumbnail();
//            configureAudio();

//            configureTTS();
//            configureLongClicks();

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Get Extras
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void getExtras() {
        lectureItem = (LectureItem) getIntent().getExtras().get(IntentNames.LECTURE);

    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Create Button
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private void configureCreateButton() {
        create = findViewById(R.id.edit_button);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createEditIntent = new Intent(LectureSummaryCreateActivity.this, TeachLectureCreationSlideActivity.class);
                // starts at 0
                if (slideNumber > -1) {
                    createEditIntent.putExtra("slide", slideNumber);
                } else {
                    createEditIntent.putExtra("slide number", 0);

                }
                // meta not included
                createEditIntent.putExtra(IntentNames.COURSE_PATH, lecturePath);
                createEditIntent.putExtra("number of slides", numberOfSlides);
                startActivity(createEditIntent);
            }
        });
    }


    private void fileCreation() {

        // File
        metaDirectory = FileHandler.createDirectoryAndReturnIt(lecturePath, FileHandler.META);
        slideDirectory = FileHandler.createDirectoryAndReturnIt(lecturePath, FileHandler.SLIDES);
        // Creating audioFile
        audioFile = FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), null, getContentResolver(), null, AUDIO );


        String lectureBluetooth = FileReaderHelper.readTextFromFile(metaDirectory.getAbsolutePath() + FileSystemConstants.lectureBluetooth);

        if (lectureBluetooth.isEmpty()) {
            lectureBluetoothUUID = UUID.randomUUID().toString();
            FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), null, getContentResolver(), lectureBluetoothUUID, FileHandler.LECTURE_UUID_BLUETOOTH );

        } else {
            lectureBluetoothUUID = lectureBluetooth;
        }

        String courseBluetooth = FileReaderHelper.readTextFromFile(metaDirectory.getAbsolutePath() + FileSystemConstants.courseBluetooth);

        if (courseBluetooth.isEmpty()) {
            courseBluetoothUUID = FileReaderHelper.readTextFromFile(metaDirectory.getAbsolutePath() + FileSystemConstants.courseBluetooth);
        }

        // Bluetooth purposes


        FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), null, getContentResolver(), lectureName, TITLE);
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
    // UI

    // List of Slides
    private void configureListViewSlides() {
//        slides = findViewById(R.id.lecture_list_view);
//
//        localSlides = retrieveLocalSlides();
//
//        if (localSlides.size() > 0) {
//            adapter = new ArrayAdapterSlideName(LectureSummaryCreateActivity.this, R.layout.list_slide, localSlides);
//            slides.setAdapter(adapter);
//        }
//
//        slides.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                for (int i = 0; i < adapter.getCount(); i++) {
//                    View item = slides.getChildAt(i);
//                        if (item != null) {
//                            item.setBackgroundColor(Color.WHITE);
//                        }
//                }
//
//                if (selectedSlide) {
//                    view.setBackgroundColor(Color.WHITE);
//                    selectedSlide = false;
//                    if (delete != null) {
//                        delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
//                    }
//                    slideNumber = -1;
//
//                } else {
//                    selectedSlide = true;
//
//                    slideNumber = position;
//                    view.setBackgroundColor(Color.LTGRAY);
//                    if (delete != null) {
//                        delete.setColorFilter(null);
//                    }
//                 }
//            }
//        });

    }

    private ArrayList<String> retrieveLocalSlides() {

        ArrayList<String> slideNames = new ArrayList<>();

        if (slideDirectory.exists()) {
            File[] slidesFiles = slideDirectory.listFiles();
            numberOfSlides = slidesFiles.length;


            for (int i = 0; i < numberOfSlides; i++) {
                String slideName;
                slideName = FileReaderHelper.readTextFromFile(slideDirectory.getPath() + "/" + i + "/title.txt");

                slideNames.add(slideName);
            }
        } else {
            slideNames = null;
        }

        return slideNames;

    }
    // Audio Button

    private void configureAudio() {
        recorder = new AudioRecorder();
        // play button
        audioPreview = findViewById(R.id.course_audio_play);
        audioButton = findViewById(R.id.list_audio_button);


        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recording) {
                    Toast.makeText(LectureSummaryCreateActivity.this, R.string.startRecording, Toast.LENGTH_SHORT).show();
                    audioButton.setColorFilter(Color.RED);
                    recorder.startRecording(audioFile.getPath());
                    recording = true;
                } else {
                    Toast.makeText(LectureSummaryCreateActivity.this,  R.string.stopRecording, Toast.LENGTH_SHORT).show();
                    recorder.stopRecording();
                    audioButton.setColorFilter(Color.BLACK);
                    audioUri = Uri.fromFile(audioFile);
                    audioPreview.setVisibility(View.VISIBLE);
                    recording = false;
                }
            }
        });


        audioPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioUri != null) {
                    player = MediaPlayer.create(LectureSummaryCreateActivity.this, audioUri);
                    player.start();
                }
            }
        });

        if (audioFile.length() != 0) {
            audioUri = Uri.fromFile(audioFile);
            audioPreview.setVisibility(View.VISIBLE);
        } else {
            audioPreview.setVisibility(View.INVISIBLE);
        }

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Photo Thumbnail
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void configurePhotoThumbnail() {
        thumbnail = findViewById(R.id.list_item_thumbnail);

        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageDialog imageDialog = new ImageDialog(ImageDialog.THUMBNAIL);
                imageDialog.show(getSupportFragmentManager(), "Video Dialog");
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case AppCodes.GALLERY_SELECTION:
                    imageUri= data.getData();
                    if (imageUri != null) {
                        FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), imageUri, getContentResolver(), null, IMAGE);
                        thumbnail.setImageURI(imageUri);
                    }
                    break;

                case AppCodes.CAMERA_ROLL_SELECTION: {
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
            case AppCodes.GALLERY_SELECTION: {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"), AppCodes.GALLERY_SELECTION);
                break;
            }
            case AppCodes.CAMERA_ROLL_SELECTION: {
                Intent rollIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(rollIntent, AppCodes.CAMERA_ROLL_SELECTION);
                break;
            }
        }
    }

    // Create Button




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

    private void configureLectureName() {
        name = findViewById(R.id.list_item_text);
        name.setText(lectureName);
    }


    // Saving Data for Course Selection
    private void saveMetaData() {
//        if (imageUri != null) {
//            FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), imageUri, getContentResolver(), null, IMAGE);
//        }
//
//        if (audioUri != null) {
//            FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), audioUri, getContentResolver(), null, AUDIO);
//        }
//
//        FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), null, null, lectureName, TITLE);

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////////////////////

    private void configureTTS() {
        textToSpeech = new SpeakIcon(this);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////


    public void configureLongClicks() {
        create.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                if (!selectedSlide) {
//                   speak(getString(R.string.createSlide));
//                } else {
//                    speak(getString(R.string.editSlide));
//                }
                return true;
            }
        });



        audioButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                speak(getString(R.string.recordWithMicrophone));
                return true;
            }
        });

        thumbnail.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                speak(getString(R.string.addPhotoThumbnail));
                return true;
            }
        });

    }

}