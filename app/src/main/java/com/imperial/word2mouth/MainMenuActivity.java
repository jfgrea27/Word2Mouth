package com.imperial.word2mouth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.imperial.word2mouth.account.AccountActivity;
import com.imperial.word2mouth.create.CreateContentActivity;
import com.imperial.word2mouth.watch.WatchContentActivity;

import java.util.Locale;

public class MainMenuActivity extends AppCompatActivity {

    /////////////////////// UI //////////////////////
    private ImageButton loginButton;
    private ImageButton createButton;
    private ImageButton shareButton;
    private ImageButton watchButton;

    /////////////////////// TTS //////////////////////
    private TextToSpeech textToSpeech;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       configureUI();
       configureTTS();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Configure UI
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private void configureUI() {
        this.loginButton = findViewById(R.id.login_button);
        this.createButton = findViewById(R.id.create_button);
        this.shareButton = findViewById(R.id.share_button);
        this.watchButton = findViewById(R.id.watch_button);
        configureOnClick();
    }

    private void configureOnClick() {
        this.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentAccountInformation = new Intent(
                        MainMenuActivity.this,
                        AccountActivity.class);
                startActivity(intentAccountInformation);
            }
        });
        this.createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentCreateContent = new Intent(
                        MainMenuActivity.this,
                        CreateContentActivity.class);
                startActivity(intentCreateContent);
            }
        });
        this.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentShareContent = new Intent(
                        MainMenuActivity.this,
                        AccountActivity.class);
                startActivity(intentShareContent);
            }
        });
        this.watchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentWatchContent = new Intent(
                        MainMenuActivity.this,
                        WatchContentActivity.class);
                startActivity(intentWatchContent);
            }
        });

//        this.Button.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                Intent login = new Intent(MainMenuActivity.this, AccountActivity.class);
//                startActivity(login);
//            }
//        });
//        this.loginButton.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                Intent login = new Intent(MainMenuActivity.this, AccountActivity.class);
//                startActivity(login);
//            }
//        });
        // TODO complete this. Should bring users to different Activities
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Text To Speech
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void configureTTS() {
        this.textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(MainMenuActivity.this, R.string.languageNotSupported, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainMenuActivity.this, R.string.initializationFailedSST, Toast.LENGTH_SHORT).show();
                }
            }
        });

        configureOnLongClick();
    }

    private void configureOnLongClick() {
        this.loginButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MainMenuActivity.this.textToSpeech.speak(
                        getString(R.string.login_button),
                        TextToSpeech.QUEUE_FLUSH,
                        null);
                return true;
            }
        });
        this.shareButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MainMenuActivity.this.textToSpeech.speak(
                        getString(R.string.share_button),
                        TextToSpeech.QUEUE_FLUSH,
                        null);
                return true;
            }
        });
        this.createButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MainMenuActivity.this.textToSpeech.speak(
                        getString(R.string.create_button),
                        TextToSpeech.QUEUE_FLUSH,
                        null);
                return true;
            }
        });
        this.watchButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MainMenuActivity.this.textToSpeech.speak(
                        getString(R.string.watch_button),
                        TextToSpeech.QUEUE_FLUSH,
                        null);
                return true;
            }
        });
    }

}