package com.imperial.word2mouth.learn.main.online;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.imperial.word2mouth.R;

import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.imperial.word2mouth.learn.main.online.LearnOnlineMainFragment.FINGER_QUERY;
import static com.imperial.word2mouth.learn.main.online.LearnOnlineMainFragment.SPEAK_QUERY;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LearnSearchSpeakCourseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LearnSearchSpeakCourseFragment extends Fragment {


    // TTS/STT
    private static final int RECORD_QUERY = 10;
    private ImageButton searchButton;
    private ImageButton speakDelete;

    private TextToSpeech textToSpeech;

    private ImageButton listenSearch;
    private ImageButton recordSearch;


    private ArrayList<String> speakResults = new ArrayList<>();

    public LearnSearchSpeakCourseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LearnSearchSpeakCourseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LearnSearchSpeakCourseFragment newInstance() {
        LearnSearchSpeakCourseFragment fragment = new LearnSearchSpeakCourseFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_learn_search_speak_course, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpUI();

        configureTextToSpeech();

        configureOnClicks();
    }

    private void setUpUI() {
        speakDelete = getView().findViewById(R.id.delete_speech_text);

        listenSearch = getView().findViewById(R.id.listen_search);
        recordSearch = getView().findViewById(R.id.speak_search);

        searchButton = getView().findViewById(R.id.search_button);

        speakDelete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);

        listenSearch.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);

        searchButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);

    }



    private void configureOnClicks() {
        searchButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                if (!speakResults.isEmpty()) {
                    String resultSpeak = speakResults.get(0);
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    CourseOnlineSelectionFragment frag = CourseOnlineSelectionFragment.newInstance(null, null, null, resultSpeak, SPEAK_QUERY );
                    manager.beginTransaction().replace(R.id.fragment_speak_course, frag).addToBackStack(null).commit();
                }
            }
        });

        recordSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentForSpeakSearch();
            }
        });

        listenSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!speakResults.isEmpty()) {
                    speak();
                }
            }
        });

        speakDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakResults.clear();
                searchButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                speakDelete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                listenSearch.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);

            }
        });



    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void speak() {
        textToSpeech.speak(speakResults.get(0), TextToSpeech.QUEUE_FLUSH, null);

    }

    private void intentForSpeakSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, RECORD_QUERY);

        } else {
            Toast.makeText(getView().getContext(), "Your device does not support speech input", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RECORD_QUERY:
                if (resultCode == RESULT_OK && data != null) {
                    speakResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    speakDelete.setColorFilter(null);
                    listenSearch.setColorFilter(null);
                    searchButton.setColorFilter(null);
                }
                break;
        }
    }




    private void configureTextToSpeech() {
        textToSpeech = new TextToSpeech(getView().getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(getView().getContext(), "Language not supported", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getView().getContext(), "Initialization failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
}