package com.imperial.word2mouth.teach.offline;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.shared.CourseItem;
import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.shared.FileHandler;
import com.imperial.word2mouth.shared.FileReaderHelper;
import com.imperial.word2mouth.shared.IntentNames;
import com.imperial.word2mouth.shared.LectureItem;
import com.imperial.word2mouth.shared.adapters.ArrayAdapterLectureOffline;
import com.imperial.word2mouth.teach.TeachActivityMain;
import com.imperial.word2mouth.teach.offline.create.TeachLectureCreationSummaryActivity;
import com.imperial.word2mouth.teach.offline.create.audio.AudioRecorder;
import com.imperial.word2mouth.teach.offline.create.video.ImageDialog;
import com.imperial.word2mouth.teach.offline.upload.UploadProcedure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;

public class    TeachOfflineCourseSummary extends AppCompatActivity implements ImageDialog.OnInputListener {
    //////////////////////////////////////////////////////////////////////////////////////////////
    // Permissions
    private final int CAMERA_PERMISSION = 1;
    private final int AUDIO_RECORDING_PERMISSION = 2;
    private final int READ_WRITE_PERMISSION = 3;

    private boolean hasReadWriteStorageAccess = false;
    private boolean hasAudioRecordingPermission = false;
    private boolean hasCameraPermission = false;

    //////////////////////////////////////////////////////////////////////////////////////////////

    private File metaDirectory = null;
    private File lecturesDirectory = null;
    private final String courseUID = UUID.randomUUID().toString();

    //////////////////////////////////////////////////////////////////////////////////////////////

    // Intents
    private String courseName;
    private String coursePath;

    //////////////////////////////////////////////////////////////////////////////////////////////

    /////// Thumbnail
    // View
    private ImageView thumbnail;
    // Model
    // Controller
    private Uri imageUri = null;

    // Camera Choice
    public final int CAMERA_ROLL_SELECTION = 0;
    public final int GALLERY_SELECTION = 1;

    //////// Audio Thumbnail
    // View
    private ImageButton audioPreview;
    private ImageButton audioButton;
    // Model
    private File audioFile = null;
    // Controller
    private Uri audioUri = null;
    private AudioRecorder recorder;
    private MediaPlayer player;


    ////// Title Course
    // View
    private TextView courseNameView;

    ////// List View of Lectures
    // View
    private ListView lecturesView;
    // Model
    private ArrayList<LectureItem> localLectures;
    private int lectureNumber = -1;

    // Controller
    private ArrayAdapterLectureOffline adapter;
    private boolean selectedLecture = false;


    /////// Delete Button
    // View
    private ImageButton delete;
    // Model
    private int numberLectures = 0;
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

    private UploadProcedure uploadProcedure;
    private String courseAuthorID;
    private CourseItem courseItem;
    private String courseLanguage;
    private String courseCategory;
    private LectureItem lectureItem;
    private String courseIdentification = "";
    private boolean completedDatabase = false;
    private boolean completedStorage = false;
    private ProgressBar uploadProgress;
    private TextToSpeech textToSpeech;
    private boolean recording = false;
    // Controller

    //////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_offline_course_summary);

        getExtras();

        getPermissions();

        if (hasReadWriteStorageAccess) {
            fetchAndCreateInformationAboutCourse();

            // Create a Type File and UUID
            configureFirstCreationCourse();

            // List of Lectures
            configureListViewLectures();
            // Create Button
            configureCreateButton();
            // Delete Button
            configureDeleteButton();
            // Name
            configureCourseName();
            // Thumbnail
            configureCourseThumbnail();
            // Audio
            configureAudio();

            configureUploadButton();


            configureTextToSpeech();
            configureLongClicks();

        } else {
            finish();
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////

    // Upload Button

    private void configureUploadButton() {
        upload = findViewById(R.id.upload_button);

        uploadProgress = findViewById(R.id.progress_upload);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedLecture) {
                    if (user != null) {

                        String authorID = FileReaderHelper.readTextFromFile(courseItem.getCoursePath() + DirectoryConstants.meta + DirectoryConstants.author);

                        if (user.getUid().equals(authorID) || authorID == "") {
                            uploadCourse();
                        } else {
                                Toast.makeText(TeachOfflineCourseSummary.this, "Creating teacher must login to upload content", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(TeachOfflineCourseSummary.this, "Must Login", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        upload.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);

    }

    private void uploadCourse() {
        uploadProgress.setVisibility(View.VISIBLE);
        setCourseAuthorIdentification();
        localLectures.get(lectureNumber).setLanguage(courseItem.getLanguage());
        localLectures.get(lectureNumber).setCategory(courseItem.getCategory());
        localLectures.get(lectureNumber).setLectureIdentification(getLectureIdentification());
        courseItem.setCourseBluetooth(FileReaderHelper.readTextFromFile(courseItem.getCoursePath() + DirectoryConstants.meta + DirectoryConstants.courseBluetooth));
        localLectures.get(lectureNumber).setBluetoothCourse(FileReaderHelper.readTextFromFile(courseItem.getCoursePath() + DirectoryConstants.meta + DirectoryConstants.courseBluetooth));
        localLectures.get(lectureNumber).setBluetoothLecture(FileReaderHelper.readTextFromFile(lectureItem.getLecturePath() + DirectoryConstants.meta + DirectoryConstants.lectureBluetooth));

        uploadProcedure = new UploadProcedure(courseItem, localLectures.get(lectureNumber), TeachOfflineCourseSummary.this);

        uploadProcedure.setListener(new UploadProcedure.UploadListener() {
            @Override
            public void onDataLoadedInDatabase() {
                uploadDataBaseSuccessful();
            }

            @Override
            public void onDataLoadedInStorage(String courseIdentification, String lectureIdentification) {
                setCourseIdentification(courseIdentification);
                setLectureIdentification(lectureIdentification);
                uploadStorageSuccessful();
            }

            @Override
            public void onDataLoadedInStorageEntireCourse(String courseIdentification, String lectureIdentification, String lecturePath) {

            }
        });

        uploadProcedure.uploadCourse();
    }

    private String getLectureIdentification() {
        return FileReaderHelper.readTextFromFile(localLectures.get(lectureNumber).getLecturePath() + DirectoryConstants.meta + DirectoryConstants.lectureIdentifcation);
    }


    private void setCourseIdentification(String identification) {
        if (courseIdentification == "") {
            courseIdentification = identification;
            courseItem.setCourseOnlineIdentification(identification);
            lectureItem.setCourseIdentification(identification);
            updateCourseIdentificationFile();
        }
    }

    private void updateCourseIdentificationFile() {
        FileHandler.createFileForSlideContentAndReturnIt(coursePath + DirectoryConstants.meta , null, null, courseIdentification, FileHandler.ONLINE_COURSE_IDENTIFICATION);
        FileHandler.createFileForSlideContentAndReturnIt(lectureItem.getLecturePath() + DirectoryConstants.meta , null, null, courseIdentification, FileHandler.ONLINE_COURSE_IDENTIFICATION);

    }


    private void setCourseAuthorIdentification() {
        if (courseItem.getAuthorID().isEmpty()) {
            courseAuthorID = user.getUid();
            courseItem.setAuthorID(user.getUid());
            updateCourseAuthorFile(user.getUid());
        }
    }

    private void updateCourseAuthorFile(String uid) {
        FileHandler.createFileForSlideContentAndReturnIt(coursePath + DirectoryConstants.meta , null, null, uid, FileHandler.AUTHOR);
        FileHandler.createFileForSlideContentAndReturnIt(lectureItem.getLecturePath() + DirectoryConstants.meta , null, null, uid, FileHandler.AUTHOR);

    }


    private void setLectureIdentification(String identification) {
        if (lectureItem.getLectureIdentification().isEmpty()) {
            lectureItem.setLectureIdentification(identification);
            updateLectureIdentification();

        }
    }

    private void updateLectureIdentification() {
        FileHandler.createFileForSlideContentAndReturnIt(lectureItem.getLecturePath() + DirectoryConstants.meta , null, null, lectureItem.getLectureIdentification(), FileHandler.ONLINE_LECTURE_IDENTIFICATION);

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void fetchAndCreateInformationAboutCourse() {
        // File
        metaDirectory = FileHandler.createDirectoryAndReturnIt(coursePath, FileHandler.META);
        lecturesDirectory = FileHandler.createDirectoryAndReturnIt(coursePath, FileHandler.LECTURES_DIRECTORY);

        // Creating audioFile
        audioFile = FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), null, getContentResolver(), null, FileHandler.AUDIO );
        FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), null, getContentResolver(), courseName, FileHandler.TITLE);

        courseLanguage = FileReaderHelper.readTextFromFile(metaDirectory.getAbsolutePath() + DirectoryConstants.language);
        courseCategory = FileReaderHelper.readTextFromFile(metaDirectory.getAbsolutePath() + DirectoryConstants.category);

    }




    private boolean checkIsAuthorOfCourse() {
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

    private void configureFirstCreationCourse() {
        FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), null, getContentResolver(), DirectoryConstants.COURSE, FileHandler.COURSE_LECTURE_DISTINGUISHING);

        String courseBluetooth = FileReaderHelper.readTextFromFile(metaDirectory.getAbsolutePath() + DirectoryConstants.courseBluetooth);

        if (courseBluetooth.isEmpty()) {
            FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), null, getContentResolver(), courseUID.toString(), FileHandler.BLUETOOTH_UUID_COURSE);
            courseBluetooth = FileReaderHelper.readTextFromFile(metaDirectory.getAbsolutePath() + DirectoryConstants.courseBluetooth);
            courseItem.setCourseBluetooth(courseBluetooth);
        } else {
            courseItem.setCourseBluetooth(courseBluetooth);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    // Create Button
    private void configureCreateButton() {
        create = findViewById(R.id.edit_button);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMetaData();

                if (lectureNumber < 0) {
                    dialogLectureName();
                } else {
                    intentEditLecture(localLectures.get(lectureNumber));
                }

            }
        });

    }

    private void intentEditLecture(LectureItem item) {
        Intent createEditIntent = new Intent(TeachOfflineCourseSummary.this, TeachLectureCreationSummaryActivity.class);

        createEditIntent.putExtra(IntentNames.LECTURE_NAME, item.getLectureName());
        createEditIntent.putExtra(IntentNames.LECTURE_PATH, item.getLecturePath());
        startActivity(createEditIntent);
    }

    private void intentCreateLecture() {
        Intent createEditIntent = new Intent(TeachOfflineCourseSummary.this, TeachLectureCreationSummaryActivity.class);

        createEditIntent.putExtra(IntentNames.LECTURE_NAME, lectureName);
        createEditIntent.putExtra(IntentNames.LECTURE_PATH, coursePath + DirectoryConstants.lectures + lectureName);
        createEditIntent.putExtra(IntentNames.COURSE, courseItem);
        startActivity(createEditIntent);
    }


    private void dialogLectureName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TeachOfflineCourseSummary.this);
        builder.setTitle("Lecture Name");
        final EditText input = new EditText(TeachOfflineCourseSummary.this);
        input.setHint("Type Lecture Name");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                lectureName = input.getText().toString();
                if (lectureName.isEmpty()) {
                    lectureName = "Untitled Lecture";
                }
                intentCreateLecture();
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

    private void getExtras() {
        courseItem = (CourseItem) getIntent().getExtras().get(IntentNames.COURSE);

        coursePath = courseItem.getCoursePath();
        courseName = courseItem.getCourseName();


        lectureItem = new LectureItem(courseName, lectureName);

    }

    // Delete Button
    private void configureDeleteButton() {
        delete = findViewById(R.id.delete_button);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectedLecture) {
                    FileHandler.deleteRecursive(new File (lecturesDirectory.getPath() + "/" + localLectures.get(lectureNumber).getLectureName()));
                    selectedLecture = false;

                    delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    adapter.remove(localLectures.get(lectureNumber));
                    adapter.notifyDataSetInvalidated();
                    adapter.notifyDataSetChanged();
                    lectureNumber = -1;
                    numberLectures--;
                }
            }
        });


        if (delete != null) {
            delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

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

    // Saving Data for Course Selection
    private void saveMetaData() {
        if (imageUri != null) {
            FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), imageUri, getContentResolver(), null, FileHandler.IMAGE);
        }

        if (audioUri != null) {
            FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), audioUri, getContentResolver(), null, FileHandler.AUDIO);
        }

        FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), null, null, courseName, FileHandler.TITLE);

    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Permissions

    private void getPermissions() {
        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED )) {
            Toast.makeText(this, "Please allow access to Storage", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, READ_WRITE_PERMISSION);
        } else{
            hasReadWriteStorageAccess = true;
        }

        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)){
            Toast.makeText(this, "Please allow access to Audio", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_RECORDING_PERMISSION);
        } else{
            hasAudioRecordingPermission = true;
        }

        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "Please allow access to Camera", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        } else{
            hasCameraPermission = true;
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

        if (requestCode == AUDIO_RECORDING_PERMISSION) {
            if(permissions[0].equals(Manifest.permission.RECORD_AUDIO) && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                hasAudioRecordingPermission = true;
            }
        }

        if (requestCode == CAMERA_PERMISSION) {
            if(permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasCameraPermission = true;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    // List of Lectures

    private void configureListViewLectures() {
        lecturesView = findViewById(R.id.lecture_list_view);

        localLectures = retrieveLocalLectures();

        if (localLectures.size() > 0) {
            adapter = new ArrayAdapterLectureOffline(TeachOfflineCourseSummary.this, R.layout.list_lectures, localLectures);
            lecturesView.setAdapter(adapter);
        }

        lecturesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                for (int i = 0; i < adapter.getCount(); i++) {
                    View item = lecturesView.getChildAt(i);
                    if (item != null) {
                        item.setBackgroundColor(Color.WHITE);
                    }
                }


                if (selectedLecture) {
                    view.setBackgroundColor(Color.WHITE);
                    selectedLecture = false;
                    if (delete != null) {
                        delete.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
                    }
                    if (upload != null) {
                        upload.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);

                    }
                    lectureNumber = -1;


                } else {
                    selectedLecture = true;
                    lectureNumber = position;
                    lectureItem.setPath(localLectures.get(position).getLecturePath());
                    view.setBackgroundColor(Color.LTGRAY);
                    if (delete != null) {
                        delete.setColorFilter(null);
                    }
                    if (upload != null) {
                        upload.setColorFilter(null);
                    }
                }
            }
        });

    }

    private ArrayList<LectureItem> retrieveLocalLectures() {

        ArrayList<LectureItem> lectureItems = new ArrayList<>();

        File[] lectureItemsFiles = lecturesDirectory.listFiles();
        if (lectureItemsFiles != null) {
            numberLectures = lectureItemsFiles.length;

            for (File f : lectureItemsFiles) {

                String lectureName;


                // Title
                lectureName = FileReaderHelper.readTextFromFile(f.getPath()+ DirectoryConstants.meta + DirectoryConstants.title);
                LectureItem item = new LectureItem(courseName, lectureName);
                item.setPath(f.getPath());

                lectureItems.add(item);
            }
        }

        return lectureItems;

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


    // Thumbnail
    private void configureCourseThumbnail() {
        thumbnail = findViewById(R.id.list_item_thumbnail);

        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasCameraPermission) {
                    ImageDialog imageDialog = new ImageDialog(ImageDialog.THUMBNAIL);
                    imageDialog.show(getSupportFragmentManager(), "Video Dialog");
                } else {
                    Toast.makeText(TeachOfflineCourseSummary.this, "Need the Camera Permission", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case GALLERY_SELECTION:
                    imageUri= data.getData();
                    if (imageUri != null) {
                        FileHandler.createFileForSlideContentAndReturnIt(metaDirectory.getAbsolutePath(), imageUri, getContentResolver(), null, FileHandler.IMAGE);
                        thumbnail.setImageURI(imageUri);
                    }
                    break;

                case CAMERA_ROLL_SELECTION: {
                    Bitmap bitMapImage = null;
                    bitMapImage = (Bitmap) data.getExtras().get("data");

                    imageUri = Uri.fromFile(new File(metaDirectory + "/thumbnail.jpg"));

                    if (imageUri != null) {
                        try {
                            FileOutputStream out = new FileOutputStream(metaDirectory + "/thumbnail.jpg");
                            bitMapImage.compress(Bitmap.CompressFormat.JPEG, 100, out);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        if (bitMapImage != null) {
                            thumbnail.setImageBitmap(bitMapImage);
                        }
                        break;
                    }
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + requestCode);
            }
        }
    }

    @Override
    public void sendInput(int choice) {
        switch (choice) {
            case GALLERY_SELECTION: {
                Toast.makeText(this, "Opening Galleries", Toast.LENGTH_SHORT).show();
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"), GALLERY_SELECTION);
                break;
            }
            case CAMERA_ROLL_SELECTION: {
                Toast.makeText(this, "Opening Camera Roll", Toast.LENGTH_SHORT).show();
                Intent rollIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(rollIntent, CAMERA_ROLL_SELECTION);
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
                if (hasAudioRecordingPermission) {
                    if (!recording) {
                        Toast.makeText(TeachOfflineCourseSummary.this, "Start Recording", Toast.LENGTH_SHORT).show();
                        audioButton.setColorFilter(Color.RED);
                        recorder.startRecording(audioFile.getPath());
                        recording = true;
                    } else {
                        Toast.makeText(TeachOfflineCourseSummary.this, "Stop Recording", Toast.LENGTH_SHORT).show();
                        recorder.stopRecording();
                        audioButton.setColorFilter(Color.BLACK);
                        audioUri = Uri.fromFile(audioFile);
                        audioPreview.setVisibility(View.VISIBLE);
                        recording = false;
                    }
                } else {
                    Toast.makeText(TeachOfflineCourseSummary.this, "Need the Microphone Permission", Toast.LENGTH_SHORT).show();
                }

            }
        });

        audioPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audioUri != null) {
                    player = MediaPlayer.create(TeachOfflineCourseSummary.this, audioUri);
                    player.start();
                }
            }
        });

        if (audioFile.length() != 0) {
            audioUri = Uri.fromFile(audioFile);
            audioPreview.setVisibility(View.VISIBLE);
        } else {
            audioPreview.setVisibility(View.INVISIBLE);
        }


    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Title


    private void configureCourseName() {
        courseNameView = findViewById(R.id.list_item_text);
        courseNameView.setText(courseItem.getCourseName());
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
            Toast.makeText(TeachOfflineCourseSummary.this, "Upload Successful", Toast.LENGTH_SHORT).show();
            upload.setEnabled(true);

        }
    }


    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }


/////////////////////////////////////////////////////////////////////////////////////////////


    public void speak(String string) {
        textToSpeech.speak(string, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void configureTextToSpeech() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(TeachOfflineCourseSummary.this, "Language not supported", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TeachOfflineCourseSummary.this, "Initialization failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////


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