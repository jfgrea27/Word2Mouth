package com.example.word2mouth.other.teach;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.example.word2mouth.R;
import com.example.word2mouth.mainActivity.MainActivity;
import com.example.word2mouth.other.OtherActivity;
import com.example.word2mouth.other.learn.LearnActivity;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import java.io.IOException;
import java.util.Random;

public class TeachActivity extends OtherActivity{


    private Button buttonStart, buttonStop, buttonPlayLastRecordAudio,
            buttonStopPlayingRecording, buttonAddImage;
    private SoundRecorder soundRecorder = new SoundRecorder();

    public static final int RequestPermissionCode = 1;

    Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach);

        configureBackButton();
        configureAddImageButton();
        configureRecordingButtons();

        random = new Random();

        buttonStart.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkPermission()) {

                    soundRecorder.AudioSavePathInDevice =
                            Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
                                    soundRecorder.CreateRandomAudioFileName(5, random) + "AudioRecording.3gp";

                    soundRecorder.MediaRecorderReady();

                    try {
                        soundRecorder.mediaRecorder.prepare();
                        soundRecorder.mediaRecorder.start();
                    } catch (IllegalStateException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    buttonStart.setEnabled(false);
                    buttonStop.setEnabled(true);

                    Toast.makeText(TeachActivity.this, "Recording started",
                            Toast.LENGTH_LONG).show();
                } else {
                    requestPermission();
                }

            }
        });

        buttonStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                soundRecorder.mediaRecorder.stop();
                buttonStop.setEnabled(false);
                buttonPlayLastRecordAudio.setEnabled(true);
                buttonStart.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);

                Toast.makeText(TeachActivity.this, "Recording Completed",
                        Toast.LENGTH_LONG).show();
            }
        });

        buttonPlayLastRecordAudio.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException,
                    SecurityException, IllegalStateException {

                buttonStop.setEnabled(false);
                buttonStart.setEnabled(false);
                buttonStopPlayingRecording.setEnabled(true);

                soundRecorder.mediaPlayer = new MediaPlayer();
                try {
                    soundRecorder.mediaPlayer.setDataSource(soundRecorder.AudioSavePathInDevice);
                    soundRecorder.mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                soundRecorder.mediaPlayer.start();
                Toast.makeText(TeachActivity.this, "Recording Playing",
                        Toast.LENGTH_LONG).show();
            }
        });

        buttonStopPlayingRecording.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonStop.setEnabled(false);
                buttonStart.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);
                buttonPlayLastRecordAudio.setEnabled(true);

                if(soundRecorder.mediaPlayer != null){
                    soundRecorder.mediaPlayer.stop();
                    soundRecorder.mediaPlayer.release();
                    soundRecorder.MediaRecorderReady();
                }
            }
        });

    }


    private void requestPermission() {
        ActivityCompat.requestPermissions(TeachActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(TeachActivity.this, "Permission Granted",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(TeachActivity.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void configureAddImageButton() {
        buttonAddImage = (Button) findViewById(R.id.button_add_media);
        buttonAddImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TeachActivity.this,
                        MediaSelectionActivity.class));
            }
        });
    }

    private void configureRecordingButtons() {
        buttonStart = (Button) findViewById(R.id.button_record_start);
        buttonStop = (Button) findViewById(R.id.button_record_end);
        buttonPlayLastRecordAudio = (Button) findViewById(R.id.button_listen_start);
        buttonStopPlayingRecording = (Button)findViewById(R.id.button_listen_end);

        buttonStop.setEnabled(false);
        buttonPlayLastRecordAudio.setEnabled(false);
        buttonStopPlayingRecording.setEnabled(false);
    }
}
