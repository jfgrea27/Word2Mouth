package com.imperial.word2mouth.create;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.imperial.word2mouth.common.tags.AppCodes;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.common.adapters.LectureItemAdapter;
import com.imperial.word2mouth.common.audio.AudioRecorder;
import com.imperial.word2mouth.common.dialog.ImageDialog;
import com.imperial.word2mouth.common.dialog.LectureNameDialog;
import com.imperial.word2mouth.common.tts.SpeakIcon;
import com.imperial.word2mouth.helpers.CourseLectureItemBuilder;
import com.imperial.word2mouth.helpers.FileSystemHelper;
import com.imperial.word2mouth.model.CourseItem;
import com.imperial.word2mouth.model.LectureItem;
import com.imperial.word2mouth.common.tags.IntentNames;
import com.imperial.word2mouth.model.SlideItem;

import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

public class CourseSummaryCreateActivity extends AppCompatActivity implements ImageDialog.OnInputListener {


    //////////////////////////////////////////////////////////////////////////////////////////////
    // Data Members
    ///////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////// Create Buttons //////////////////////
    private ImageButton createButton;

    /////////////////////// TTS //////////////////////
    private SpeakIcon textToSpeech;

    /////////////////////// RecycleView  //////////////////////
    private int selectedContent = -1;
    private RecyclerView lectureRecycleView;
    private LectureItemAdapter lectureItemAdapter;

    //////////////////Image Thumbnail////////////////////////////
    private ImageView photoThumbnail;

    //////////////////Sound Thumbnail////////////////////////////
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

    //////////////////Course Title////////////////////////////
    private TextView courseTitleView;

    //////////////////Model////////////////////////////
    // Extras
    private CourseItem courseItem;
    // Create Lecture
    private String lectureName;
    private LectureItem lectureItem;


    //////////////////////////////////////////////////////////////////////////////////////////////
    // Activity Lifecycle
    ///////////////////////////////////////////////////////////////////////////////////////////////
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_summary);


        // TODO save thumbnail image in a different thread and display once it has been saved.

        getExtras();

        configureCreateButton();
        configureLectureRecycleView();
        configureCourseName();
        configurePhotoThumbnail();
        configureAudio();
        configureTTS();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Get Extras
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void getExtras() {
        courseItem = (CourseItem) getIntent().getParcelableExtra(IntentNames.COURSE);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Create Button
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private void configureCreateButton() {
        createButton = findViewById(R.id.create_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogLectureName();
            }
        });
        // TTS
        createButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                textToSpeech.speak(getString(R.string.createLecture));
                return true;
            }
        });

    }
    private void dialogLectureName() {
        LectureNameDialog lectureNameDialog = new LectureNameDialog(CourseSummaryCreateActivity.this);
        lectureNameDialog.show(getSupportFragmentManager(), getString(R.string.lecture_name));
    }

    public void createLecture() {
        lectureItem = new LectureItem(courseItem, lectureName);
        lectureItem = FileSystemHelper.createLectureFileSystem(lectureItem, getApplicationContext());
        intentLectureSummaryCreateActivity();
    }

    public void enterLecture(LectureItem lectureItem) {
        this.lectureItem = lectureItem;
        intentLectureSummaryCreateActivity();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Intent to LectureSummaryCreateActivity
    ///////////////////////////////////////////////////////////////////////////////////////////////
    private void intentLectureSummaryCreateActivity() {
        Intent createEditIntent = new Intent(CourseSummaryCreateActivity.this, LectureSummaryCreateActivity.class);
        createEditIntent.putExtra(IntentNames.LECTURE, lectureItem);
        startActivity(createEditIntent);
    }

    public void setLectureName(String lectureName) {
        this.lectureName = lectureName;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // RecycleView
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void configureLectureRecycleView() {
        lectureRecycleView = findViewById(R.id.recycleCourseView);

        ArrayList<LectureItem> lectureItems = getFileLectureItems();
        lectureItemAdapter = new LectureItemAdapter(lectureItems, this);

        lectureRecycleView.setAdapter(lectureItemAdapter);
        lectureRecycleView.setLayoutManager(new LinearLayoutManager(CourseSummaryCreateActivity.this));
        lectureRecycleView.scrollToPosition(0);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ArrayList<LectureItem> getFileLectureItems() {
        ArrayList<LectureItem> lectureItems = new ArrayList<>();
        try {
            lectureItems = CourseLectureItemBuilder.getLectureItemsFromCourseDirectory(courseItem, getApplicationContext());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lectureItems;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Course Name
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void configureCourseName() {
        courseTitleView = findViewById(R.id.list_item_text);
        courseTitleView.setText(courseItem.getCourseName());
        courseTitleView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                textToSpeech.speak(courseTitleView.getText().toString());
                return true;
            }
        });
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Photo Thumbnail
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void configurePhotoThumbnail() {
        photoThumbnail = findViewById(R.id.list_item_thumbnail);

        photoThumbnail.setImageURI(Uri.fromFile(new File(courseItem.getCourseImageThumbnailPath())));
        photoThumbnail.setOnClickListener(v -> {
            ImageDialog imageDialog = new ImageDialog(ImageDialog.THUMBNAIL, CourseSummaryCreateActivity.this);
            imageDialog.show(getSupportFragmentManager(), getString(R.string.thumbnail_selection));
        });
        // TTS
        photoThumbnail.setOnLongClickListener(v -> {
            textToSpeech.speak(getString(R.string.addPhotoThumbnail));
            return true;
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri imageUri = null;
            switch (requestCode) {
                case AppCodes.GALLERY_SELECTION:
                    imageUri = data.getData();
                    FileSystemHelper.saveImageVideoFile(courseItem.getCourseImageThumbnailPath(), imageUri, getContentResolver());
                    photoThumbnail.setImageURI(imageUri);

                    break;

                case AppCodes.CAMERA_ROLL_SELECTION: {
                    Bitmap bitmapImage = (Bitmap) data.getExtras().get("data");
                    FileSystemHelper.saveImageVideoFile(courseItem.getCourseImageThumbnailPath(),bitmapImage);
                    photoThumbnail.setImageBitmap(bitmapImage);
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + requestCode);
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
    // Sound Thumbnail
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void configureAudio() {
        recorder = new AudioRecorder();
        // play button
        audioPreview = findViewById(R.id.course_audio_play);
        audioButton = findViewById(R.id.list_audio_button);

        audioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recording) {
                    audioButton.setColorFilter(Color.RED);
                    recorder.startRecording(audioFile.getPath());
                    recording = true;
                } else {
                    recorder.stopRecording();
                    audioButton.setColorFilter(Color.BLACK);
                    audioUri = Uri.fromFile(audioFile);
                    audioPreview.setVisibility(View.VISIBLE);
                    recording = false;
                    FileSystemHelper.saveAudioFile(courseItem.getCourseAudioThumbnailPath(), audioUri, getContentResolver());
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
        // TTS
        audioButton.setOnLongClickListener(v -> {
            textToSpeech.speak(getString(R.string.recordWithMicrophone));
            return true;
        });

        audioPreview.setOnLongClickListener(v -> {
            // TODO add some TTS
            return true;
        });

        audioFile = new File(courseItem.getCourseAudioThumbnailPath());
        if (audioFile.length() != 0) {
            audioUri = Uri.fromFile(audioFile);
            audioPreview.setVisibility(View.VISIBLE);
        } else {
            audioPreview.setVisibility(View.INVISIBLE);
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Text To Speech
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void configureTTS() {
        textToSpeech = new SpeakIcon(this);
    }

    public void enterSlide(SlideItem slideItem) {
        // TODO
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////

    // TODO move this somewhere else? Discuss with Jake



//    //////////////////Upload Button////////////////////////////
//    private ImageButton upload;
//    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//    // Firebase - // TODO complet this when you get to it - Could be in the Share part
//    private UploadProcedure uploadProcedure;
//    private String courseAuthorID;
//    private String courseLanguage;
//    private String courseCategory;
//    private PrevLectureItem prevLectureItem;
//    private String courseIdentification = "";
//    private boolean completedDatabase = false;
//    private boolean completedStorage = false;
//    private ProgressBar uploadProgress;
    // TODO Discuss with Jake where we could implement the upload? Otherwise need to go to the other side of the app...




    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Upload Button

//    private void configureUploadButton() {
//        upload = findViewById(R.id.upload_button);
//
//        uploadProgress = findViewById(R.id.progress_upload);
//        uploadProgress.bringToFront();
//
//        upload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (selectedLecture) {
//                    if (user != null) {
//
//                        String authorID = FileReaderHelper.readTextFromFile(courseItem.getCoursePath() + FileSystemConstants.meta + FileSystemConstants.author);
//
//                        if (user.getUid().equals(authorID) || authorID == "") {
//                            uploadCourse();
//                        } else {
//                            // TODO Think of a better way than Toast?
//                            Toast.makeText(CourseSummaryCreateActivity.this, R.string.creatingTeacherOnly, Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Toast.makeText(CourseSummaryCreateActivity.this, R.string.mustCreateAccount, Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        });
//
//        upload.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN);
//    }

    //TODO Complete this method for Firebase (Methdos are comented below)
//
//    private void uploadCourse() {
////        uploadProgress.setVisibility(View.VISIBLE);
////        uploadProgress.bringToFront();
////
////        setCourseAuthorIdentification();
////        localLectures.get(lectureNumber).setLanguage(courseItem.getCourseLanguage());
////        localLectures.get(lectureNumber).setCategory(courseItem.getCourseTopic());
////        localLectures.get(lectureNumber).setLectureIdentification(getLectureIdentification());
////        localLectures.get(lectureNumber).setBluetoothCourse(FileReaderHelper.readTextFromFile(courseItem.getCoursePath() + DirectoryConstants.meta + DirectoryConstants.courseBluetooth));
////        localLectures.get(lectureNumber).setBluetoothLecture(FileReaderHelper.readTextFromFile(lectureItem.getLecturePath() + DirectoryConstants.meta + DirectoryConstants.lectureBluetooth));
////
////        uploadProcedure = new UploadProcedure(courseItem, localLectures.get(lectureNumber), CourseSummaryCreateActivity.this);
////
////        uploadProcedure.setListener(new UploadProcedure.UploadListener() {
////            @Override
////            public void onDataLoadedInDatabase() {
////                uploadDataBaseSuccessful();
////            }
////
////            @Override
////            public void onDataLoadedInStorage(String courseIdentification, String lectureIdentification) {
////                setCourseIdentification(courseIdentification);
////                setLectureIdentification(lectureIdentification);
////                uploadStorageSuccessful();
////            }
////
////            @Override
////            public void onDataLoadedInStorageEntireCourse(String courseIdentification, String lectureIdentification, String lecturePath) {
////
////            }
////        });
////
////        uploadProcedure.uploadCourse();
//    }
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

//    private boolean checkIsAuthorOfCourse() {
////        File f = new File(coursePath + DirectoryConstants.meta + DirectoryConstants.author);
////
////        if (f.length() == 0) {
////            return true;
////        }
////
////        Scanner fileReader = null;
////        try {
////            fileReader = new Scanner(f);
////        } catch (FileNotFoundException e) {
////            e.printStackTrace();
////        }
////        String s = fileReader.nextLine();
////
////        if (user.getUid().equals(s)) {
////            return true;
////        }
////
////        return false;
//        return false;
//    }

//    private void uploadDataBaseSuccessful() {
//        completedDatabase = true;
//        uploadSuccessful();
//    }
//
//
//    private void uploadStorageSuccessful() {
//        completedStorage = true;
//        uploadSuccessful();
//
//    }
//
//    private void uploadSuccessful() {
//        if (completedDatabase && completedStorage) {
//            uploadProgress.setVisibility(View.GONE);
//            Toast.makeText(CourseSummaryCreateActivity.this, R.string.uploadSuccessful, Toast.LENGTH_SHORT).show();
//            upload.setEnabled(true);
//
//        }
//    }
//

}