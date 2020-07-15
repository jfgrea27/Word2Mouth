package com.imperial.word2mouth.teach.online;

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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.shared.IntentNames;
import com.imperial.word2mouth.shared.adapters.ArrayAdapterCourseItemsOnline;
import com.imperial.word2mouth.shared.CourseItem;
import com.imperial.word2mouth.teach.offline.upload.database.CourseTransferObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeachOnlineMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeachOnlineMainFragment extends Fragment {

    // Permissions
    private final int INTERNET_PERMISSION = 1;
    private final int READ_WRITE_PERMISSION = 2;

    private boolean hasInternetAccess = false;
    private boolean hasReadWriteStorageAccess = false;

    // Internet Permission

    private ListView onlineListCourses;
    private ImageButton courseSummaryButton;

    // Courses
    private ArrayList<CourseItem> onlineCourses = null;


    // Adapter
    private ArrayAdapterCourseItemsOnline adapter = null;

    private SearchView searchView;

    // Online
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private boolean selectedCourse = false;

    // Course data
    private String courseName = null;
    private CourseItem courseItem = null;
    private String courseIdentification = null;

    private ImageButton delete;
    private int courseNumber = -1;

    public TeachOnlineMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LearnOnlineMainFragment.
     */
    public static TeachOnlineMainFragment newInstance(String param1, String param2) {
        TeachOnlineMainFragment fragment = new TeachOnlineMainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if (onlineListCourses != null) {
            retrieveOnlineCourses();
        }
        return inflater.inflate(R.layout.fragment_teach_online_main, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getPermissions();

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            if (hasNecessaryPermissions()) {
                configureCourseSummaryButton();
                configureSearchView();
                configureDeleteButton();
                configureListCourses();
            }

        } else {
            Toast.makeText(getView().getContext(), "Need to log in", Toast.LENGTH_SHORT).show();
        }
    }

    private void configureDeleteButton() {
        delete = getView().findViewById(R.id.delete_course_button);

        delete.setVisibility(View.INVISIBLE);

        delete.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if (courseNumber > -1) {
                    // Find the Lectures and Courses Under that course and Delete them
                    db.collection("content").whereEqualTo("courseUID", onlineCourses.get(courseNumber).getCourseOnlineIdentification()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            deleteEachItem(queryDocumentSnapshots.getDocuments());
                        }
                    });

                    adapter.remove(onlineCourses.get(courseNumber));
                    adapter.notifyDataSetChanged();
                    courseNumber = -1;
                }
            }

            private void deleteEachItem(List<DocumentSnapshot> documents) {
                for (DocumentSnapshot doc : documents) {
                    String type = (String) doc.get("type");
                    StorageReference itemRef;
                    switch (type) {
                        case "Lecture":
                            itemRef = FirebaseStorage.getInstance().getReference().child("content").child((String) doc.get("lectureUID"));
                            itemRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                @Override
                                public void onSuccess(ListResult listResult) {
                                    deleteEverySubItem(listResult.getItems());
                                }
                            });
                            itemRef.delete();

                            db.collection("content").document((String) doc.get("lectureUID")).delete();
                            break;
                        case "Course":
                            itemRef = FirebaseStorage.getInstance().getReference().child("content").child((String) doc.get("courseUID"));
                            itemRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                @Override
                                public void onSuccess(ListResult listResult) {
                                    deleteEverySubItem(listResult.getItems());
                                }
                            });
                            itemRef.delete();
                            db.collection("content").document((String) doc.get("courseUID")).delete();

                            break;
                    }
                }
            }

            private void deleteEverySubItem(List<StorageReference> items) {
                for (StorageReference item : items) {
                    item.delete();
                }
            }
        });
    }


    private void configureSearchView() {
        searchView = getView().findViewById(R.id.searchView);

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
            }
        });

        searchView .setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onQueryTextChange(String newText) {
                String text = newText;
                if (adapter != null) {
                    adapter.filter(text);
                }
                return false;
            }
        });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Permissions
    private void getPermissions() {
        if (!(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED)){
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
    // Retrieving courses from Database

    private void configureListCourses() {
        onlineListCourses = getView().findViewById(R.id.list_courses_online);

        retrieveOnlineCourses();

        onlineListCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                for (int i = 0; i < adapter.getCount(); i++) {
                    View item = onlineListCourses.getChildAt(i);
                    if (item != null) {
                        item.setBackgroundColor(Color.WHITE);
                    }
                }

                if (selectedCourse) {
                    view.setBackgroundColor(Color.WHITE);

                    selectedCourse = false;

                    if (courseSummaryButton != null) {
                        courseSummaryButton.setVisibility(View.INVISIBLE);
                    }
                    if (delete != null) {
                        delete.setVisibility(View.INVISIBLE);
                    }
                    courseNumber = -1;


                } else {
                    view.setBackgroundColor(Color.LTGRAY);

                    selectedCourse = true;
                    if (courseSummaryButton != null) {
                        courseSummaryButton.setVisibility(View.VISIBLE);
                    }
                    if (delete != null) {
                        delete.setVisibility(View.VISIBLE);
                    }
                    courseNumber = position;
                }


            }
        });
    }


    private void retrieveOnlineCourses() {
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            db.collection("content").whereEqualTo("authorUID", user.getUid()).whereEqualTo("type", "Course").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    onlineCourses = getCourses(queryDocumentSnapshots.getDocuments());

                    updateListView();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getView().getContext(), "Could not retrieve courses for " +  user.getEmail(), Toast.LENGTH_LONG).show();
                }
            });

        } else {
            Toast.makeText(getView().getContext(), "Teacher must sign-in to retrieve their courses", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updateListView() {
        if (onlineCourses.size() > 0) {
            if (getView() != null) {
                adapter = new ArrayAdapterCourseItemsOnline(getView().getContext(), R.layout.list_item, onlineCourses);
                adapter.loadThumbnails();
                onlineListCourses.setAdapter(adapter);
            }
        }
    }

    private ArrayList<CourseItem> getCourses(List<DocumentSnapshot> courses) {

        ArrayList<CourseItem> courseItems = new ArrayList<>();


        for (DocumentSnapshot course: courses) {
            CourseItem courseItem = new CourseItem((String) course.get("courseName"), (String) course.get("courseUID"), true);
            courseItem.setLanguage((String) course.get("language"));
            courseItem.setCategory((String) course.get("category"));

            courseItems.add(courseItem);
        }
        return courseItems;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void configureCourseSummaryButton() {
        courseSummaryButton = getView().findViewById(R.id.course_summary_button);

        courseSummaryButton.setVisibility(View.INVISIBLE);

        courseSummaryButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if (selectedCourse) {
                    if (courseNumber != -1) {
                        Intent intentSummaryCourse = new Intent(getActivity(), TeachOnlineCourseSummary.class);
                        intentSummaryCourse.putExtra(IntentNames.COURSE, onlineCourses.get(courseNumber));
                        startActivity(intentSummaryCourse);

                    }

                }
            }
        });

    }


}