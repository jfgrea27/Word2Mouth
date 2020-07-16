package com.imperial.word2mouth.learn.main.online;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.learn.main.online.teacher.Teacher;
import com.imperial.word2mouth.shared.IntentNames;
import com.imperial.word2mouth.shared.adapters.ArrayAdapterCourseItemsOnline;
import com.imperial.word2mouth.shared.CourseItem;
import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.shared.UnzipFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CourseOnlineSelectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CourseOnlineSelectionFragment extends Fragment {


    // Permissions
    private final int INTERNET_PERMISSION = 1;
    private final int READ_WRITE_PERMISSION = 2;

    private boolean hasInternetAccess = false;
    private boolean hasReadWriteStorageAccess = false;


    private ImageButton searchCourse = null;
    private ListView listCourses = null;

    // List of Courses

    private ArrayAdapterCourseItemsOnline adapter;


    private boolean selectedCourse = false;


    // Parameter (Teacher Name)
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final String ARG_PARAM5 = "param5";


    private int searchType;


    // Search Touch
    private String language;
    private String teacherUID;
    private String category;

    // Search Speaking
    private String speakQuery;


    // Model
    private ArrayList<CourseItem> onlineCourses;
    private CourseItem courseItem = null;



    // Firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private HashMap<String, Teacher> teachersHashMap = new HashMap<>();
    private ArrayList<Teacher> teachers = new ArrayList<>();
    private int courseNumber = -1;


    ////////////////////////////////////////////////////////////////////////////////////////////////
    public CourseOnlineSelectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CourseOnlineSelection.
     */
    public static CourseOnlineSelectionFragment newInstance(String teacher, String language, String category, String speakQuery, int searchType) {
        CourseOnlineSelectionFragment fragment = new CourseOnlineSelectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, teacher);
        args.putString(ARG_PARAM2, language);
        args.putString(ARG_PARAM3, category);
        args.putString(ARG_PARAM4, speakQuery);
        args.putInt(ARG_PARAM5, searchType);

        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            teacherUID = getArguments().getString(ARG_PARAM1);
            language = getArguments().getString(ARG_PARAM2);
            category = getArguments().getString(ARG_PARAM3);

            speakQuery = getArguments().getString(ARG_PARAM4);
            searchType = getArguments().getInt(ARG_PARAM5);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_course_online_selection, container, false);
        v.bringToFront();
        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getPermissions();

        if (hasNecessaryPermissions()) {
            configureUI();
            configureSearchButton();
            configureRequest();
            configureListCourses();
        }
    }

    private void configureSearchButton() {
        searchCourse = getView().findViewById(R.id.search_button);

        searchCourse.setVisibility(View.INVISIBLE);

        searchCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (courseNumber > -1) {
                    Intent courseSummaryIntent = new Intent(getActivity(), LearnOnlineCourseSummary.class);
                    courseSummaryIntent.putExtra(IntentNames.COURSE, onlineCourses.get(courseNumber));
                    startActivity(courseSummaryIntent);
                }
            }
        });
    }




    private void configureUI() {
        searchCourse = getView().findViewById(R.id.search_button);
        listCourses = getView().findViewById(R.id.list_courses_per_teacher);

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


    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void configureRequest() {
        switch (searchType) {
            case LearnOnlineMainFragment.FINGER_QUERY:
                configureQuerySearch();

                break;
            case LearnOnlineMainFragment.SPEAK_QUERY:
                configureSpeakSearch();
                break;
        }

    }

    private void configureSpeakSearch() {
        Query query = db.collection("content").whereEqualTo("type", "Course").whereEqualTo("low_CourseName", speakQuery + " ");

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                onlineCourses = getCourses(queryDocumentSnapshots.getDocuments());
                updateListView();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getView().getContext(), "Could not retrieve the query", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void configureQuerySearch() {


        Query query = prepareQuery();

        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
           @RequiresApi(api = Build.VERSION_CODES.KITKAT)
           @Override
           public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                onlineCourses = getCourses(queryDocumentSnapshots.getDocuments());
               updateListView();
           }
       }).addOnFailureListener(new OnFailureListener() {
           @Override
           public void onFailure(@NonNull Exception e) {
               Toast.makeText(getView().getContext(), "Could not retrieve the query", Toast.LENGTH_SHORT).show();

           }
       });

    }

    private Query prepareQuery() {
        Query query = db.collection("content").whereEqualTo("type", "Course");

        if (language != "") {
            query = query.whereEqualTo("language", language);
        }

        if (category != "") {
            query.whereEqualTo("category", category);
        }

        if (teacherUID != "" && teacherUID != null) {
            query = query.whereEqualTo("authorUID", teacherUID);
        }

        return query;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updateListView() {
        if (onlineCourses.size() > 0) {
            if (getView() != null) {
                adapter = new ArrayAdapterCourseItemsOnline(getView().getContext(), R.layout.list_item, onlineCourses);
                adapter.loadThumbnails();
                listCourses.setAdapter(adapter);
            }
        }
    }

    private ArrayList<CourseItem> getCourses(List<DocumentSnapshot> courses) {
        ArrayList<CourseItem> courseItems = new ArrayList<>();

        for (DocumentSnapshot course: courses) {
            CourseItem courseItem = new CourseItem((String) course.get("courseName"), (String) course.get("courseUID"), true);
            courseItem.setAuthorID((String) course.get("authorUID"));
            courseItem.setLanguage((String) course.get("language"));
            courseItem.setCategory((String) course.get("category"));
            courseItem.setCourseBluetooth((String) course.get("bluetoothCourse"));
            courseItems.add(courseItem);
        }
        return courseItems;
    }



    private void configureListCourses() {
        if (listCourses != null) {
            listCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    for (int i = 0; i < adapter.getCount(); i++) {
                        View item = listCourses.getChildAt(i);
                        if (item != null) {
                            item.setBackgroundColor(Color.WHITE);
                        }
                    }

                    if (selectedCourse) {
                        view.setBackgroundColor(Color.WHITE);
                        courseNumber = -1;
                        searchCourse.setVisibility(View.INVISIBLE);

                    } else {
                        view.setBackgroundColor(Color.LTGRAY);
                        courseNumber = position;
                        searchCourse.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

}