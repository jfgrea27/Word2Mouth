package com.imperial.word2mouth.learn.main.offline;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.imperial.word2mouth.R;
import com.imperial.word2mouth.learn.main.LearnActivityMain;
import com.imperial.word2mouth.learn.main.online.LearnOnlineCourseSummary;
import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.shared.FileHandler;
import com.imperial.word2mouth.shared.FileReaderHelper;
import com.imperial.word2mouth.shared.IntentNames;
import com.imperial.word2mouth.shared.LectureItem;
import com.imperial.word2mouth.shared.adapters.ArrayAdapterLectureOffline;
import com.imperial.word2mouth.teach.offline.create.audio.AudioRecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class LearnOfflineCourseSummary extends AppCompatActivity {

    //////////////////////////////////////////////////////////////////////////////////////////////

    private File metaDirectory = null;
    private File lecturesDirectory = null;

    //////////////////////////////////////////////////////////////////////////////////////////////

    // Intents
    private String courseName;
    private String coursePath;

    //////////////////////////////////////////////////////////////////////////////////////////////

    /////// Thumbnail
    // View
    private ImageView thumbnail;
    // Model
    // Controller
    private Uri imageUri = null;


    //////// Audio Thumbnail
    // View
    private ImageButton audioPreview;
    private ImageButton audioButton;
    // Model
    private File audioFile = null;
    // Controller
    private Uri audioUri = null;
    private AudioRecorder recorder;
    private MediaPlayer player;


    ////// Title Course
    // View
    private TextView courseNameView;

    ////// List View of Lectures
    // View
    private ListView lecturesView;
    // Model
    private ArrayList<LectureItem> localLectures;
    private int lectureNumber = -1;

    // Controller
    private ArrayAdapterLectureOffline adapter;
    private boolean selectedLecture = false;


    /////// Delete Button
    // View
    private ImageButton deleteButton;
    // Model
    private int numberLectures = 0;
    // Controller

    /////// Learn Button
    // View
    private ImageButton learnButton;
    // Model
    private String lectureName;
    private TextToSpeech textToSpeech;
    // Controller



    //////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_offline_course_summary);

        // Get Intents
        getIntents();

        fetchAndCreateInformationAboutCourse();

        // List of Lectures
        configureListViewLectures();
        // Create Button
        configureLearnButton();
        // Delete Button
        configureDeleteButton();

        // Name
        configureCourseName();
        // Thumbnail
        configureCourseThumbnail();
        // Audio
        configureAudio();

        configureTextToSpeech();
        configureOnLongClicks();
    }

    private void configureOnLongClicks() {
        learnButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.learn));
                return true;
            }
        });

        deleteButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.delete));
                return true;
            }
        });
    }


    /////////////////////////////////////////////////////////////////////////////////////////////


    public void speak(String string) {
        textToSpeech.speak(string, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void configureTextToSpeech() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(LearnOfflineCourseSummary.this, "Language not supported", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LearnOfflineCourseSummary.this, "Initialization failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////


    private void fetchAndCreateInformationAboutCourse() {
        // File
        metaDirectory = new File(coursePath + DirectoryConstants.meta);

        lecturesDirectory = new File(coursePath + DirectoryConstants.lectures);
//        // Creating audioFile
        audioFile = new File(metaDirectory.getPath() + "/" + DirectoryConstants.soundThumbnail);


    }


    private void getIntents() {
        courseName = (String) getIntent().getExtras().get(IntentNames.COURSE_NAME);
        coursePath = (String) getIntent().getExtras().get(IntentNames.COURSE_PATH);
    }

    private void configureAudio() {
        audioButton = findViewById(R.id.audio_button);

        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioUri != null) {
                    audioButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    player = MediaPlayer.create(LearnOfflineCourseSummary.this, audioUri);
                    if (player != null) {
                        player.start();

                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                audioButton.setColorFilter(null);
                            }
                        });
                    } else {
                        Toast.makeText(LearnOfflineCourseSummary.this, "No audio File", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

        if (audioFile.length() != 0) {
            audioUri = Uri.fromFile(audioFile);
        }
    }

    private void configureCourseThumbnail() {
        thumbnail = findViewById(R.id.list_item_thumbnail);

        imageUri = Uri.fromFile(new File(metaDirectory + "/thumbnail.jpg"));

        if (imageUri != null) {
            thumbnail.setImageURI(imageUri);
        }
    }

    private void configureCourseName() {
        courseNameView = findViewById(R.id.list_item_text);
        courseNameView.setText(courseName);
    }

    private void configureDeleteButton() {
        deleteButton = findViewById(R.id.delete_button);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectedLecture) {
                    if (lectureNumber > -1) {
                        FileHandler.deleteRecursive(new File (lecturesDirectory.getPath() + "/" + localLectures.get(lectureNumber).getLectureName()));
                        selectedLecture = false;

                        deleteButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                        adapter.remove(localLectures.get(lectureNumber));
                        adapter.notifyDataSetInvalidated();
                        adapter.notifyDataSetChanged();
                        lectureNumber = -1;
                        numberLectures--;
                    }
                }
            }
        });


        if (deleteButton != null) {
            deleteButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        }
    }

    private void configureLearnButton() {
        learnButton = findViewById(R.id.learn_button);

        learnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lectureNumber > -1) {
                    Intent learnIntent = new Intent(LearnOfflineCourseSummary.this, SlideLearningActivity.class);
                    learnIntent.putExtra(IntentNames.LECTURE_PATH,  coursePath + DirectoryConstants.lectures + localLectures.get(lectureNumber).getLectureName());
                    learnIntent.putExtra(IntentNames.LECTURE_NAME, localLectures.get(lectureNumber).getLectureName());
                    startActivity(learnIntent);
                }
            }
        });

        if (learnButton != null) {
            learnButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        }

    }

    private void configureListViewLectures() {
        lecturesView = findViewById(R.id.lecture_list_view);

        localLectures = retrieveLocalLectures();

        if (localLectures.size() > 0) {
            adapter = new ArrayAdapterLectureOffline(LearnOfflineCourseSummary.this, R.layout.list_lectures, localLectures);
            lecturesView.setAdapter(adapter);
        }

        lecturesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                for (int i = 0; i < adapter.getCount(); i++) {
                    View item = lecturesView.getChildAt(i);
                    if (item != null) {
                        item.setBackgroundColor(Color.WHITE);
                    }
                }
                if (selectedLecture) {
                    view.setBackgroundColor(Color.WHITE);
                    selectedLecture = false;
                    if (deleteButton != null) {
                        deleteButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    }
                    if (learnButton != null) {
                        learnButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    }

                    lectureNumber = -1;


                } else {
                    selectedLecture = true;
                    lectureNumber = position;

                    view.setBackgroundColor(Color.LTGRAY);
                    if (deleteButton != null) {
                        deleteButton.setColorFilter(null);
                    }
                    if (learnButton != null) {
                        learnButton.setColorFilter(null);
                    }
                }
            }
        });

    }

    private ArrayList<LectureItem> retrieveLocalLectures() {

        ArrayList<LectureItem> lectureItems = new ArrayList<>();

        File[] lectureItemsFiles = lecturesDirectory.listFiles();

        if (lectureItemsFiles != null) {
            numberLectures = lectureItemsFiles.length;

            for (File f : lectureItemsFiles) {

                String lectureName;


                // Title
                lectureName = FileReaderHelper.readTextFromFile(f.getPath()+ DirectoryConstants.meta + DirectoryConstants.title);
                LectureItem item = new LectureItem(courseName, lectureName);
                item.setLecturePath(f.getPath());
                lectureItems.add(item);
            }
        }

        return lectureItems;
    }







}