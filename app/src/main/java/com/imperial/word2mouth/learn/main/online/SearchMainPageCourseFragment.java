package com.imperial.word2mouth.learn.main.online;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.imperial.word2mouth.R;
import com.imperial.word2mouth.learn.main.online.dialog.DialogCategory;
import com.imperial.word2mouth.learn.main.online.dialog.DialogLanguage;
import com.imperial.word2mouth.learn.main.online.teacher.TeacherSearchFragment;
import com.imperial.word2mouth.shared.Categories;
import com.imperial.word2mouth.shared.Languages;

import java.util.ArrayList;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.imperial.word2mouth.learn.main.online.LearnOnlineMainFragment.*;
import static com.imperial.word2mouth.learn.main.online.dialog.DialogLanguage.QUERY_SPEAK;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchMainPageCourseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchMainPageCourseFragment extends Fragment {

    private Languages languages = new Languages();
    private static int searchChoice = 0;

    // Permissions
    private final int INTERNET_PERMISSION = 1;
    private final int READ_WRITE_PERMISSION = 2;

    private boolean hasInternetAccess = false;
    private boolean hasReadWriteStorageAccess = false;


    // UI
    private ImageButton personButton;
    private ImageButton languageButton;
    private ImageButton categoryButton;
    private TextView teacherText;
    private TextView languageText;
    private TextView categoryText;

    private ImageButton listenSearch;
    private ImageButton recordSearch;

    private ImageButton teacherDelete;
    private ImageButton languageDelete;
    private ImageButton categoryDelete;

    // TTS/STT
    private static final int RECORD_QUERY = 10;
    private ImageButton searchButton;
    private String selectedUserUID;
    private ImageButton speakDelete;

    private TextToSpeech textToSpeech;
    private String selectedLanguage;

    private ArrayList<String> speakResults = new ArrayList<>();


    public SearchMainPageCourseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LearnOnlineMainFragment.
     * @param choice
     */
    public static SearchMainPageCourseFragment newInstance(int choice) {
        SearchMainPageCourseFragment fragment = new SearchMainPageCourseFragment();
        searchChoice = choice;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_learn_online_main, container, false);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getPermissions();

        if (hasNecessaryPermissions()) {
            setUpUI();
            updateUI();
            configureTextToSpeech();
            configureOnClicks();
        } else {
            getActivity().getSupportFragmentManager().popBackStack();
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


    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Permissions
    private void getPermissions() {
        if (!(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(getView().getContext(), "Please allow access to Internet Access", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION);
        } else {
            hasInternetAccess = true;
        }

        if (!(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED )) {
            Toast.makeText(getView().getContext(), "Please allow access to Storage", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, READ_WRITE_PERMISSION);
        } else {
            hasReadWriteStorageAccess = true;
        }
    }

    private boolean hasNecessaryPermissions() {
        return hasReadWriteStorageAccess && hasInternetAccess;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == INTERNET_PERMISSION) {
            if(permissions[0].equals(Manifest.permission.INTERNET) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasInternetAccess = true;
            }
        }
        if (requestCode == READ_WRITE_PERMISSION) {
            if(permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    permissions[1].equals(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasReadWriteStorageAccess = true;
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////


    // UI


    private void setUpUI() {
        personButton = getView().findViewById(R.id.account_button);
        categoryButton = getView().findViewById(R.id.category_button);
        languageButton = getView().findViewById(R.id.language_button);

        languageText = getView().findViewById(R.id.label_language);
        categoryText = getView().findViewById(R.id.label_category);
        teacherText = getView().findViewById(R.id.label_teacher);


        teacherDelete = getView().findViewById(R.id.delete_teacher_query);
        languageDelete = getView().findViewById(R.id.delete_language_query);
        categoryDelete = getView().findViewById(R.id.delete_category_query);
        speakDelete = getView().findViewById(R.id.delete_speech_text);

        listenSearch = getView().findViewById(R.id.listen_search);
        recordSearch = getView().findViewById(R.id.speak_search);

        searchButton = getView().findViewById(R.id.search_button);


    }



    private void configureOnClicks() {
        categoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogCategory dialogCategory = new DialogCategory(getView(), SearchMainPageCourseFragment.this);
                dialogCategory.buildDialog(DialogCategory.QUERY);
            }
        });


        languageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogLanguage dialogLanguage = new DialogLanguage(getView(), SearchMainPageCourseFragment.this);
                dialogLanguage.buildDialog(DialogLanguage.QUERY_FINGER);
            }
        });

        personButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager manager = getActivity().getSupportFragmentManager();
                TeacherSearchFragment frag;
                frag = TeacherSearchFragment.newInstance();
                frag.setFragment(SearchMainPageCourseFragment.this);
                manager.beginTransaction().replace( getView().getId(), frag).addToBackStack(null).commit();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                switch (searchChoice) {
                    case FINGER_QUERY:
                        if (!languageText.getText().toString().equals("") || !categoryText.getText().toString().equals("") || !teacherText.getText().toString().equals("")) {
                            FragmentManager manager = getActivity().getSupportFragmentManager();
                            CourseOnlineSelectionFragment frag = CourseOnlineSelectionFragment.newInstance(selectedUserUID, languageText.getText().toString(), categoryText.getText().toString(), null, FINGER_QUERY);
                            manager.beginTransaction().replace(R.id.fragment_learn_online_main, frag).addToBackStack(null).commit();

                        }break;
                    case SPEAK_QUERY:
                        if (!speakResults.isEmpty()) {
                            String resultSpeak = speakResults.get(0);
                            FragmentManager manager = getActivity().getSupportFragmentManager();
                            CourseOnlineSelectionFragment frag = CourseOnlineSelectionFragment.newInstance(null, null, null, resultSpeak, SPEAK_QUERY );
                            manager.beginTransaction().replace(R.id.fragment_learn_online_main, frag).addToBackStack(null).commit();
                        }

                }

            }
        });

        categoryDelete.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                categoryText.setText("");
                categoryButton.setImageResource(R.drawable.ic_category);
            }
        });

        teacherDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                teacherText.setText("");
                selectedUserUID = "";
            }
        });

        languageDelete.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                languageText.setText("");
                languageButton.setImageResource(R.drawable.ic_flag);
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
                searchButton.setVisibility(View.INVISIBLE);
                selectedLanguage = null;

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

    private void speak() {
        textToSpeech.speak(speakResults.get(0), TextToSpeech.QUEUE_FLUSH, null);

    }

    private void intentForSpeakSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            Locale chosenLanguage = retrieveLocaleLanguage(languageText.getText().toString());
            if (chosenLanguage != null) {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, chosenLanguage);
            } else {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            }
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(intent, RECORD_QUERY);

            } else {
                Toast.makeText(getView().getContext(), "Your device does not support speech input", Toast.LENGTH_SHORT).show();
            }
    }

    private Locale retrieveLocaleLanguage(String selectedLanguage) {
        Locale language = null;
        switch (selectedLanguage) {
            case "Francais":
                language = Locale.FRENCH;
                break;
            case "English":
                language = Locale.ENGLISH;
                break;
            default:
                Toast.makeText(getContext(), "Language not supported", Toast.LENGTH_SHORT).show();
        }
        return language;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RECORD_QUERY:
                if (resultCode == RESULT_OK && data != null) {
                    speakResults = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    searchButton.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void updateUI() {
        switch (searchChoice) {
            case SPEAK_QUERY:
                personButton.setVisibility(View.INVISIBLE);
                categoryButton.setVisibility(View.INVISIBLE);

                categoryText.setVisibility(View.INVISIBLE);
                teacherText.setVisibility(View.INVISIBLE);

                categoryDelete.setVisibility(View.INVISIBLE);
                teacherDelete.setVisibility(View.INVISIBLE);

                speakDelete.setVisibility(View.VISIBLE);
                recordSearch.setVisibility(View.VISIBLE);

                listenSearch.setVisibility(View.VISIBLE);

                languageText.setVisibility(View.VISIBLE);
                languageButton.setVisibility(View.VISIBLE);
                languageDelete.setVisibility(View.VISIBLE);


                searchButton.setVisibility(View.INVISIBLE);

                break;
            case FINGER_QUERY:
                personButton.setVisibility(View.VISIBLE);
                categoryButton.setVisibility(View.VISIBLE);
                languageButton.setVisibility(View.VISIBLE);

                languageText.setVisibility(View.VISIBLE);
                categoryText.setVisibility(View.VISIBLE);
                teacherText.setVisibility(View.VISIBLE);

                languageDelete.setVisibility(View.VISIBLE);
                categoryDelete.setVisibility(View.VISIBLE);
                teacherDelete.setVisibility(View.VISIBLE);


                speakDelete.setVisibility(View.INVISIBLE);
                recordSearch.setVisibility(View.INVISIBLE);

                listenSearch.setVisibility(View.INVISIBLE);
                break;
        }
    }

    public void setTeacherName(String userName, String userUid) {
        teacherText.setText(userName);
        this.selectedUserUID = userUid;

    }

    public void setLanguageTo(String s) {
        languageText.setText(s);
        languageButton.setImageResource(languages.languageIconMap.get(s));

    }

    public void setCategory(String s) {
        categoryText.setText(s);
        categoryButton.setImageResource(Categories.categoryIconMap.get(s));

    }


    public void setTeacherUri(Uri thumbnail) {
        personButton.setImageURI(thumbnail);
        personButton.invalidate();
    }

    public void setLanguageSelected(String s) {
        selectedLanguage = s;
    }
}