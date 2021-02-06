package com.imperial.word2mouth.previous.teach.offline;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.imperial.word2mouth.previous.main.online.dialog.DialogCategory;
import com.imperial.word2mouth.previous.main.online.dialog.DialogLanguage;
import com.imperial.word2mouth.previous.shared.DirectoryConstants;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.previous.shared.adapters.ArrayAdapterCourseOffline;
import com.imperial.word2mouth.previous.shared.CourseItem;
import com.imperial.word2mouth.previous.shared.FileHandler;
import com.imperial.word2mouth.previous.shared.FileReaderHelper;
import com.imperial.word2mouth.previous.shared.IntentNames;
import com.imperial.word2mouth.previous.teach.TeachActivityMain;
import com.imperial.word2mouth.previous.teach.offline.upload.UploadProcedure;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

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
    private ArrayAdapterCourseOffline adapter = null;

    // Upload Listener
    private UploadProcedure uploadProcedure;

    private String selectedLanguage;
    private String selectedCategory;
    private String courseLanguage;
    private String courseCategory;
    private String courseAuthorID;

    private boolean completedDatabase = false;
    private boolean completedStorage = false;

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private int courseNumber = -1;


    public TeachOfflineMainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TeachOfflineMainFragment.
     */
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

        configureLongClicks();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Permissions
    private void getPermissions() {

        if (!(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED )) {
            Toast.makeText(getView().getContext(), R.string.accessStorage, Toast.LENGTH_SHORT).show();
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
                        if (delete != null) {
                            delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                        }
                        if (upload != null) {
                            upload.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                        }
                        courseNumber = -1;

                    } else {
                        selectedCourse = true;
                        view.setBackgroundColor(Color.LTGRAY);
                        if (delete != null) {
                            delete.setColorFilter(null);
                        }
                        if (upload != null) {
                            upload.setColorFilter(null);
                        }

                        courseNumber = position;
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
            String courseName = FileReaderHelper.readTextFromFile(f.getPath()+ "/meta/title.txt");
            String courseIdentification = FileReaderHelper.readTextFromFile(f.getPath() + "/meta/identification.txt");

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
                if (courseNumber > -1) {
                    File courseFile = new File(localCourses.get(courseNumber).getCoursePath());
                    File lectureFolder = new File(courseFile.getPath() + DirectoryConstants.lectures);
                    File[] lectures = lectureFolder.listFiles();

                    for (File l : lectures) {
                        String version = FileReaderHelper.readTextFromFile(l.getPath() + DirectoryConstants.meta + DirectoryConstants.versionLecture);
                        File f = new File(getActivity().getExternalFilesDir(null )+ DirectoryConstants.cache + version + ".txt");
                        if (f.exists()) {
                            f.delete();
                        }
                    }


                    if (courseFile.exists()) {
                        FileHandler.deleteRecursive(courseFile);
                        adapter.remove(localCourses.get(courseNumber));
                        adapter.notifyDataSetChanged();
                        adapter.notifyDataSetInvalidated();
                    }

                    courseNumber = -1;
                    upload.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    create.setColorFilter(null);
                    delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                }
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
                if(courseNumber > -1) {
                    intentToCreateCourseAndStartActivity();
                } else {
                    dialogNameCourse();
                }
            }
        });
    }

    private void dialogNameCourse() {
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
                dialogLanguageSelection();

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

    public void dialogLanguageSelection() {

        DialogLanguage dialogLanguage = new DialogLanguage(getView(), this);
        dialogLanguage.buildDialog(DialogLanguage.CREATE);

    }

    public void dialogCategorySelection() {
        DialogCategory dialogCategory = new DialogCategory(getView(), this);
        dialogCategory.buildDialog(DialogLanguage.CREATE);

    }

    private void intentToCreateCourseAndStartActivity() {
        Intent createIntent = new Intent(getView().getContext(), TeachOfflineCourseSummary.class);

        if (courseNumber > -1) {
            createIntent.putExtra(IntentNames.COURSE, localCourses.get(courseNumber));
            startActivity(createIntent);
        } else {
            CourseItem newCourse = new CourseItem(courseName, coursePath);

            courseItem = new CourseItem(courseName, coursePath);
            newCourse.setCourseBluetooth(FileReaderHelper.readTextFromFile(coursePath + DirectoryConstants.courseBluetooth));
            newCourse.setLanguage(selectedLanguage);
            newCourse.setCategory(selectedCategory);
            newCourse.setCourseName(courseName);
            newCourse.setCoursePath(coursePath);
            createIntent.putExtra(IntentNames.COURSE, newCourse);
            startActivity(createIntent);
        }


    }


    private boolean checkNewAuthor() {
        File f = new File(coursePath + DirectoryConstants.meta + DirectoryConstants.author);

        if (f.length() == 0) {
            return true;
        }

        Scanner fileReader = null;
        try {
            fileReader = new Scanner(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String s = fileReader.nextLine();

        if (user.getUid().equals(s)) {
            return true;
        }

        return false;
    }


    // Upload Button
    private void configureUploadButton() {
        upload = getView().findViewById(R.id.upload_button);
        uploadProgress = getView().findViewById(R.id.progress_upload);

        uploadProgress.bringToFront();
        if (upload!= null) {
            upload.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        }

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(courseNumber > -1) {

                    if (user != null) {
                        String authorCourse = FileReaderHelper.readTextFromFile(localCourses.get(courseNumber).getCoursePath() + DirectoryConstants.meta + DirectoryConstants.author);

                        if (user.getUid().equals(authorCourse) || authorCourse == "") {
                            localCourses.get(courseNumber).setAuthorID(authorCourse);
                            uploadCourse();
                        } else {
                            Toast.makeText(getView().getContext(), R.string.creatingTeacherOnly, Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(getView().getContext(), R.string.mustCreateAccount, Toast.LENGTH_SHORT).show();

                    }
                }

            }
        });
    }

    private void  uploadCourse() {
            uploadProgress.setVisibility(View.VISIBLE);
            uploadProgress.bringToFront();
            localCourses.get(courseNumber).setCourseBluetooth(FileReaderHelper.readTextFromFile(localCourses.get(courseNumber).getCoursePath() + DirectoryConstants.meta + DirectoryConstants.courseBluetooth));
            UploadProcedure uploadProcedure = new UploadProcedure(localCourses.get(courseNumber), getActivity());

            uploadProcedure.setListener(new UploadProcedure.UploadListener() {
                @Override
                public void onDataLoadedInDatabase() {
                    uploadDataBaseSuccessful();
                }

                @Override
                public void onDataLoadedInStorage(String courseIdentification, String lectureIdentification) {

                }

                @Override
                public void onDataLoadedInStorageEntireCourse(String courseIdentification, String lectureIdentification, String lecturePath) {
                    setCourseIdentification(courseIdentification);
                    setLectureIdentification(lectureIdentification, lecturePath);
                    uploadStorageSuccessful();
                }
            });

            uploadProcedure.uploadCourse(true);
    }

    private void setLectureIdentification(String identification, String lecturePath) {

        File course = new File(localCourses.get(courseNumber).getCoursePath() + DirectoryConstants.lectures);

        File[] lectures = course.listFiles();
        for (File lecture : lectures) {
            if (lecture.getPath().equals(lecturePath)) {
                FileHandler.createFileForSlideContentAndReturnIt(lecture.getPath() + DirectoryConstants.meta , null, null, identification, FileHandler.ONLINE_LECTURE_IDENTIFICATION);
            }
        }
    }


    public static void enableDisableViewGroup(ViewGroup viewGroup, boolean enabled) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = viewGroup.getChildAt(i);
            view.setEnabled(enabled);
            if (view instanceof ViewGroup) {
                enableDisableViewGroup((ViewGroup) view, enabled);
            }
        }
    }


    private void setCourseIdentification(String identification) {
        if (courseIdentification == "") {
            courseIdentification = identification;
           courseItem.setCourseOnlineIdentification(identification);
           updateCourseIdentificationFile();
        }
    }

    private void updateCourseIdentificationFile() {
        FileHandler.createFileForSlideContentAndReturnIt(coursePath + DirectoryConstants.meta , null, null, courseIdentification, FileHandler.ONLINE_COURSE_IDENTIFICATION);
    }



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
            Toast.makeText(getView().getContext(), R.string.uploadSuccessful, Toast.LENGTH_SHORT).show();
            upload.setVisibility(View.VISIBLE);
            uploadProgress.setVisibility(View.INVISIBLE);
            enableDisableViewGroup((ViewGroup) getView().getParent(), true);

        }
    }

    public void setLanguageSelected(String s) {
        selectedLanguage = s;
    }

    public String getLanguageSelected() {
        return selectedLanguage;
    }

    public void setSelectedCategory(String s) {
        selectedCategory = s;
    }

    public String getCategorySelected() {
        return selectedCategory;
    }

    public void createCourse() {
        courseDirectory = FileHandler.createDirectoryForCourseAndReturnIt(courseName, getView().getContext());
        coursePath = courseDirectory.getPath();
        FileHandler.createDirectoryAndReturnIt(courseDirectory.getPath(), FileHandler.META);
        FileHandler.createFileForSlideContentAndReturnIt(coursePath + DirectoryConstants.meta , null, null, selectedLanguage, FileHandler.LANGUAGE_SELECTION);
        FileHandler.createFileForSlideContentAndReturnIt(coursePath + DirectoryConstants.meta , null, null,selectedCategory, FileHandler.CATEGORY_SELECTION);
        intentToCreateCourseAndStartActivity();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////


    private void configureLongClicks() {
        create.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TeachActivityMain act = (TeachActivityMain) getActivity();
                if (selectedCourse) {
                    act.speak(getString(R.string.editCourse));
                } else {
                    act.speak(getString(R.string.createCourse));
                }
                return true;
            }
        });

        upload.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TeachActivityMain act = (TeachActivityMain) getActivity();
                act.speak(getString(R.string.uploadCourse));
                return true;            }
        });
        delete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TeachActivityMain act = (TeachActivityMain) getActivity();
                act.speak(getString(R.string.delete));
                return true;
            }
        });
    }

}