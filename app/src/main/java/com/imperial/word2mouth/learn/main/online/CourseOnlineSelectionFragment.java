package com.imperial.word2mouth.learn.main.online;

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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.shared.ArrayAdapterCourseItemsOnline;
import com.imperial.word2mouth.shared.CourseItem;
import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.shared.UnzipFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

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


    private ImageButton download = null;
    private ListView listCourses = null;
    private ProgressBar progress = null;

    // List of Courses

    private ArrayAdapterCourseItemsOnline adapter;


    private boolean selectedCourse = false;


    // Parameter (Teacher Name)
    private static final String ARG_PARAM1 = "param1";
    private String teacherName;
    private ArrayList<CourseItem> onlineCourses;

    // Model
    private String courseName = null;
    private CourseItem courseItem = null;
    private String courseIdentification = null;

    public CourseOnlineSelectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment CourseOnlineSelection.
     */
    // TODO: Rename and change types and number of parameters
    public static CourseOnlineSelectionFragment newInstance(String param1) {
        CourseOnlineSelectionFragment fragment = new CourseOnlineSelectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            teacherName = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_course_online_selection, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getPermissions();

        if (hasNecessaryPermissions()) {
            configureUI();
            configureProgressBar();
            configureDownloadButton();
            configureListCourses();
        }
    }

    private void configureProgressBar() {
        progress = getView().findViewById(R.id.progress_download);
        progress.bringToFront();

        progress.setVisibility(View.INVISIBLE);
    }

    private void configureUI() {
        download = getView().findViewById(R.id.download_button);
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

    // UI



    private void configureListCourses() {
        if (listCourses != null) {
            retrieveOnlineCourses();

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
                        selectedCourse = false;

                        if (download != null) {
                            download.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                        }

                        courseName = null;

                    } else {
                        view.setBackgroundColor(Color.LTGRAY);

                        selectedCourse = true;
                        if (download != null) {
                            download.setColorFilter(null);
                        }

                        courseItem = (CourseItem) parent.getAdapter().getItem(position);
                        courseName = courseItem.getCourseName();
                        courseIdentification = courseItem.getCourseOnlineIdentification();
                    }


                }
            });
        }

    }


    private void retrieveOnlineCourses() {

        DatabaseReference teacherRef = FirebaseDatabase.getInstance().getReference(teacherName);

        if (teacherRef != null) {
            teacherRef.addValueEventListener(new ValueEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.getValue() != null) {
                        onlineCourses = getCourses((Map<String, Map<String, String>>) snapshot.getValue());
                        updateListView();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getView().getContext(), "Could Not retrieve courses", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void updateListView() {
        if (onlineCourses.size() > 0) {
            if (getView() != null) {
                adapter = new ArrayAdapterCourseItemsOnline(getView().getContext(), R.layout.list_item, onlineCourses);
                adapter.loadThumbnails(teacherName);
                listCourses.setAdapter(adapter);
            }
        }
    }

    private ArrayList<CourseItem> getCourses(Map<String, Map<String, String>> courses) {

        ArrayList<CourseItem> courseItems = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> entry : courses.entrySet()) {

            courseItems.add(new CourseItem((String) entry.getValue().get("courseName"), (String) entry.getValue().get("key"), true));
        }
        return courseItems;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Download Button



    private void configureDownloadButton() {
        if (download != null) {
            download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedCourse) {
                        StorageReference courseRef = FirebaseStorage.getInstance().getReference(teacherName + "/" + courseName + courseIdentification + "/" + courseName +".zip");
                        progress.setVisibility(View.VISIBLE);
                        File f = new File(getContext().getExternalFilesDir(null).getPath() + DirectoryConstants.zip + courseName + ".zip");
                        if (!f.exists()) {
                            try {
                                f.createNewFile();

                                courseRef.getFile(f).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        moveZipCourse(f.getPath(), getContext().getExternalFilesDir(null).getPath() + DirectoryConstants.offline);
                                        signalCompleteDownload();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            });
        }

        download.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);

    }

    private void moveZipCourse(String sourcePath, String destPath) {
        UnzipFile.unzipFile(sourcePath, destPath);

    }

    private void signalCompleteDownload() {
        Toast.makeText(getContext(), "Download Completed", Toast.LENGTH_SHORT).show();

        progress.setVisibility(View.INVISIBLE);

        courseItem = null;
        courseName = null;
        courseIdentification = null;

    }

}