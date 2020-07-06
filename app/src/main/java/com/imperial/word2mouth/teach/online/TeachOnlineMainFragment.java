package com.imperial.word2mouth.teach.online;

import android.Manifest;
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
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.shared.ArrayAdapterCourseItemsOnline;
import com.imperial.word2mouth.shared.CourseItem;
import com.imperial.word2mouth.teach.TeachActivityMain;
import com.imperial.word2mouth.teach.offline.upload.database.DataTransferObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    // Internet Permissison

    private ListView onlineListCourses;
    private ImageButton delete;

    // Courses
    private ArrayList<CourseItem> onlineCourses = null;


    // Adapter
    private ArrayList<CourseItem> localCourses = null;
    private ArrayAdapterCourseItemsOnline adapter = null;


    // Online
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private boolean selectedCourse = false;

    // Course data
    private String courseName = null;
    private CourseItem courseItem = null;
    private String courseIdentification = null;

    public TeachOnlineMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LearnOnlineMainFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        if (hasNecessaryPermissions()) {
            configureDeleteButton();
            configureListCourses();
        }

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

                    if (delete != null) {
                        delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    }

                    courseName = null;

                } else {
                    view.setBackgroundColor(Color.LTGRAY);

                    selectedCourse = true;
                    if (delete != null) {
                        delete.setColorFilter(null);
                    }

                    courseItem = (CourseItem) parent.getAdapter().getItem(position);
                    courseName = courseItem.getCourseName();
                    courseIdentification = courseItem.getCourseOnlineIdentification();
                }


            }
        });
    }


    private void retrieveOnlineCourses() {
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            db.collection("content").whereEqualTo("userUID", user.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
            CourseItem courseItem = new CourseItem((String) course.get("courseName"), (String) course.get("key"), true);
            courseItem.setLanguage((String) course.get("language"));
            courseItem.setCategory((String) course.get("category"));

            courseItems.add(courseItem);
        }
        return courseItems;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void configureDeleteButton() {
        delete = getView().findViewById(R.id.delete_button);

        delete.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if (selectedCourse) {

                    if (courseName != null) {


                        // Delete from Firebase
                        db.collection("content").document(courseIdentification).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getView().getContext(), "Successfully deleted from the Database", Toast.LENGTH_SHORT).show();

                            }
                        });
//                        DatabaseReference courseDatabaseRef = FirebaseDatabase.getInstance().getReference("/content/" + courseIdentification);
//                        courseDatabaseRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                            }
//                        });


                        StorageReference courseDirectoryRef = FirebaseStorage.getInstance().getReference("/content/" + courseName + courseIdentification);
                        StorageReference courseStorageRef = courseDirectoryRef.child(courseName + ".zip");
                        courseStorageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getView().getContext(), "Course zip has been deleted", Toast.LENGTH_SHORT).show();

                            }
                        });

                        StorageReference coursePhotoThumbnailRef = courseDirectoryRef.child("Photo Thumbnail");
                        coursePhotoThumbnailRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getView().getContext(), "Course Photo Thumbnail has been deleted", Toast.LENGTH_SHORT).show();
                            }
                        });

                        StorageReference courseAudioThumbnail = courseDirectoryRef.child("Sound Thumbnail");
                        courseAudioThumbnail.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getView().getContext(), "Course Audio Thumbnail has been deleted", Toast.LENGTH_SHORT).show();

                            }
                        });

                        courseDirectoryRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getView().getContext(), "Course Directory has been successfully deleted", Toast.LENGTH_SHORT).show();
                            }
                        });


                        // delete from the list adapter
                        adapter.remove(courseItem);
                        adapter.notifyDataSetChanged();
                        adapter.notifyDataSetInvalidated();

                        selectedCourse = false;

                        courseName = null;
                        courseIdentification = null;
                        courseItem = null;

                        delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);

                    }

                }
            }
        });

        if (delete != null) {
            delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        }
    }


}