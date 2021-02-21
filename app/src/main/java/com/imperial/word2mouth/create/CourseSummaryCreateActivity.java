package com.imperial.word2mouth.create;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.imperial.word2mouth.AppCodes;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.common.dialog.ImageDialog;
import com.imperial.word2mouth.common.dialog.LectureNameDialog;
import com.imperial.word2mouth.helpers.FileSystemHelper;
import com.imperial.word2mouth.model.CourseItem;
import com.imperial.word2mouth.model.LectureItem;
import com.imperial.word2mouth.helpers.FileSystemConstants;
import com.imperial.word2mouth.previous.shared.FileReaderHelper;
import com.imperial.word2mouth.IntentNames;
import com.imperial.word2mouth.previous.shared.PrevLectureItem;
import com.imperial.word2mouth.previous.shared.adapters.ArrayAdapterLectureOffline;
import com.imperial.word2mouth.previous.teach.offline.upload.UploadProcedure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class CourseSummaryCreateActivity extends AppCompatActivity implements ImageDialog.OnInputListener {

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Extras
    private CourseItem courseItem;

    //////////////////////////////////////////////////////////////////////////////////////////////
    // UI
    //////////////////////////////////////////////////////////////////////////////////////////////

    /////////// Image Thumbnail
    // View
    private ImageView thumbnail;
    // Model
    private Uri imageUri = null;

    //////// Audio Thumbnail
    // View
    private ImageButton audioPreview;
    private ImageButton audioButton;
    // Model
    private File audioFile;
    // Controller
    private Uri audioUri = null;
    private AudioRecorder recorder;
    private MediaPlayer player;
    private boolean recording = false;


    ////// Title Course
    // View
    private TextView courseNameView;

    ////// List View of Lectures
    // View
    private ListView lecturesView;
    // Model
//    private ArrayList<LectureItem> localLectures;
    private int lectureNumber = -1;

    // Controller
    private ArrayAdapterLectureOffline adapter;
    private boolean selectedLecture = false;

    /////// Delete Button
    // View
    private ImageButton delete;
    // Controller

    /////// Create Button
    // View
    private ImageButton create;
    // Model
    private String lectureName;
    // Controller

    /////// Upload Button
    // View
    private ImageButton upload;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    // Model

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Firebase
    ///////////////////////////////////////////////////////////////////////////////////////////
    // TODO complete this when you get to it
    private UploadProcedure uploadProcedure;
    private String courseAuthorID;
    private String courseLanguage;
    private String courseCategory;
    private PrevLectureItem prevLectureItem;
    private String courseIdentification = "";
    private boolean completedDatabase = false;
    private boolean completedStorage = false;
    private ProgressBar uploadProgress;
    // Controller
    ///////////////////////////////////////////////////////////////////////////////////////////
    // TTS
    ///////////////////////////////////////////////////////////////////////////////////////////
    private TextToSpeech textToSpeech;

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Model
    ///////////////////////////////////////////////////////////////////////////////////////////
    private LectureItem lectureItem;

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Activity Lifecycle
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_offline_course_summary);


        getExtras();

        // UI
        //configureListViewLectures();
        configureCreateButton();
//        configureDeleteButton();
//        configureCourseName();
        configureCourseThumbnail();
        configureAudio();
//        configureUploadButton();
//
//        // TTS
//        configureTTS();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveMetaData();
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    // Saving Data for Course Selection
    private void saveMetaData() {
        if (imageUri != null) {
            FileSystemHelper.saveImageVideoFile(courseItem.getCourseImageThumbnailPath(), imageUri, getContentResolver());
        }

        if (audioUri != null) {
            FileSystemHelper.saveAudioFile(courseItem.getCourseAudioThumbnailPath(), audioUri, getContentResolver());
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Get Extras
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void getExtras() {
        courseItem = (CourseItem) getIntent().getParcelableExtra(IntentNames.COURSE);
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    /////////////////////////////////////////////////////////////////////////////////////////////////
    public void setLectureName(String lectureName) {
        this.lectureName = lectureName;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    // UI
    /////////////////////////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Create Button

    private void configureCreateButton() {
        create = findViewById(R.id.edit_button);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMetaData();

                if (lectureNumber > -1) {
                    //CourseSummaryCreateActivity.this.lectureItem = localLectures.get(lectureNumber);
                    intentCreateEditLecture();
                } else {
                    dialogLectureName();
                }

            }
        });

    }

    // Create New Lecture
    private void dialogLectureName() {
        LectureNameDialog lectureNameDialog = new LectureNameDialog(CourseSummaryCreateActivity.this);
        lectureNameDialog.show(getSupportFragmentManager(), getString(R.string.lecture_name));
    }

    public void createLecture() {
        this.lectureItem = new LectureItem(this.courseItem, this.lectureName);
        this.lectureItem= FileSystemHelper.createLectureFileSystem(lectureItem, getApplicationContext());
        intentCreateEditLecture();
    }

    // Create/Edit Lecture
    private void intentCreateEditLecture() {
        Intent createEditIntent = new Intent(CourseSummaryCreateActivity.this, LectureSummaryCreateActivity.class);
        createEditIntent.putExtra(IntentNames.LECTURE, lectureItem);
        startActivity(createEditIntent);
    }


    // Pop Up Dialogs

    private void fetchAndCreateInformationAboutCourse() {

        // TODO Add audio file if person presses buttont to add one
//        audioFile = FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), null, getContentResolver(), null, FileHandler.AUDIO );
        // TODO Implement JSON Reader metadata
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Delete Button
    private void configureDeleteButton() {
        delete = findViewById(R.id.delete_button);
//
//        delete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (selectedLecture) {
//
//                    String version = FileReaderHelper.readTextFromFile(localLectures.get(lectureNumber).getLecturePath() + FileSystemConstants.meta + FileSystemConstants.versionLecture);
//                    File f = new File(getExternalFilesDir(null) + FileSystemConstants.cache + version + ".txt");
//                    if (f.exists()) {
//                        f.delete();
//                    }
//
//                    FileHandler.deleteRecursive(new File (lectureItem.getLecturePath() + "/" + localLectures.get(lectureNumber).getLectureName()));
//                    selectedLecture = false;
//
//                    delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
//                    delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
//                    adapter.remove(localLectures.get(lectureNumber));
//                    adapter.notifyDataSetInvalidated();
//                    adapter.notifyDataSetChanged();
//                    lectureNumber = -1;
//                }
//            }
//        });
//
//
//        if (delete != null) {
//            delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
//        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Image Thumbnail

    // Thumbnail
    private void configureCourseThumbnail() {
        thumbnail = findViewById(R.id.list_item_thumbnail);

        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageDialog imageDialog = new ImageDialog(ImageDialog.THUMBNAIL, CourseSummaryCreateActivity.this);
                imageDialog.show(getSupportFragmentManager(), getString(R.string.thumbnail_selection));
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Bitmap bitmapImage = null;
            switch (requestCode) {
                case AppCodes.GALLERY_SELECTION:
                    try {
                        bitmapImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case AppCodes.CAMERA_ROLL_SELECTION: {
                    bitmapImage = (Bitmap) data.getExtras().get("data");
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + requestCode);
            }
            if (bitmapImage != null) {
                FileSystemHelper.saveImageVideoFile(courseItem.getCourseImageThumbnailPath(), bitmapImage);
                imageUri = Uri.fromFile(new File(courseItem.getCourseImageThumbnailPath()));
                thumbnail.setImageBitmap(bitmapImage);
            }
        }
    }

    @Override
    public void sendInput(int choice) {
        switch (choice) {
            case AppCodes.GALLERY_SELECTION: {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"), AppCodes.GALLERY_SELECTION);
                break;
            }
            case AppCodes.CAMERA_ROLL_SELECTION: {
                Intent rollIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(rollIntent, AppCodes.CAMERA_ROLL_SELECTION);
                break;
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Audio Button

    private void configureAudio() {
        recorder = new AudioRecorder();
        // play button
        audioPreview = findViewById(R.id.course_audio_play);
        audioButton = findViewById(R.id.list_audio_button);

        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recording) {
                    Toast.makeText(CourseSummaryCreateActivity.this, R.string.startRecording, Toast.LENGTH_SHORT).show();
                    audioButton.setColorFilter(Color.RED);
                    recorder.startRecording(audioFile.getPath());
                    recording = true;
                } else {
                    Toast.makeText(CourseSummaryCreateActivity.this, R.string.stopRecording, Toast.LENGTH_SHORT).show();
                    recorder.stopRecording();
                    audioButton.setColorFilter(Color.BLACK);
                    audioUri = Uri.fromFile(audioFile);
                    audioPreview.setVisibility(View.VISIBLE);
                    recording = false;
                }
            }
        });

        audioPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioUri != null) {
                    player = MediaPlayer.create(CourseSummaryCreateActivity.this, audioUri);
                    player.start();
                }
            }
        });

        audioFile = new File(courseItem.getCourseAudioThumbnailPath());
        if (audioFile.length() != 0) {
            audioUri = Uri.fromFile(audioFile);
            audioPreview.setVisibility(View.VISIBLE);
        } else {
            audioPreview.setVisibility(View.INVISIBLE);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    // List of Lectures

    private void configureListViewLectures() {
        lecturesView = findViewById(R.id.lecture_list_view);

//        localLectures = retrieveLocalLectures();
//
//        if (localLectures.size() > 0) {
//            adapter = new ArrayAdapterLectureOffline(CourseSummaryCreateActivity.this, R.layout.list_lectures, localLectures);
//            lecturesView.setAdapter(adapter);
//        }
//
//        lecturesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                for (int i = 0; i < adapter.getCount(); i++) {
//                    View item = lecturesView.getChildAt(i);
//                    if (item != null) {
//                        item.setBackgroundColor(Color.WHITE);
//                    }
//                }
//
//
//                if (selectedLecture) {
//                    view.setBackgroundColor(Color.WHITE);
//                    selectedLecture = false;
//                    if (delete != null) {
//                        delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
//                    }
//                    if (upload != null) {
//                        upload.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
//
//                    }
//                    lectureNumber = -1;
//
//
//                } else {
//                    selectedLecture = true;
//                    lectureNumber = position;
//                    prevLectureItem.setPath(localLectures.get(position).getLecturePath());
//                    view.setBackgroundColor(Color.LTGRAY);
//                    if (delete != null) {
//                        delete.setColorFilter(null);
//                    }
//                    if (upload != null) {
//                        upload.setColorFilter(null);
//                    }
//                }
//            }
//        });

    }

    private ArrayList<PrevLectureItem> retrieveLocalLectures() {

        ArrayList<PrevLectureItem> prevLectureItems = new ArrayList<>();

        File lectureDirectory = new File(courseItem.getCourseLecturePath());
        File[] lectureItemsFiles = lectureDirectory.listFiles();
        if (lectureItemsFiles != null) {

            for (File f : lectureItemsFiles) {

                String lectureName;


                // Title
                lectureName = FileReaderHelper.readTextFromFile(f.getPath()+ FileSystemConstants.meta + FileSystemConstants.title);
                PrevLectureItem item = new PrevLectureItem(courseItem.getCourseName(), lectureName);
                item.setPath(f.getPath());

                prevLectureItems.add(item);
            }
        }

        return prevLectureItems;

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Title


    private void configureCourseName() {
        courseNameView = findViewById(R.id.list_item_text);
        courseNameView.setText(courseItem.getCourseName());
    }






    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Upload Button

    private void configureUploadButton() {
        upload = findViewById(R.id.upload_button);

        uploadProgress = findViewById(R.id.progress_upload);
        uploadProgress.bringToFront();

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedLecture) {
                    if (user != null) {

                        String authorID = FileReaderHelper.readTextFromFile(courseItem.getCoursePath() + FileSystemConstants.meta + FileSystemConstants.author);

                        if (user.getUid().equals(authorID) || authorID == "") {
                            uploadCourse();
                        } else {
                            // TODO Think of a better way than Toast?
                            Toast.makeText(CourseSummaryCreateActivity.this, R.string.creatingTeacherOnly, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CourseSummaryCreateActivity.this, R.string.mustCreateAccount, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        upload.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
    }

    //TODO Complete this method for Firebase (Methdos are comented below)

    private void uploadCourse() {
//        uploadProgress.setVisibility(View.VISIBLE);
//        uploadProgress.bringToFront();
//
//        setCourseAuthorIdentification();
//        localLectures.get(lectureNumber).setLanguage(courseItem.getCourseLanguage());
//        localLectures.get(lectureNumber).setCategory(courseItem.getCourseTopic());
//        localLectures.get(lectureNumber).setLectureIdentification(getLectureIdentification());
//        localLectures.get(lectureNumber).setBluetoothCourse(FileReaderHelper.readTextFromFile(courseItem.getCoursePath() + DirectoryConstants.meta + DirectoryConstants.courseBluetooth));
//        localLectures.get(lectureNumber).setBluetoothLecture(FileReaderHelper.readTextFromFile(lectureItem.getLecturePath() + DirectoryConstants.meta + DirectoryConstants.lectureBluetooth));
//
//        uploadProcedure = new UploadProcedure(courseItem, localLectures.get(lectureNumber), CourseSummaryCreateActivity.this);
//
//        uploadProcedure.setListener(new UploadProcedure.UploadListener() {
//            @Override
//            public void onDataLoadedInDatabase() {
//                uploadDataBaseSuccessful();
//            }
//
//            @Override
//            public void onDataLoadedInStorage(String courseIdentification, String lectureIdentification) {
//                setCourseIdentification(courseIdentification);
//                setLectureIdentification(lectureIdentification);
//                uploadStorageSuccessful();
//            }
//
//            @Override
//            public void onDataLoadedInStorageEntireCourse(String courseIdentification, String lectureIdentification, String lecturePath) {
//
//            }
//        });
//
//        uploadProcedure.uploadCourse();
    }
//    private String getLectureIdentification() {
//        return FileReaderHelper.readTextFromFile(localLectures.get(lectureNumber).getLecturePath() + DirectoryConstants.meta + DirectoryConstants.lectureIdentifcation);
//    }

//
//    private void setCourseIdentification(String identification) {
////        if (courseIdentification == "") {
////            courseIdentification = identification;
////            courseItem.setCourseOnlineIdentification(identification);
////            lectureItem.setCourseIdentification(identification);
////            updateCourseIdentificationFile();
////        }
//    }
//
//    private void updateCourseIdentificationFile() {
//        FileHandler.createFileForSlideContentAndReturnIt(courseItem.getCoursePath() + DirectoryConstants.meta , null, null, courseIdentification, FileHandler.ONLINE_COURSE_IDENTIFICATION);
//        FileHandler.createFileForSlideContentAndReturnIt(lectureItem.getLecturePath() + DirectoryConstants.meta , null, null, courseIdentification, FileHandler.ONLINE_COURSE_IDENTIFICATION);
//
//    }
//
//
//    private void setCourseAuthorIdentification() {
////        if (courseItem.getAuthorID().isEmpty()) {
////            courseAuthorID = user.getUid();
////            courseItem.setAuthorID(user.getUid());
////            updateCourseAuthorFile(user.getUid());
////        }
//    }
//
//    private void updateCourseAuthorFile(String uid) {
//        FileHandler.createFileForSlideContentAndReturnIt(coursePath + DirectoryConstants.meta , null, null, uid, FileHandler.AUTHOR);
//        FileHandler.createFileForSlideContentAndReturnIt(lectureItem.getLecturePath() + DirectoryConstants.meta , null, null, uid, FileHandler.AUTHOR);
//
//    }
//
//
//    private void setLectureIdentification(String identification) {
//        if (lectureItem.getLectureIdentification().isEmpty()) {
//            lectureItem.setLectureIdentification(identification);
//            updateLectureIdentification();
//
//        }
//    }
//
//    private void updateLectureIdentification() {
//        FileHandler.createFileForSlideContentAndReturnIt(lectureItem.getLecturePath() + DirectoryConstants.meta , null, null, lectureItem.getLectureIdentification(), FileHandler.ONLINE_LECTURE_IDENTIFICATION);
//
//    }

    private boolean checkIsAuthorOfCourse() {
//        File f = new File(coursePath + DirectoryConstants.meta + DirectoryConstants.author);
//
//        if (f.length() == 0) {
//            return true;
//        }
//
//        Scanner fileReader = null;
//        try {
//            fileReader = new Scanner(f);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        String s = fileReader.nextLine();
//
//        if (user.getUid().equals(s)) {
//            return true;
//        }
//
//        return false;
        return false;
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
            Toast.makeText(CourseSummaryCreateActivity.this, R.string.uploadSuccessful, Toast.LENGTH_SHORT).show();
            upload.setEnabled(true);

        }
    }




/////////////////////////////////////////////////////////////////////////////////////////////
// TTS
/////////////////////////////////////////////////////////////////////////////////////////////


    public void speak(String string) {
        textToSpeech.speak(string, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void configureTTS() {
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(CourseSummaryCreateActivity.this, R.string.languageNotSupported, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CourseSummaryCreateActivity.this, R.string.initializationFailedSST, Toast.LENGTH_SHORT).show();
                }
            }
        });
        configureLongClicks();
    }

    public void configureLongClicks() {
        delete.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.delete));
                return true;
            }
        });

        upload.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.uploadLesson));
                return true;
            }
        });

        create.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (selectedLecture) {
                    speak(getString(R.string.editLecture));
                } else {
                    speak(getString(R.string.createLecture));
                }
                return true;
            }
        });

        audioButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.recordWithMicrophone));
                return true;
            }
        });

        thumbnail.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.addPhotoThumbnail));
                return true;
            }
        });
    }


}