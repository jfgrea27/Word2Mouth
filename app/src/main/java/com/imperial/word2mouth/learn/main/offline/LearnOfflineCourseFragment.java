package com.imperial.word2mouth.learn.main.offline;

import android.Manifest;
import android.annotation.SuppressLint;
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

import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.imperial.word2mouth.learn.main.LearnActivityMain;
import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.shared.IntentNames;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.learn.main.offline.share.bluetooth.ShareBluetoothActivity;
import com.imperial.word2mouth.shared.adapters.ArrayAdapterCourseOffline;
import com.imperial.word2mouth.shared.CourseItem;
import com.imperial.word2mouth.shared.FileHandler;
import com.imperial.word2mouth.shared.FileReaderHelper;


import java.io.File;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LearnOfflineCourseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LearnOfflineCourseFragment extends Fragment {

    // Permission
    private int READ_WRITE_PERMISSION = 1;

    private boolean hasReadWriteStorageAccess = false;

    // Button View
    private ImageButton learn;
    private ImageButton share;
    private ImageButton delete;

    // ListView
    private ListView courseList;
    private ArrayAdapterCourseOffline adapter;

    // Model
    private ArrayList<CourseItem> localCourses = null;
    private CourseItem courseItem = null;

    private boolean selectedCourse = false;
    private String courseName = null;

    public LearnOfflineCourseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LearnOfflineMainFragment.
     */
    public static LearnOfflineCourseFragment newInstance() {
        LearnOfflineCourseFragment fragment = new LearnOfflineCourseFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_learn_offline_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        configureLearnButton();
        configureDeleteButton();
        configureShareButton();
        configureListView();

        getPermissions();

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {

            // Refresh tab data:

            if(getFragmentManager() != null) {

                getFragmentManager()
                        .beginTransaction()
                        .detach(this)
                        .attach(this)
                        .commit();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Permissions

    private void getPermissions() {
        if (!(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(getView().getContext(), "Please allow access to Storage", Toast.LENGTH_SHORT).show();
            int permissionCheck = 0;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, READ_WRITE_PERMISSION);
            }
        } else{
            hasReadWriteStorageAccess = true;
        }
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


    /////////////////////////////////////////////////////////////////////////////////////////////////



    ////////////////////////////////////////////////////////////////////////////////////////////////

    // UI
    private void configureListView() {
        courseList = getView().findViewById(R.id.list_view_course_offline);
        if (hasReadWriteStorageAccess) {
            localCourses = retrieveLocalCourses();

            if (localCourses.size() > 0) {
                adapter = new ArrayAdapterCourseOffline(getContext(), R.layout.list_item, localCourses);

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
                            if (learn != null) {
                                learn.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                            }
                            if (delete != null) {
                                delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                            }
                            courseName = null;
                        } else {
                            selectedCourse = true;
                            view.setBackgroundColor(Color.LTGRAY);
                            if (learn != null) {
                                learn.setColorFilter(null);
                            }
                            if (delete != null) {
                                delete.setColorFilter(null);
                            }
                            courseItem = (CourseItem) parent.getAdapter().getItem(position);
                            courseName = courseItem.getCourseName();
                        }
                    }
                });
            }

        }

    }

    private ArrayList<CourseItem> retrieveLocalCourses() {
        ArrayList<CourseItem> courseItems = new ArrayList<>();

        File directory = new File(getView().getContext().getExternalFilesDir(null) + DirectoryConstants.offline);

        File[] courses = directory.listFiles();

        for (File f : courses) {

            String courseName = FileReaderHelper.readTextFromFile(f.getPath()+ DirectoryConstants.meta + DirectoryConstants.title);
            String courseLanguage = FileReaderHelper.readTextFromFile(f.getPath()+ DirectoryConstants.meta + DirectoryConstants.language);
            String courseCategory = FileReaderHelper.readTextFromFile(f.getPath()+ DirectoryConstants.meta + DirectoryConstants.category);

            CourseItem courseItem= new CourseItem(courseName, f.getPath());

            courseItem.setCategory(courseCategory);
            courseItem.setLanguage(courseLanguage);

            courseItems.add(courseItem);
        }
        return courseItems;
    }


    // Share
    private void configureShareButton() {
        share = getView().findViewById(R.id.share_button);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hasReadWriteStorageAccess) {

                    Intent sharingIntent = new Intent(getView().getContext(), ShareBluetoothActivity.class);
                    if (selectedCourse) {
                        sharingIntent.putExtra(IntentNames.COURSE_PATH, courseItem.getCoursePath());
                        sharingIntent.putExtra(IntentNames.COURSE_NAME, courseName);
                    }
                    startActivity(sharingIntent);
                } else {
                    Toast.makeText(getView().getContext(), "Need Storage Permission For Share Button", Toast.LENGTH_SHORT).show();
                }

            }
        });


        share.setOnLongClickListener(new View.OnLongClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onLongClick(View v) {
                LearnActivityMain activity = (LearnActivityMain) getActivity();
                return true;
            }
        });
    }

    // Learn
    private void configureLearnButton() {
        learn = getView().findViewById(R.id.learn_button);
        learn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hasReadWriteStorageAccess) {
                    if (selectedCourse) {
                        if (courseName != null) {
                            Intent learnIntent = new Intent(getView().getContext(), LearnOfflineCourseSummary.class);
                            learnIntent.putExtra(IntentNames.COURSE_NAME, courseName);
                            learnIntent.putExtra(IntentNames.COURSE_PATH, getView().getContext().getExternalFilesDir(null) + DirectoryConstants.offline + courseName);
                            startActivity(learnIntent);
                        }
                    }
                } else {
                    Toast.makeText(getView().getContext(), "Need Storage Permission For Learn Button", Toast.LENGTH_SHORT).show();
                }
            }
        });


        if (learn != null) {
            learn.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        }
    }

    // Delete
    private void configureDeleteButton() {
        delete = getView().findViewById(R.id.delete_button);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasReadWriteStorageAccess) {
                    if (selectedCourse) {
                        if (courseName != null) {
                            File courseFile = new File(courseItem.getCoursePath());
                            if (courseFile.exists()) {
                                FileHandler.deleteRecursive(courseFile);
                                adapter.remove(courseItem);
                                adapter.notifyDataSetChanged();
                                adapter.notifyDataSetInvalidated();
                            }
                            selectedCourse = false;
                            learn.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                            delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);

                        } else {
                            Toast.makeText(getView().getContext(), "Need Storage Permission For Learn Button", Toast.LENGTH_SHORT).show();

                        }
                    }
                }
            }
        });

        if (delete != null) {
            delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


}
