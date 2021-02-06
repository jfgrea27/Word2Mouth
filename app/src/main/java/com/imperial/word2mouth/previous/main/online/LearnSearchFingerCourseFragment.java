package com.imperial.word2mouth.previous.main.online;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.imperial.word2mouth.R;
import com.imperial.word2mouth.previous.main.LearnActivityMain;
import com.imperial.word2mouth.previous.main.online.dialog.DialogCategory;
import com.imperial.word2mouth.previous.main.online.dialog.DialogLanguage;
import com.imperial.word2mouth.previous.main.online.teacher.TeacherSearchFragment;
import com.imperial.word2mouth.previous.shared.Categories;
import com.imperial.word2mouth.previous.shared.Languages;

import static com.imperial.word2mouth.previous.main.online.LearnOnlineMainFragment.*;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LearnSearchFingerCourseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LearnSearchFingerCourseFragment extends Fragment {

    private Languages languages = new Languages();

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


    private ImageButton teacherDelete;
    private ImageButton languageDelete;
    private ImageButton categoryDelete;


    private String selectedUserUID;


    private ImageButton searchButton;
    private boolean searchable = false;


    public LearnSearchFingerCourseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LearnOnlineMainFragment.
     */
    public static LearnSearchFingerCourseFragment newInstance() {
        LearnSearchFingerCourseFragment fragment = new LearnSearchFingerCourseFragment();
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
            configureOnClicks();
        } else {
            getActivity().getSupportFragmentManager().popBackStack();
        }

    }



    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Permissions
    private void getPermissions() {
        if (!(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(getView().getContext(), R.string.allowInternetAccess, Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION);
        } else {
            hasInternetAccess = true;
        }

        if (!(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED )) {
            Toast.makeText(getView().getContext(), R.string.accessStorage, Toast.LENGTH_SHORT).show();
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


        searchButton = getView().findViewById(R.id.search_button);


        personButton.setVisibility(View.VISIBLE);
        categoryButton.setVisibility(View.VISIBLE);
        languageButton.setVisibility(View.VISIBLE);

        languageText.setVisibility(View.VISIBLE);
        categoryText.setVisibility(View.VISIBLE);
        teacherText.setVisibility(View.VISIBLE);

        languageDelete.setVisibility(View.VISIBLE);
        categoryDelete.setVisibility(View.VISIBLE);
        teacherDelete.setVisibility(View.VISIBLE);

        languageDelete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        categoryDelete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        teacherDelete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);


        searchButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);

        setLongHoldSpeak();


    }


    public void setLongHoldSpeak() {
        personButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LearnActivityMain act = (LearnActivityMain) getActivity();
                act.speak(getString(R.string.searchProfile));
                return true;
            }
        });

        categoryButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LearnActivityMain act = (LearnActivityMain) getActivity();
                act.speak(getString(R.string.searchCategory));
                return true;
            }
        });

        languageButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LearnActivityMain act = (LearnActivityMain) getActivity();
                act.speak(getString(R.string.searchLanguage));
                return true;
            }
        });


        teacherDelete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LearnActivityMain act = (LearnActivityMain) getActivity();
                act.speak(getString(R.string.delete));
                return true;            }
        });
        languageDelete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LearnActivityMain act = (LearnActivityMain) getActivity();
                act.speak(getString(R.string.delete));
                return true;            }
        });
        categoryDelete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LearnActivityMain act = (LearnActivityMain) getActivity();
                act.speak(getString(R.string.delete));
                return true;            }
        });

        searchButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                LearnActivityMain act = (LearnActivityMain) getActivity();
                act.speak(getString(R.string.search));
                return true;
            }
        });
    }


    private void configureOnClicks() {

        searchButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                if (searchable) {
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    CourseOnlineSelectionFragment frag = CourseOnlineSelectionFragment.newInstance(selectedUserUID, languageText.getText().toString(), categoryText.getText().toString(), null, FINGER_QUERY);
                    manager.beginTransaction().replace(R.id.fragment_learn_online_main, frag).addToBackStack(null).commit();
                }
            }
        });


        categoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogCategory dialogCategory = new DialogCategory(getView(), LearnSearchFingerCourseFragment.this);
                dialogCategory.buildDialog(DialogCategory.QUERY);
                if (!categoryText.getText().toString().equals("")) {
                    searchable = true;

                    if (searchButton != null) {
                        searchButton.setColorFilter(null);
                    }
                    if (categoryText != null) {
                        categoryDelete.setColorFilter(null);
                    }
                }
            }
        });


        languageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogLanguage dialogLanguage = new DialogLanguage(getView(), LearnSearchFingerCourseFragment.this);
                dialogLanguage.buildDialog(DialogLanguage.QUERY_FINGER);
            }
        });

        personButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager manager = getActivity().getSupportFragmentManager();
                TeacherSearchFragment frag;
                frag = TeacherSearchFragment.newInstance();
                frag.setFragment(LearnSearchFingerCourseFragment.this);
                manager.beginTransaction().replace( getView().getId(), frag).addToBackStack(null).commit();
            }
        });


        categoryDelete.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                categoryText.setText("");
                categoryButton.setImageResource(R.drawable.ic_category);
                categoryDelete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                if (teacherText.getText().toString().equals("") && languageText.getText().toString().equals("")) {
                    searchable = false;

                    if (searchButton != null) {
                        searchButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    }
                }
            }
        });

        teacherDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                teacherText.setText("");
                selectedUserUID = "";
                teacherDelete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                personButton.setImageResource(R.drawable.ic_account_black);

                if (languageText.getText().toString().equals("") && categoryText.getText().toString().equals("")) {
                    searchable = false;

                    if (searchButton != null) {
                        searchButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    }
                }
            }
        });

        languageDelete.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                languageText.setText("");
                languageButton.setImageResource(R.drawable.ic_flag);
                languageDelete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                if (teacherText.getText().toString().equals("") && categoryText.getText().toString().equals("")) {
                    searchable = false;

                    if (searchButton != null) {
                        searchButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    }
                }
            }
        });



    }

    public void setTeacherName(String userName, String userUid) {
        teacherText.setText(userName);
        this.selectedUserUID = userUid;

        if (!teacherText.getText().toString().equals("")) {
            searchable = true;

            if (searchButton != null) {
                searchButton.setColorFilter(null);
            }
            teacherDelete.setColorFilter(null);

        }

    }

    public void setLanguageTo(String s) {
        languageText.setText(s);
        languageButton.setImageResource(languages.languageIconMap.get(s));

        if (!languageText.getText().toString().equals("")) {
            searchable = true;

            if (searchButton != null) {
                searchButton.setColorFilter(null);
            }
            languageDelete.setColorFilter(null);

        }

    }

    public void setCategory(String s) {
        categoryText.setText(s);
        categoryButton.setImageResource(Categories.categoryIconMap.get(s));

        if (!categoryText.getText().toString().equals("")) {
            searchable = true;
            if (searchButton != null) {
                searchButton.setColorFilter(null);
            }
            categoryDelete.setColorFilter(null);
        }

    }


    public void setTeacherUri(Uri thumbnail) {
        personButton.setImageURI(thumbnail);
        personButton.invalidate();
    }

}