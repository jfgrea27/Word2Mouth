package com.imperial.word2mouth.common.tts;

import android.app.Activity;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import com.imperial.word2mouth.R;
import com.imperial.word2mouth.create.CreateContentActivity;

import org.w3c.dom.Text;

import java.util.Locale;

public class SpeakIcon {
    private static TextToSpeech tts;

    public static TextToSpeech setUpTTS(Activity activity) {
        tts = new TextToSpeech(activity.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.getDefault());

                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(activity,
                            R.string.languageNotSupported, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(activity,
                        R.string.initializationFailedSST, Toast.LENGTH_SHORT).show();
            }
        });
        return tts;
    }
}
