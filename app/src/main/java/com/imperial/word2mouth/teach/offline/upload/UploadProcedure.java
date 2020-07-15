package com.imperial.word2mouth.teach.offline.upload;

import android.app.Activity;
import android.net.Uri;
import android.util.Pair;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.imperial.word2mouth.shared.CourseItem;
import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.shared.FileHandler;
import com.imperial.word2mouth.shared.FileReaderHelper;
import com.imperial.word2mouth.shared.LectureItem;
import com.imperial.word2mouth.teach.offline.upload.database.CourseTransferObject;
import com.imperial.word2mouth.teach.offline.upload.database.LectureTransferObject;
import com.imperial.word2mouth.teach.offline.upload.storage.StorageUploadPreparation;

import java.io.File;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UploadProcedure {

    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // FirebaseFireStore
    private CourseTransferObject dto;
    private LectureTransferObject lto;
    private DatabaseReference contentRef;

    private Activity activity;


    // Intents
    private LectureItem lectureItem;
    private CourseItem courseItem;

    private UploadListener listener;
    private boolean uploadEntireCourse = false;
    private HashMap<LectureTransferObject, LectureItem> mapLTOItem = new HashMap<>();



    ////////////////////////////////////////////////////////////////////////////////////////////////

    public UploadProcedure(CourseItem courseItem, LectureItem lectureItem, Activity activity) {
        this.courseItem = courseItem;
        this.lectureItem = lectureItem;
        this.activity = activity;
    }


    // Upload only a Lecture (may need to upload the course if it does not exist)
    public void uploadCourse() {
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();


        if (user != null) {
            courseItem.setAuthorID(user.getUid());
            lectureItem.setAuthorID(user.getUid());

            addAllNecessaryFilesToCourse();
            addAllNecessaryFilesToLecture();
            uploadCourseToDataBase();


        }
    }

    private void addAllNecessaryFilesToLecture() {
        FileHandler.createFileForSlideContentAndReturnIt( lectureItem.getLecturePath() + DirectoryConstants.meta, null, activity.getContentResolver(), lectureItem.getLanguage(), FileHandler.LANGUAGE_SELECTION);
        FileHandler.createFileForSlideContentAndReturnIt(lectureItem.getLecturePath() + DirectoryConstants.meta, null, activity.getContentResolver(), lectureItem.getCategory(), FileHandler.CATEGORY_SELECTION);
        FileHandler.createFileForSlideContentAndReturnIt(lectureItem.getLecturePath() + DirectoryConstants.meta, null, activity.getContentResolver(), lectureItem.getAuthorUID(), FileHandler.AUTHOR);

    }

    private void addAllNecessaryFilesToCourse() {
        FileHandler.createFileForSlideContentAndReturnIt( courseItem.getCoursePath() + DirectoryConstants.meta, null, activity.getContentResolver(), courseItem.getLanguage(), FileHandler.LANGUAGE_SELECTION);
        FileHandler.createFileForSlideContentAndReturnIt(courseItem.getCoursePath() + DirectoryConstants.meta, null, activity.getContentResolver(), courseItem.getCategory(), FileHandler.CATEGORY_SELECTION);
        FileHandler.createFileForSlideContentAndReturnIt(courseItem.getCoursePath() + DirectoryConstants.meta, null, activity.getContentResolver(), courseItem.getAuthorID(), FileHandler.AUTHOR);
    }


    // Step 1 Upload Course to Firebase
    private void uploadCourseToDataBase() {
        if (user != null) {
            dto = new CourseTransferObject(courseItem);
            contentRef = database.getReference("/content/");

            // Allow Speak Search //TODO change this using Algolia
            dto.setLowerCapital();

            if (courseItem.getCourseOnlineIdentification().isEmpty()) {

                db.collection("content").add(dto).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        dto.setCourseUID(documentReference.getId());
                        if (!uploadEntireCourse) {
                            lectureItem.setCourseIdentification(dto.getCourseUID());
                            FileHandler.createFileForSlideContentAndReturnIt(lectureItem.getLecturePath() + DirectoryConstants.meta, null, activity.getContentResolver(), lectureItem.getCourseIdentification(), FileHandler.ONLINE_COURSE_IDENTIFICATION);

                        }
                        courseItem.setCourseOnlineIdentification(dto.getCourseUID());

                        // Update
                        FileHandler.createFileForSlideContentAndReturnIt(courseItem.getCoursePath() + DirectoryConstants.meta, null, activity.getContentResolver(), courseItem.getCourseOnlineIdentification(), FileHandler.ONLINE_COURSE_IDENTIFICATION);


                        db.collection("content").document(dto.getCourseUID()).update("courseUID", dto.getCourseUID()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                listener.onDataLoadedInDatabase();
                                uploadThumbnailCourseStorage();
                            }
                        });

                    }
                });
            } else {

                // retrieve key
                dto.setCourseUID(courseItem.getCourseOnlineIdentification());
                if (!uploadEntireCourse) {
                    lectureItem.setCourseIdentification(courseItem.getCourseOnlineIdentification());
                    FileHandler.createFileForSlideContentAndReturnIt(lectureItem.getLecturePath() + DirectoryConstants.meta, null, activity.getContentResolver(), lectureItem.getCourseIdentification(), FileHandler.ONLINE_COURSE_IDENTIFICATION);

                }

                FileHandler.createFileForSlideContentAndReturnIt(courseItem.getCoursePath() + DirectoryConstants.meta, null, activity.getContentResolver(), courseItem.getCourseOnlineIdentification(), FileHandler.ONLINE_COURSE_IDENTIFICATION);

                db.collection("content").document(courseItem.getCourseOnlineIdentification()).set(dto).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onDataLoadedInDatabase();

                        uploadThumbnailCourseStorage();
                    }
                });
            }

        }
    }

    // Step 2 Upload Meta Data To Storage
    private void uploadThumbnailCourseStorage() {
        if (user != null) {

            StorageReference contentRef = storage.getReference("/content/");

            StorageReference courseRef = contentRef.child(dto.getCourseUID());


            StorageReference courseThumbnail = courseRef.child("Photo Thumbnail");
            StorageReference courseSound = courseRef.child("Sound Thumbnail");


            // Photo
            File photo = new File(courseItem.getCoursePath() + DirectoryConstants.meta + DirectoryConstants.photoThumbnail);
            UploadTask photoCourseThumbnail = null;
            if (photo.exists()) {
                photoCourseThumbnail = courseThumbnail.putFile(Uri.fromFile(photo));

                photoCourseThumbnail.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(activity.getApplicationContext(), "Uploaded Thumbnail", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            // Sound
            File sound  = new File(courseItem.getCoursePath() + DirectoryConstants.meta + DirectoryConstants.soundThumbnail);
            UploadTask soundCourseThumbnail = null;

            if (sound.exists()) {
                soundCourseThumbnail = courseSound.putFile(Uri.fromFile(sound));

                soundCourseThumbnail.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(activity.getApplicationContext(), "Uploaded Sound" , Toast.LENGTH_SHORT).show();
                    }
                });

            }


            if (!uploadEntireCourse) {
                uploadLectureDataBase();
            } else {

                // Fetch all the lectures under that course
                uploadEachCourseToDatBase();
                // Upload these on by one to the Firestore and Storage

            }

        } else {
            Toast.makeText(activity, "Must create an Account", Toast.LENGTH_SHORT).show();

        }
    }

    // Step 3 Upload Lecture to Firebase
    private void uploadLectureDataBase() {

        lto = new LectureTransferObject(lectureItem);


        if (lectureItem.getLectureIdentification().isEmpty()) {

            db.collection("content").add(lto).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    lectureItem.setLectureIdentification(documentReference.getId());
                    lto.setLectureUID(lectureItem.getLectureIdentification());

                    FileHandler.createFileForSlideContentAndReturnIt(lectureItem.getLecturePath() + DirectoryConstants.meta, null, activity.getContentResolver(), lectureItem.getLectureIdentification(), FileHandler.ONLINE_LECTURE_IDENTIFICATION);

                    db.collection("content").document(lectureItem.getLectureIdentification()).update("lectureUID", lectureItem.getLectureIdentification()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            listener.onDataLoadedInDatabase();
                            uploadThumbnailLecture();
                        }
                    });

                }
            });
        } else {

            // retrieve key
            lto.setLectureUID(lectureItem.getLectureIdentification());
            FileHandler.createFileForSlideContentAndReturnIt(lectureItem.getLecturePath() + DirectoryConstants.meta, null, activity.getContentResolver(), lectureItem.getLectureIdentification(), FileHandler.ONLINE_LECTURE_IDENTIFICATION);

            db.collection("content").document(lto.getLectureUID()).set(lto).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    listener.onDataLoadedInDatabase();
                    uploadThumbnailLecture();
                }
            });
        }
    }
    // Step 4 Upload Lecture Data To Storage
    private void uploadThumbnailLecture() {
        if (user != null) {
            StorageReference lectureRef = storage.getReference("/content/").child(lectureItem.getLectureIdentification());


            StorageReference lecturePhoto = lectureRef.child("Photo Thumbnail");
            StorageReference lectureSound = lectureRef.child("Sound Thumbnail");


            // Photo
            File photo = new File(lectureItem.getLecturePath() + DirectoryConstants.meta + DirectoryConstants.photoThumbnail);
            UploadTask photoCourseThumbnail = null;
            if (photo.exists()) {
                photoCourseThumbnail = lecturePhoto.putFile(Uri.fromFile(photo));

                photoCourseThumbnail.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(activity.getApplicationContext(), "Uploaded Thumbnail", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            // Sound
            File sound  = new File( lectureItem.getLecturePath() + DirectoryConstants.meta + DirectoryConstants.soundThumbnail);
            UploadTask soundCourseThumbnail = null;

            if (sound.exists()) {
                soundCourseThumbnail = lectureSound.putFile(Uri.fromFile(sound));

                soundCourseThumbnail.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(activity.getApplicationContext(), "Uploaded Sound" , Toast.LENGTH_SHORT).show();
                    }
                });

            }

            // Zip of the Lecture
            if (!uploadEntireCourse) {
                uploadLectureToStorage();
            }

        } else {
            Toast.makeText(activity, "Must create an Account", Toast.LENGTH_SHORT).show();

        }
    }

    // Step 5 Upload Zip File To Storage
    private void uploadLectureToStorage() {

        StorageUploadPreparation prep = new StorageUploadPreparation(lectureItem.getLecturePath(), activity.getApplicationContext());
        byte[] slideData = prep.getZippedCourse();

        StorageReference lectureZip = storage.getReference("/content/").child(lto.getLectureUID()).child("Lecture.zip");



        UploadTask uploadCourseContentZip = lectureZip.putBytes(slideData);
        uploadCourseContentZip.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (listener != null) {
                    listener.onDataLoadedInStorage(dto.getCourseUID(), lto.getLectureUID());
                }
            }
        });

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    public UploadProcedure(CourseItem courseItem, Activity activity) {
        this.courseItem = courseItem;
        this.activity = activity;
    }

    // Upload Entire Course
    public void uploadCourse(boolean entireCourse) {
        uploadEntireCourse = entireCourse;
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();


        if (user != null) {
            courseItem.setAuthorID(user.getUid());

            addAllNecessaryFilesToCourse();

            uploadCourseToDataBase();


        }
    }

    // Step 1 to 2 are the same

    // Step 3b
    private void uploadEachCourseToDatBase() {
        File f = new File(courseItem.getCoursePath() + DirectoryConstants.lectures);

        File[] lectures = f.listFiles();
        final int[] counter = {0};
        int max = lectures.length;

        for (File lecture : lectures) {
            lto = new LectureTransferObject(retrieveLectureContent(lecture));
            // never uploaded to online before
            if (lto.lectureUID.isEmpty()) {
                db.collection("content").add(lto).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        lectureItem.setLectureIdentification(documentReference.getId());
                        lto.setLectureUID(lectureItem.getLectureIdentification());
                        lectureItem.setPath(lecture.getPath());
                        mapLTOItem.put(lto, lectureItem);

                        FileHandler.createFileForSlideContentAndReturnIt(lectureItem.getLecturePath() + DirectoryConstants.meta, null, activity.getContentResolver(), lectureItem.getCourseIdentification(), FileHandler.ONLINE_COURSE_IDENTIFICATION);


                        db.collection("content").document(lectureItem.getLectureIdentification()).update("lectureUID", lectureItem.getLectureIdentification()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                counter[0]++;

                                if (counter[0] == max) {
                                    uploadEachLectureThumbnailToStorage();
                                }
                            }
                        });

                    }
                });

            } else {
                // retrieve key
                dto.setCourseUID(courseItem.getCourseOnlineIdentification());
                lectureItem.setCourseIdentification(courseItem.getCourseOnlineIdentification());
                lectureItem.setPath(lecture.getPath());

                FileHandler.createFileForSlideContentAndReturnIt(lectureItem.getLecturePath() + DirectoryConstants.meta, null, activity.getContentResolver(), lectureItem.getCourseIdentification(), FileHandler.ONLINE_COURSE_IDENTIFICATION);


                mapLTOItem.put(lto, lectureItem);
                db.collection("content").document(lectureItem.getLectureIdentification()).set(lto).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onDataLoadedInDatabase();

                        counter[0]++;

                        if (counter[0] == max) {
                            uploadEachLectureThumbnailToStorage();
                        }
                    }
                });
            }
        }
    }

    private void uploadEachLectureThumbnailToStorage() {

        if (user != null) {

            for (Map.Entry pair : mapLTOItem.entrySet()) {

                StorageReference lectureRef = storage.getReference("/content/").child(((LectureTransferObject) pair.getKey()).lectureUID);


                StorageReference lecturePhoto = lectureRef.child("Photo Thumbnail");
                StorageReference lectureSound = lectureRef.child("Sound Thumbnail");


                // Photo
                File photo = new File(((LectureItem) pair.getValue()).getLecturePath() + DirectoryConstants.meta + DirectoryConstants.photoThumbnail);
                UploadTask photoCourseThumbnail = null;
                if (photo.exists()) {
                    photoCourseThumbnail = lecturePhoto.putFile(Uri.fromFile(photo));

                    photoCourseThumbnail.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(activity.getApplicationContext(), "Uploaded Thumbnail", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

                // Sound
                File sound  = new File( ((LectureItem) pair.getValue()).getLecturePath()  + DirectoryConstants.meta + DirectoryConstants.soundThumbnail);
                UploadTask soundCourseThumbnail = null;

                if (sound.exists()) {
                    soundCourseThumbnail = lectureSound.putFile(Uri.fromFile(sound));

                    soundCourseThumbnail.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(activity.getApplicationContext(), "Uploaded Sound" , Toast.LENGTH_SHORT).show();
                        }
                    });

                }

                StorageUploadPreparation prep = new StorageUploadPreparation(((LectureItem) pair.getValue()).getLecturePath(), activity.getApplicationContext());
                byte[] slideData = prep.getZippedCourse();

                StorageReference lectureZip = storage.getReference("/content/").child(((LectureTransferObject) pair.getKey()).lectureUID).child("Lecture.zip");


                // Add Necessary Files To Lecture Before Zipping

                addNecessaryFilesToLecture(((LectureItem) pair.getValue()).getLecturePath(), ((LectureTransferObject) pair.getKey()).lectureUID);

                UploadTask uploadCourseContentZip = lectureZip.putBytes(slideData);
                uploadCourseContentZip.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if (listener != null) {
                            LectureTransferObject newLTO = (LectureTransferObject) pair.getKey();
                            tidyZipFolder();

                            listener.onDataLoadedInStorageEntireCourse(newLTO.getCourseUID(), newLTO.lectureUID,((LectureItem)pair.getValue()).getLecturePath()) ;
                        }
                    }
                });

            }


        } else {
            Toast.makeText(activity, "Must create an Account", Toast.LENGTH_SHORT).show();

        }

    }

    private  void addNecessaryFilesToLecture(String path, String key) {
        FileHandler.createFileForSlideContentAndReturnIt(path + DirectoryConstants.meta, null, activity.getContentResolver(), courseItem.getLanguage(), FileHandler.LANGUAGE_SELECTION);
        FileHandler.createFileForSlideContentAndReturnIt(path + DirectoryConstants.meta, null, activity.getContentResolver(), courseItem.getCategory(), FileHandler.CATEGORY_SELECTION);
        FileHandler.createFileForSlideContentAndReturnIt(path + DirectoryConstants.meta, null, activity.getContentResolver(), courseItem.getAuthorID(), FileHandler.AUTHOR);
        FileHandler.createFileForSlideContentAndReturnIt(path + DirectoryConstants.meta, null, activity.getContentResolver(), key, FileHandler.ONLINE_LECTURE_IDENTIFICATION);


    }



    private void tidyZipFolder() {
        File f = new File(activity.getExternalFilesDir(null) + DirectoryConstants.zip);

        File[] files = f.listFiles();

        for (File file : files) {
            file.delete();
        }
    }


    private LectureItem retrieveLectureContent(File lecture) {
        String authorUID = user.getUid();
        String courseUID = courseItem.getCourseOnlineIdentification();
        String language = courseItem.getLanguage();
        String category = courseItem.getCategory();
        String lectureName = FileReaderHelper.readTextFromFile(lecture.getPath() + DirectoryConstants.meta + DirectoryConstants.title);
        String lectureUID = FileReaderHelper.readTextFromFile(lecture.getPath() + DirectoryConstants.meta + DirectoryConstants.lectureIdentifcation);
        String courseName = courseItem.getCourseName();

        lectureItem = new LectureItem(lectureName, lectureUID, true);
        lectureItem.setCategory(category);
        lectureItem.setLanguage(language);
        lectureItem.setAuthorID(authorUID);
        lectureItem.setCourseIdentification(courseUID);
        lectureItem.setCourseName(courseName);

        return lectureItem;
    }






    public void setListener(UploadListener listener) {
        this.listener = listener;
    }


    public interface UploadListener {
        void onDataLoadedInDatabase();
        void onDataLoadedInStorage(String courseIdentification, String lectureIdentification);
        void onDataLoadedInStorageEntireCourse(String courseIdentification, String lectureIdentification, String lecturePath);
    }
}
