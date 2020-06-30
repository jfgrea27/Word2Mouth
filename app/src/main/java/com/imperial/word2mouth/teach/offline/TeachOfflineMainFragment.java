package com.imperial.word2mouth.teach.offline;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.shared.ArrayAdapterCourseItemsOffline;
import com.imperial.word2mouth.shared.CourseItem;
import com.imperial.word2mouth.shared.FileHandler;
import com.imperial.word2mouth.shared.FileReader;
import com.imperial.word2mouth.teach.offline.create.TeachCourseCreationSummaryActivity;
import com.imperial.word2mouth.teach.offline.upload.UploadProcedure;
import com.imperial.word2mouth.teach.online.TeachOnlineMainFragment;

import java.io.File;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TeachOfflineMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TeachOfflineMainFragment extends Fragment {

    // Permissions
    private static final int READ_WRITE_PERMISSION = 1;
    private boolean hasReadWriteStorageAccess = false;

    // View
    private ImageButton create = null;
    private ImageButton upload = null;
    private ImageButton delete = null;
    private ListView courseList;
    private ProgressBar uploadProgress = null;



    // Model
    private boolean selectedCourse = false;
    private CourseItem courseItem = null;
    private String courseName = null;
    private String courseIdentification = null;
    private File courseDirectory;
    private String coursePath = null;

    // Adapter for the ListView
    private ArrayList<CourseItem> localCourses = null;
    private ArrayAdapterCourseItemsOffline adapter = null;

    // Upload Listener
    private UploadProcedure uploadProcedure;


    public TeachOfflineMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TeachOfflineMainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TeachOfflineMainFragment newInstance(String param1, String param2) {
        TeachOfflineMainFragment fragment = new TeachOfflineMainFragment();
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

        return inflater.inflate(R.layout.fragment_teach_offline_main, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getPermissions();

        if (hasNecessaryPermissions()) {
            configureCreateButton();
            configureUploadButton();
            configureDeleteButton();
            configureListView();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Permissions
    private void getPermissions() {

        if (!(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED )) {
            Toast.makeText(getView().getContext(), "Please allow access to Storage", Toast.LENGTH_SHORT).show();
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, READ_WRITE_PERMISSION);
        } else {
            hasReadWriteStorageAccess = true;
        }
    }


    private boolean hasNecessaryPermissions() {
        return hasReadWriteStorageAccess;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_WRITE_PERMISSION) {
            if(permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    permissions[1].equals(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasReadWriteStorageAccess = true;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // UI

    // List Of Courses
    private void configureListView() {
        courseList = (ListView) getView().findViewById(R.id.list_view_course_offline);

        localCourses = retrieveLocalCourses();

        if (localCourses.size() > 0) {
            adapter = new ArrayAdapterCourseItemsOffline(getContext(), R.layout.list_item, localCourses);
            courseList.setAdapter(adapter);
            courseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @SuppressLint("ResourceAsColor")
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    for (int i = 0; i < adapter.getCount(); i++) {
                        View item = courseList.getChildAt(i);
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
                        if (upload != null) {
                            upload.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                        }
                        courseName = null;

                    } else {
                        selectedCourse = true;
                        view.setBackgroundColor(Color.LTGRAY);
                        if (delete != null) {
                            delete.setColorFilter(null);
                        }
                        if (upload != null) {
                            upload.setColorFilter(null);
                        }

                        courseItem = (CourseItem) parent.getAdapter().getItem(position);
                        courseName = courseItem.getCourseName();
                        coursePath = courseItem.getCoursePath();
                        courseIdentification = courseItem.getCourseOnlineIdentification();
                    }
                }
            });
        }
    }

    private ArrayList<CourseItem> retrieveLocalCourses() {
        ArrayList<CourseItem> courseItems = new ArrayList<>();

        File directory = new File(getView().getContext().getExternalFilesDir(null) + DirectoryConstants.offline);

        File[] courses = directory.listFiles();

        for (File f : courses) {
            String courseName = FileReader.readTextFromFile(f.getPath()+ "/meta/title.txt");
            String courseIdentification = FileReader.readTextFromFile(f.getPath() + "/meta/identification.txt");

            CourseItem courseItem= new CourseItem(courseName, f.getPath());
            courseItem.setCourseOnlineIdentification(courseIdentification);
            courseItems.add(courseItem);
        }
        return courseItems;
    }

    // Delete Button

    private void configureDeleteButton() {
        delete = getView().findViewById(R.id.delete_button);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedCourse) {
                    if (courseName != null) {
                        File courseFile = new File(courseItem.getCoursePath());
                        if (courseFile.exists()) {
                            FileHandler.deleteRecursive(courseFile);
                            adapter.remove(courseItem);
                            adapter.notifyDataSetChanged();
                            adapter.notifyDataSetInvalidated();
                        }
                    }
                }
                selectedCourse = false;
                upload.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                create.setColorFilter(null);
                delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
            }
        });

        if (delete != null) {
            delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        }
    }

    // Create Button

    private void configureCreateButton() {

        create = getView().findViewById(R.id.create_edit_button);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectedCourse) {
                    intentToCreateCourseAndStartActivity();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getView().getContext());
                    builder.setTitle("Course Name");
                    final EditText input = new EditText(getView().getContext());
                    input.setHint("Type Course Name");
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            courseName = input.getText().toString();
                            if (courseName.isEmpty()) {
                                courseName = "Untitled Course";
                            }
                            courseDirectory = FileHandler.createDirectoryForCourseAndReturnIt(courseName, getView().getContext());
                            coursePath = courseDirectory.getPath();
                            intentToCreateCourseAndStartActivity();
                        }


                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                }
            }
        });
    }

    private void intentToCreateCourseAndStartActivity() {
        Intent createIntent = new Intent(getView().getContext(), TeachCourseCreationSummaryActivity.class);
        createIntent.putExtra("course name", courseName);
        createIntent.putExtra("course directory path", coursePath);
        startActivity(createIntent);
    }


    // Upload Button
    private void configureUploadButton() {
        upload = getView().findViewById(R.id.upload_button);
        uploadProgress = getView().findViewById(R.id.progress_upload);

        if (upload!= null) {
            upload.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        }

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedCourse) {
                    uploadProcedure = new UploadProcedure(courseName, coursePath, courseIdentification, getActivity());

                    uploadProgress.setVisibility(View.VISIBLE);
                    upload.setEnabled(false);


                    uploadProcedure.setListener(new UploadProcedure.UploadListener() {
                        @Override
                        public void onDataLoadedInDatabase() {
                            uploadDataBaseSuccessful();
                        }

                        @Override
                        public void onDataLoadedInStorage(String courseIdentification) {
                            setCourseIdentification(courseIdentification);
                           uploadStorageSuccessful();
                        }
                    });


                    completedDatabase = false;
                    completedStorage = false;

                    uploadProcedure.uploadCourse();

                }
            }
        });
    }

    private void setCourseIdentification(String identification) {
        if (courseIdentification == "") {
            courseIdentification = identification;
           courseItem.setCourseOnlineIdentification(identification);
           updateCourseIdentificationFile();
        }
    }

    private void updateCourseIdentificationFile() {
        FileHandler.createFileForSlideContentAndReturnIt(coursePath + DirectoryConstants.meta , null, null, courseIdentification, FileHandler.ONLINE_IDENTIFICATION);
    }

    private boolean completedDatabase = false;
    private boolean completedStorage = false;

    private void uploadDataBaseSuccessful() {
        completedDatabase = true;
        uploadSuccessful();
    }


    private void uploadStorageSuccessful() {
        completedStorage = true;
        uploadSuccessful();
    }

    private void uploadSuccessful() {
        if (completedDatabase && completedStorage) {
            uploadProgress.setVisibility(View.GONE);
//            Toast.makeText(getView().getContext(), "Upload Successful", Toast.LENGTH_SHORT).show();
            upload.setEnabled(true);
        }
    }
}