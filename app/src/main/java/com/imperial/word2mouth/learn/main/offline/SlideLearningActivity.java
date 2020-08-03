package com.imperial.word2mouth.learn.main.offline;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.imperial.word2mouth.learn.main.offline.tracker.LectureTracker;
import com.imperial.word2mouth.learn.main.offline.tracker.SlideTracker;
import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.shared.IntentNames;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.shared.FileReaderHelper;
import com.imperial.word2mouth.shared.FileHandler;

import java.io.File;
import java.io.FileWriter;
import java.util.Locale;

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
    private String lecturePath;


    // File Management
    private File slidesFolder = null;
    private File currentSlideDirectory = null;
    private File titleFile = null;
    private File videoFile = null;
    private File audioFile = null;
    private File instructionsFile = null;

    private TextToSpeech textToSpeech;
    private LectureTracker lectureTracker;

    // Time Start and end for each slide
    private long start;
    private long end;
    private int videoCounter = 0;
    private int soundCounter = 0;
    private String versionUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_slide);

        lecturePath = (String) getIntent().getExtras().get(IntentNames.LECTURE_PATH);

        slidesFolder = new File(lecturePath + DirectoryConstants.slides);

        // meta
        if (!slidesFolder.exists()) {
            finish();
        } else {
            totalNumberSlides = slidesFolder.listFiles().length;


            emptyCourseCheck();

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

        configureTextToSpeech();
        configureLongClicks();

        // Data
        configureLectureTracker();
        configureInitialSlideTracker();
    }

    private void configureInitialSlideTracker() {
        versionUID = FileReaderHelper.readTextFromFile(lecturePath + DirectoryConstants.meta + DirectoryConstants.versionLecture);

        lectureTracker = new LectureTracker(versionUID, totalNumberSlides);
    }

    private void configureLectureTracker() {
        start = System.currentTimeMillis();
    }

    private void configureLongClicks() {
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
                        Toast.makeText(SlideLearningActivity.this, "Language not supported", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SlideLearningActivity.this, "Initialization failed", Toast.LENGTH_SHORT).show();
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

        // Update the current state of the lecture
        FileHandler.updateTracker(lectureTracker, this, lecturePath);

        super.onDestroy();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////



    private void emptyCourseCheck() {
        if (totalNumberSlides < 1) {
            finish();
        }
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
                updateDataTrackingForCurrentSlide();
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

    private void updateDataTrackingForCurrentSlide() {
        // Add time for the current slide and then new tracker
        end = System.currentTimeMillis();
        lectureTracker.getSlideTracker(slideCounter).setTimeSpent(end - start);
        lectureTracker.getSlideTracker(slideCounter).setSoundCounter(soundCounter);
        lectureTracker.getSlideTracker(slideCounter).setVideoCounter(videoCounter);

        end = 0;
        start = System.currentTimeMillis();
        soundCounter = 0;
        videoCounter = 0;
    }

    private void configureNextButton() {
        nextSlide = findViewById(R.id.button_next);
        nextSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDataTrackingForCurrentSlide();

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
        currentSlideDirectory = FileHandler.retrieveSlideDirectoryByNumber(lecturePath, slideCounter);

        if (currentSlideDirectory != null) {
            // title
            titleFile = new File(currentSlideDirectory.getPath() + "/title.txt");
            if(titleFile.exists()) {
                String text = FileReaderHelper.readTextFromFile(currentSlideDirectory.getPath() + "/title.txt");
                if (text.isEmpty()) {
                    titleView.setText("");
                } else {
                    titleView.setText(text);
                }
            }

            // instructions
            instructionsFile = new File(currentSlideDirectory.getPath() + "/instructions.txt");
            if (instructionsFile.exists()) {
                String text = FileReaderHelper.readTextFromFile(currentSlideDirectory.getPath() + "/instructions.txt");
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
                    soundCounter++;
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
                    videoCounter++;
                    videoButton.setVisibility(View.INVISIBLE);
                    videoView.start();
                    videoView.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });
    }
}