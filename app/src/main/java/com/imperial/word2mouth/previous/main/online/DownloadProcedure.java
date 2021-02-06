package com.imperial.word2mouth.previous.main.online;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.previous.background.LearnOnlineNewCourseSummary;
import com.imperial.word2mouth.previous.shared.CourseItem;
import com.imperial.word2mouth.previous.shared.DirectoryConstants;
import com.imperial.word2mouth.previous.shared.FileHandler;
import com.imperial.word2mouth.previous.shared.FileReaderHelper;
import com.imperial.word2mouth.previous.shared.LectureItem;
import com.imperial.word2mouth.previous.shared.UnzipFile;

import java.io.File;
import java.io.IOException;

public class DownloadProcedure {
    public static final int NEW = 0;
    public static final int ELSE = 1;

    private final int type;

    private LectureItem lectureItem;
    private final Context context;
    private final Activity activity;
    private CourseItem courseItem;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Uri courseImageUri;
    private Uri courseSoundUri;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public DownloadProcedure(CourseItem courseItem, LectureItem lectureItem, Context context, Activity activity, int type) {
        this.courseItem = courseItem;
        this.lectureItem = lectureItem;
        this.context = context;
        this.activity = activity;
        this.type = type;
    }

    public void download() {

        String localCoursePath =  checkCourseExistOnDevice();

        if (localCoursePath != null) {

            downloadLecture(localCoursePath);

        } else {
            downloadCourse();

        }
    }

    private void downloadCourse() {
        File courseFile = FileHandler.createDirectoryForCourseAndReturnIt(courseItem.getCourseName(), context);

        // Retrieve Course Name
        db.collection("content").document(courseItem.getCourseOnlineIdentification()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                retrieveCourseCredentials(documentSnapshot);
                insertCourseMetaFiles(courseFile.getPath());
                fetchThumbnailCourse(courseFile.getPath());
                downloadLecture(courseFile.getPath());

            }
        });
    }


    private void fetchThumbnailCourse(String path) {

        StorageReference soundRef = storage.getReference().child("content").child(courseItem.getCourseOnlineIdentification()).child("Sound Thumbnail");

        File sound = new File(context.getExternalFilesDir(null) + DirectoryConstants.zip + "sound.3gp");

        soundRef.getFile(sound).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                FileHandler.saveUriFileToDestination(Uri.fromFile(sound),path + DirectoryConstants.meta + DirectoryConstants.soundThumbnail );

            }
        });


        StorageReference imageRef = storage.getReference().child("/content/").child(courseItem.getCourseOnlineIdentification()).child("Photo Thumbnail");

        File thumbnail = new File(context.getExternalFilesDir(null) + DirectoryConstants.zip + "thumbnail.jpg");
        imageRef.getFile(thumbnail).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                FileHandler.saveUriFileToDestination(Uri.fromFile(thumbnail),path + DirectoryConstants.meta + DirectoryConstants.photoThumbnail );

            }
        });

    }

    private void insertCourseMetaFiles(String path) {
        // Identification
        FileHandler.createDirectoryAndReturnIt(path, FileHandler.META);
        FileHandler.createFileForSlideContentAndReturnIt(path + DirectoryConstants.meta, null, context.getContentResolver(), courseItem.getCourseName(), FileHandler.TITLE);
        FileHandler.createFileForSlideContentAndReturnIt(path + DirectoryConstants.meta, null, context.getContentResolver(), courseItem.getCourseOnlineIdentification(), FileHandler.ONLINE_COURSE_IDENTIFICATION);
        FileHandler.createFileForSlideContentAndReturnIt( path + DirectoryConstants.meta, null, context.getContentResolver(), courseItem.getLanguage(), FileHandler.LANGUAGE_SELECTION);
        FileHandler.createFileForSlideContentAndReturnIt(path + DirectoryConstants.meta, null, context.getContentResolver(), courseItem.getCategory(), FileHandler.CATEGORY_SELECTION);
        FileHandler.createFileForSlideContentAndReturnIt(path + DirectoryConstants.meta, null, context.getContentResolver(), courseItem.getAuthorID(), FileHandler.AUTHOR);
        FileHandler.createFileForSlideContentAndReturnIt(path + DirectoryConstants.meta, null, context.getContentResolver(), courseItem.getBluetoothCourse(), FileHandler.BLUETOOTH_UUID_COURSE);


        FileHandler.createDirectoryAndReturnIt(path, FileHandler.LECTURES_DIRECTORY);

    }

    private void retrieveCourseCredentials(DocumentSnapshot documentSnapshot) {


       FirebaseStorage.getInstance().getReference().child("content").child(courseItem.getCourseName() + courseItem.getCourseOnlineIdentification())
                .child("Photo Thumbnail").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                courseImageUri = uri;
            }
        });

        FirebaseStorage.getInstance().getReference().child("content").child(courseItem.getCourseName() + courseItem.getCourseOnlineIdentification())
                .child("Sound Thumbnail").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                courseSoundUri = uri;
            }
        });


    }


    private void downloadLecture(String localCoursePath) {

        // Retrieve Previous Version And Delete From the  cache Folder
        String version = FileReaderHelper.readTextFromFile(localCoursePath + DirectoryConstants.meta + DirectoryConstants.versionLecture);
        File f = new File(activity.getExternalFilesDir(null) + DirectoryConstants.cache + version + ".txt");
        if (f.exists()) {
            f.delete();
        }

        File lectureFile = new File(context.getExternalFilesDir(null).getPath() + DirectoryConstants.zip + lectureItem.getLectureName() + ".zip");
        if (!lectureFile.exists()) {
            try {
                lectureFile.createNewFile();

            } catch (IOException e) {
                e.printStackTrace();
            }

            StorageReference lectureRef = FirebaseStorage.getInstance().getReference().child("content").child(lectureItem.getLectureIdentification()).child("Lecture.zip");

            lectureRef.getFile(lectureFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(context, R.string.downloadingLecture, Toast.LENGTH_SHORT).show();
                    moveZipCourse(lectureFile.getPath(), localCoursePath + DirectoryConstants.lectures);

                    createACacheForNewVersion(localCoursePath + DirectoryConstants.lectures + "/" + lectureItem.getLectureName());
                    tidyZipFolder();

                    switch (type) {
                        case NEW:
                            LearnOnlineNewCourseSummary temp = (LearnOnlineNewCourseSummary) activity;
                            temp.signalCompleteDownload(lectureItem.getLectureIdentification());
                            break;
                        case ELSE:
                            LearnOnlineCourseSummary temp2 = (LearnOnlineCourseSummary) activity;
                            temp2.signalCompleteDownload(lectureItem.getLectureIdentification());

                            break;
                    }
                    // Update Downloads Counter of the lecture
                    db.collection("content").document(lectureItem.getLectureIdentification()).update("downloadCounter", FieldValue.increment(1));
                }

                private void createACacheForNewVersion(String lecturePath) {
                    String version = FileReaderHelper.readTextFromFile(lecturePath + DirectoryConstants.meta + DirectoryConstants.versionLecture);
                    File f = new File(localCoursePath + DirectoryConstants.lectures + lectureItem.getLectureName() + DirectoryConstants.slides);
                    int numberSlides = f.listFiles().length;
                    FileHandler.createFileForLectureTracking(version, numberSlides, activity);
                }
            });
        }
    }

    private void tidyZipFolder() {
        File f = new File(        context.getExternalFilesDir(null) + DirectoryConstants.zip);

        File[] files = f.listFiles();

        for (File file : files) {
            file.delete();
        }
    }

    private void moveZipCourse(String sourcePath, String destPath) {
        UnzipFile.unzipFile(sourcePath, destPath);
    }

    private String checkCourseExistOnDevice() {
        File f = new File(context.getExternalFilesDir(null) + DirectoryConstants.offline);

        File[] courses = f.listFiles();

        for (File course : courses) {

            String identification = FileReaderHelper.readTextFromFile(course.getPath() + DirectoryConstants.meta + DirectoryConstants.identification);

            if (identification.equals(courseItem.getCourseOnlineIdentification())) {
                return course.getPath();
            }
        }
        return null;
    }


}
