package com.imperial.word2mouth.learn.main.online;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.imperial.word2mouth.shared.CourseItem;
import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.shared.FileHandler;
import com.imperial.word2mouth.shared.FileReaderHelper;
import com.imperial.word2mouth.shared.LectureItem;
import com.imperial.word2mouth.shared.UnzipFile;

import java.io.File;
import java.io.IOException;

public class DownloadProcedure {
    private LectureItem lectureItem;
    private final Context context;
    private final LearnOnlineCourseSummary activity;
    private CourseItem courseItem;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private Uri courseImageUri;
    private Uri courseSoundUri;
    private FirebaseStorage storage = FirebaseStorage.getInstance();

    public DownloadProcedure(CourseItem courseItem, LectureItem lectureItem, Context context, LearnOnlineCourseSummary activity) {
        this.courseItem = courseItem;
        this.lectureItem = lectureItem;
        this.context = context;
        this.activity = activity;
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
        // Retrieve Course ID

        // Retrieve Course Languae
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
        File lectureFile = new File(context.getExternalFilesDir(null).getPath() + DirectoryConstants.zip + lectureItem.getLectureName() + ".zip");
        if (!lectureFile.exists()) {
            try {
                lectureFile.createNewFile();

            } catch (IOException e) {
                e.printStackTrace();
            }

            StorageReference lectureRef = FirebaseStorage.getInstance().getReference().child("content").child(lectureItem.getLectureIdentification()).child("Lecture.zip");

            lectureRef.getFile(lectureFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(context, "Downloading the Lecture", Toast.LENGTH_SHORT).show();
                    moveZipCourse(lectureFile.getPath(), localCoursePath + DirectoryConstants.lectures);

                    tidyZipFolder();
                    activity.signalCompleteDownload();
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
