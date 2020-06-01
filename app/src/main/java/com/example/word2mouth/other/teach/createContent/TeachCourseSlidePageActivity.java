package com.example.word2mouth.other.teach.createContent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.word2mouth.R;
import com.example.word2mouth.other.CourseSlidePageActivity;
import com.example.word2mouth.other.teach.createContent.data.CourseContent;
import com.example.word2mouth.other.teach.createContent.data.SlideContent;
import com.example.word2mouth.other.teach.createContent.sound.SoundSelectionActivity;
import com.example.word2mouth.other.teach.createContent.video.VideoSelectionActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class TeachCourseSlidePageActivity extends CourseSlidePageActivity {

    // View
    private VideoView videoView = null;
    private Button previewVideo = null;

    // Model
    private SlideContent slideContent;
    private CourseContent courseContent;
    private String slideName = null;
    private File directorySlide;
    private String videoFilePath = null;

    // Intents
    private static final int RESULT_CHOOSE_IMAGE = 1;
    private static final int RESULT_CHOOSE_AUDIO = 2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_slide_page);

        // View
        configureBackButton();
        configureNextSlideButton();

        // preliminaries
        configureTitleSlide();

        // Video
        configurePreviewVideoButton();
        configureVideoButton();

    }

    @Override
    protected void configureAudioButton() {

    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        outState.putString("videoFilePath", videoFilePath);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        videoFilePath = savedInstanceState.getString("videoFilePath");
    }


    private void configureTitleSlide() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TeachCourseSlidePageActivity.this);
        builder.setTitle("Slide Name");

        final EditText input = new EditText(TeachCourseSlidePageActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                slideName = input.getText().toString();
                configureSlideContent();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        builder.show();
    }

    private void configureSlideContent() {
        courseContent = getIntent().getParcelableExtra("contents");
        if (slideName.isEmpty()) {
            slideName = "Untitled slide";
        }
        slideContent = new SlideContent(courseContent.getCoursePath(), courseContent.getNumberSlides() + 1, slideName);
        courseContent.addSlide(slideContent);

        directorySlide = new File(courseContent.getCoursePath(), slideContent.getSlidePath());

        if (!directorySlide.exists()) {
            directorySlide.mkdirs();
        }
    }

    private void configurePreviewVideoButton() {
        previewVideo = findViewById(R.id.preview_video);
        previewVideo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView != null) {
                    videoView.start();
                    previewVideo.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    protected void configureNextSlideButton() {
        nextSlide = findViewById(R.id.button_next);
        nextSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent creatingCourseIntent = new Intent(TeachCourseSlidePageActivity.this, CourseSlideSummaryActivity.class);
                creatingCourseIntent.putExtra("contents", courseContent);
                startActivity(creatingCourseIntent);
            }
        });
    }

    @Override
    protected void configureVideoButton() {
        videoView = findViewById(R.id.videoView);
        videoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView != null) {
                    Intent choiceVideo = new Intent(TeachCourseSlidePageActivity.this, VideoSelectionActivity.class);
                    choiceVideo.putExtra("video", (android.os.Parcelable) null);
                    startActivityForResult(choiceVideo, RESULT_CHOOSE_IMAGE);
                }
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                previewVideo.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_CHOOSE_IMAGE: {
                    Uri video = Uri.parse(data.getExtras().get("video").toString());
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(video);
                        slideContent.setVideoFilePath(inputStream);
                        videoFilePath = slideContent.getVideoFilePath();
                        videoView.setVideoURI(Uri.parse(videoFilePath));
                        previewVideo.setVisibility(View.VISIBLE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
//                case RESULT_CHOOSE_AUDIO: {
//                    MediaPlayer mediaPlayer = new MediaPlayer();
//                    try {
//                        mediaPlayer.setDataSource(data.getDataString());
//                        mediaPlayer.prepare();
//                        mediaPlayer.start();
//                    } catch (IOException e) {
//
//                    }
//                    break;
//                }
                default:
                    throw new IllegalStateException("Unexpected value: " + requestCode);
            }
        }
    }
}
