package com.imperial.word2mouth.previous.teach.offline.upload;

import android.app.Activity;
import android.net.Uri;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.previous.shared.TopicItem;
import com.imperial.word2mouth.helpers.FileSystemConstants;
import com.imperial.word2mouth.previous.shared.FileHandler;
import com.imperial.word2mouth.previous.shared.FileReaderHelper;
import com.imperial.word2mouth.previous.shared.PrevLectureItem;
import com.imperial.word2mouth.previous.teach.offline.upload.database.CourseTransferObject;
import com.imperial.word2mouth.previous.teach.offline.upload.database.LectureTrackerObject;
import com.imperial.word2mouth.previous.teach.offline.upload.database.LectureTransferObject;
import com.imperial.word2mouth.previous.teach.offline.upload.storage.StorageUploadPreparation;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private PrevLectureItem prevLectureItem;
    private TopicItem topicItem;

    private UploadListener listener;
    private boolean uploadEntireCourse = false;
    private HashMap<LectureTransferObject, PrevLectureItem> mapLTOItem = new HashMap<>();
    private String previousVersion = null;
    private int numberSlidesPerLecture;


    ////////////////////////////////////////////////////////////////////////////////////////////////

    public UploadProcedure(TopicItem topicItem, PrevLectureItem prevLectureItem, Activity activity) {
        this.topicItem = topicItem;
        this.prevLectureItem = prevLectureItem;
        this.activity = activity;
    }


    // Upload only a Lecture (may need to upload the course if it does not exist)
    public void uploadCourse() {
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();


        if (user != null) {
            if (topicItem.getAuthorID().isEmpty()) {
                topicItem.setAuthorID(user.getUid());
                prevLectureItem.setAuthorID(user.getUid());
                addAllNecessaryFilesToCourse();
                addAllNecessaryFilesToLecture();
                uploadCourseToDataBase();
            } else {
                if (!user.getUid().equals(topicItem.getAuthorID())) {
                    Toast.makeText(activity.getApplicationContext(), R.string.uploadOtherTeacherContent, Toast.LENGTH_SHORT).show();
                } else {
                    topicItem.setAuthorID(user.getUid());
                    prevLectureItem.setAuthorID(user.getUid());
                    addAllNecessaryFilesToCourse();
                    addAllNecessaryFilesToLecture();
                    uploadCourseToDataBase();
                }
            }
        }  else {
            Toast.makeText(activity.getApplicationContext(), R.string.TeacherLoginToUpload, Toast.LENGTH_SHORT).show();

        }


    }

    private void addAllNecessaryFilesToLecture() {
        FileHandler.createFileForSlideContentAndReturnIt( prevLectureItem.getLecturePath() + FileSystemConstants.meta, null, activity.getContentResolver(), prevLectureItem.getLanguage(), FileHandler.LANGUAGE_SELECTION);
        FileHandler.createFileForSlideContentAndReturnIt(prevLectureItem.getLecturePath() + FileSystemConstants.meta, null, activity.getContentResolver(), prevLectureItem.getCategory(), FileHandler.CATEGORY_SELECTION);
        FileHandler.createFileForSlideContentAndReturnIt(prevLectureItem.getLecturePath() + FileSystemConstants.meta, null, activity.getContentResolver(), prevLectureItem.getAuthorUID(), FileHandler.AUTHOR);

        previousVersion = FileReaderHelper.readTextFromFile(prevLectureItem.getLecturePath() + FileSystemConstants.meta + FileSystemConstants.versionLecture);

        prevLectureItem.setVersion(UUID.randomUUID().toString());

        numberSlidesPerLecture = new File(prevLectureItem.getLecturePath() + FileSystemConstants.slides).listFiles().length;
        FileHandler.createFileForSlideContentAndReturnIt(prevLectureItem.getLecturePath() + FileSystemConstants.meta, null, activity.getContentResolver(), prevLectureItem.getVersion(), FileHandler.VERSION);
        FileHandler.createFileForLectureTracking(prevLectureItem, activity);

    }

    private void addAllNecessaryFilesToCourse() {
        FileHandler.createFileForSlideContentAndReturnIt( topicItem.getCoursePath() + FileSystemConstants.meta, null, activity.getContentResolver(), topicItem.getLanguage(), FileHandler.LANGUAGE_SELECTION);
        FileHandler.createFileForSlideContentAndReturnIt(topicItem.getCoursePath() + FileSystemConstants.meta, null, activity.getContentResolver(), topicItem.getCategory(), FileHandler.CATEGORY_SELECTION);
        FileHandler.createFileForSlideContentAndReturnIt(topicItem.getCoursePath() + FileSystemConstants.meta, null, activity.getContentResolver(), topicItem.getAuthorID(), FileHandler.AUTHOR);
    }


    // Step 1 Upload Course to Firebase
    private void uploadCourseToDataBase() {
        if (user != null) {
            dto = new CourseTransferObject(topicItem);
            contentRef = database.getReference("/content/");

            // Allow Speak Search
            dto.setLowerCapital();

            if (topicItem.getCourseOnlineIdentification() == null || topicItem.getCourseOnlineIdentification().isEmpty()) {

                db.collection("content").add(dto).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        dto.setCourseUID(documentReference.getId());
                        if (!uploadEntireCourse) {
                            prevLectureItem.setCourseIdentification(dto.getCourseUID());
                            FileHandler.createFileForSlideContentAndReturnIt(prevLectureItem.getLecturePath() + FileSystemConstants.meta, null, activity.getContentResolver(), prevLectureItem.getCourseIdentification(), FileHandler.ONLINE_COURSE_IDENTIFICATION);

                        }
                        topicItem.setCourseOnlineIdentification(dto.getCourseUID());

                        // Update
                        FileHandler.createFileForSlideContentAndReturnIt(topicItem.getCoursePath() + FileSystemConstants.meta, null, activity.getContentResolver(), topicItem.getCourseOnlineIdentification(), FileHandler.ONLINE_COURSE_IDENTIFICATION);


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
                dto.setCourseUID(topicItem.getCourseOnlineIdentification());
                if (!uploadEntireCourse) {
                    prevLectureItem.setCourseIdentification(topicItem.getCourseOnlineIdentification());
                    FileHandler.createFileForSlideContentAndReturnIt(prevLectureItem.getLecturePath() + FileSystemConstants.meta, null, activity.getContentResolver(), prevLectureItem.getCourseIdentification(), FileHandler.ONLINE_COURSE_IDENTIFICATION);

                }

                FileHandler.createFileForSlideContentAndReturnIt(topicItem.getCoursePath() + FileSystemConstants.meta, null, activity.getContentResolver(), topicItem.getCourseOnlineIdentification(), FileHandler.ONLINE_COURSE_IDENTIFICATION);

                db.collection("content").document(topicItem.getCourseOnlineIdentification()).set(dto).addOnSuccessListener(new OnSuccessListener<Void>() {
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
            File photo = new File(topicItem.getCoursePath() + FileSystemConstants.meta + FileSystemConstants.photoThumbnail);
            UploadTask photoCourseThumbnail = null;
            if (photo.exists()) {
                photoCourseThumbnail = courseThumbnail.putFile(Uri.fromFile(photo));

                photoCourseThumbnail.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    }
                });

            }

            // Sound
            File sound  = new File(topicItem.getCoursePath() + FileSystemConstants.meta + FileSystemConstants.audioThumbnail);
            UploadTask soundCourseThumbnail = null;

            if (sound.exists()) {
                soundCourseThumbnail = courseSound.putFile(Uri.fromFile(sound));

                soundCourseThumbnail.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    }
                });

            }


            if (!uploadEntireCourse) {
                uploadLectureDataBase();
            } else {

                // Fetch all the lectures under that course
                // Upload these on by one to the Firestore and Storage
                uploadAllLecturesToDatabase();

            }

        } else {
            Toast.makeText(activity, R.string.mustCreateAccount, Toast.LENGTH_SHORT).show();

        }
    }

    // Step 3 Upload Lecture to Firebase
    private void uploadLectureDataBase() {

        lto = new LectureTransferObject(prevLectureItem);

        lto.versionUID = prevLectureItem.getVersion();

        // delete current version
        if (!previousVersion.isEmpty() && previousVersion != null) {
            // delete from firebase
            db.collection("track").whereEqualTo("version", previousVersion).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                    for (DocumentSnapshot doc : docs) {
                        doc.getReference().delete();
                    }
                }
            });

            // delete file from tracker
            File trackingData = new File(activity.getExternalFilesDir(null) + FileSystemConstants.lecturerTracking +  previousVersion + ".txt");
            if (trackingData.exists()) {
                trackingData.delete();
            }
        }

        LectureTrackerObject lTrackO = new LectureTrackerObject(lto.versionUID, numberSlidesPerLecture);

        db.collection("track").add(lTrackO).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {

            }
        });

        if (prevLectureItem.getLectureIdentification().isEmpty()) {


            db.collection("content").add(lto).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    prevLectureItem.setLectureIdentification(documentReference.getId());
                    lto.setLectureUID(prevLectureItem.getLectureIdentification());

                    FileHandler.createFileForSlideContentAndReturnIt(prevLectureItem.getLecturePath() + FileSystemConstants.meta, null, activity.getContentResolver(), prevLectureItem.getLectureIdentification(), FileHandler.ONLINE_LECTURE_IDENTIFICATION);

                    db.collection("content").document(prevLectureItem.getLectureIdentification()).update("lectureUID", prevLectureItem.getLectureIdentification()).addOnSuccessListener(new OnSuccessListener<Void>() {
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
            lto.setLectureUID(prevLectureItem.getLectureIdentification());
            FileHandler.createFileForSlideContentAndReturnIt(prevLectureItem.getLecturePath() + FileSystemConstants.meta, null, activity.getContentResolver(), prevLectureItem.getLectureIdentification(), FileHandler.ONLINE_LECTURE_IDENTIFICATION);

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
            StorageReference lectureRef = storage.getReference("/content/").child(prevLectureItem.getLectureIdentification());


            StorageReference lecturePhoto = lectureRef.child("Photo Thumbnail");
            StorageReference lectureSound = lectureRef.child("Sound Thumbnail");


            // Photo
            File photo = new File(prevLectureItem.getLecturePath() + FileSystemConstants.meta + FileSystemConstants.photoThumbnail);
            UploadTask photoCourseThumbnail = null;
            if (photo.exists()) {
                photoCourseThumbnail = lecturePhoto.putFile(Uri.fromFile(photo));

                photoCourseThumbnail.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    }
                });

            }

            // Sound
            File sound  = new File( prevLectureItem.getLecturePath() + FileSystemConstants.meta + FileSystemConstants.audioThumbnail);
            UploadTask soundCourseThumbnail = null;

            if (sound.exists()) {
                soundCourseThumbnail = lectureSound.putFile(Uri.fromFile(sound));

                soundCourseThumbnail.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    }
                });

            }

            // Zip of the Lecture
            if (!uploadEntireCourse) {
                uploadLectureToStorage();
            }

        } else {
            Toast.makeText(activity, R.string.mustCreateAccount, Toast.LENGTH_SHORT).show();

        }
    }

    // Step 5 Upload Zip File To Storage
    private void uploadLectureToStorage() {

        StorageUploadPreparation prep = new StorageUploadPreparation(prevLectureItem.getLecturePath(), activity.getApplicationContext());
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

    public UploadProcedure(TopicItem topicItem, Activity activity) {
        this.topicItem = topicItem;
        this.activity = activity;
    }

    // Upload Entire Course
    public void uploadCourse(boolean entireCourse) {
        uploadEntireCourse = entireCourse;
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();


        if (user != null) {
            if (topicItem.getAuthorID().isEmpty()) {
                topicItem.setAuthorID(user.getUid());
                addAllNecessaryFilesToCourse();
                uploadCourseToDataBase();
            } else {
                if (!user.getUid().equals(topicItem.getAuthorID())) {
                    Toast.makeText(activity, R.string.uploadOtherTeacherContent, Toast.LENGTH_SHORT).show();
                } else {
                    topicItem.setAuthorID(user.getUid());
                    addAllNecessaryFilesToCourse();
                    uploadCourseToDataBase();
                }
            }
        } else {
            Toast.makeText(activity, R.string.mustCreateAccount, Toast.LENGTH_SHORT).show();

        }


    }

    // Step 1 to 2 are the same

    // Step 3b
    private void uploadAllLecturesToDatabase() {
        File f = new File(topicItem.getCoursePath() + FileSystemConstants.lectures);

        File[] lectures = f.listFiles();
        final int[] counter = {0};
        int max = lectures.length;

        for (File lecture : lectures) {

            // delete current version
            String version = getCurrentVersion(lecture);

            if (!version.isEmpty() && version != null) {
                // delete from firebase
                db.collection("track").whereEqualTo("version", version).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot doc : docs) {
                            doc.getReference().delete();
                        }
                    }
                });
            }

            // delete file from tracker
            File trackingData = new File(activity.getExternalFilesDir(null) + FileSystemConstants.lecturerTracking +  version + ".txt");
            if (trackingData.exists()) {
                trackingData.delete();
            }


            PrevLectureItem temp = retrieveLectureContent(lecture);
            lto = new LectureTransferObject(temp);
            lto.versionUID = temp.getVersion();
            // Upload the version of the lecture
            FileHandler.createFileForSlideContentAndReturnIt(lecture.getPath() + FileSystemConstants.meta, null, activity.getContentResolver(), prevLectureItem.getVersion(), FileHandler.VERSION);

            LectureTrackerObject lTrackO = new LectureTrackerObject(lto.versionUID, collectNumberSlides(lecture));

            db.collection("track").add(lTrackO).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {

                }
            });
            lto.setBluetoothLecture(FileReaderHelper.readTextFromFile(lecture.getAbsolutePath() + FileSystemConstants.meta + FileSystemConstants.lectureBluetooth));
            // never uploaded to online before
            if (lto.lectureUID.isEmpty()) {
                db.collection("content").add(lto).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        prevLectureItem.setLectureIdentification(documentReference.getId());
                        lto.setLectureUID(prevLectureItem.getLectureIdentification());
                        prevLectureItem.setPath(lecture.getPath());
                        mapLTOItem.put(lto, prevLectureItem);

                        FileHandler.createFileForSlideContentAndReturnIt(prevLectureItem.getLecturePath() + FileSystemConstants.meta, null, activity.getContentResolver(), prevLectureItem.getCourseIdentification(), FileHandler.ONLINE_COURSE_IDENTIFICATION);


                        db.collection("content").document(prevLectureItem.getLectureIdentification()).update("lectureUID", prevLectureItem.getLectureIdentification()).addOnSuccessListener(new OnSuccessListener<Void>() {
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
                dto.setCourseUID(topicItem.getCourseOnlineIdentification());
                prevLectureItem.setCourseIdentification(topicItem.getCourseOnlineIdentification());
                prevLectureItem.setPath(lecture.getPath());
                lto.setBluetoothLecture(FileReaderHelper.readTextFromFile(lecture.getAbsolutePath() + FileSystemConstants.meta + FileSystemConstants.lectureBluetooth));

                FileHandler.createFileForSlideContentAndReturnIt(prevLectureItem.getLecturePath() + FileSystemConstants.meta, null, activity.getContentResolver(), prevLectureItem.getCourseIdentification(), FileHandler.ONLINE_COURSE_IDENTIFICATION);


                mapLTOItem.put(lto, prevLectureItem);
                db.collection("content").document(prevLectureItem.getLectureIdentification()).set(lto).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    private String getCurrentVersion(File lecture) {
        File f = new File(lecture.getPath() + FileSystemConstants.meta + FileSystemConstants.versionLecture);
        return FileReaderHelper.readTextFromFile(f.getPath());
    }

    private void uploadEachLectureThumbnailToStorage() {

        if (user != null) {

            for (Map.Entry pair : mapLTOItem.entrySet()) {

                StorageReference lectureRef = storage.getReference("/content/").child(((LectureTransferObject) pair.getKey()).lectureUID);


                StorageReference lecturePhoto = lectureRef.child("Photo Thumbnail");
                StorageReference lectureSound = lectureRef.child("Sound Thumbnail");


                // Photo
                File photo = new File(((PrevLectureItem) pair.getValue()).getLecturePath() + FileSystemConstants.meta + FileSystemConstants.photoThumbnail);
                UploadTask photoCourseThumbnail = null;
                if (photo.exists()) {
                    photoCourseThumbnail = lecturePhoto.putFile(Uri.fromFile(photo));

                    photoCourseThumbnail.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    });

                }

                // Sound
                File sound  = new File( ((PrevLectureItem) pair.getValue()).getLecturePath()  + FileSystemConstants.meta + FileSystemConstants.audioThumbnail);
                UploadTask soundCourseThumbnail = null;

                if (sound.exists()) {
                    soundCourseThumbnail = lectureSound.putFile(Uri.fromFile(sound));

                    soundCourseThumbnail.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        }
                    });

                }

                StorageUploadPreparation prep = new StorageUploadPreparation(((PrevLectureItem) pair.getValue()).getLecturePath(), activity.getApplicationContext());
                byte[] slideData = prep.getZippedCourse();

                StorageReference lectureZip = storage.getReference("/content/").child(((LectureTransferObject) pair.getKey()).lectureUID).child("Lecture.zip");


                // Add Necessary Files To Lecture Before Zipping

                addNecessaryFilesToLecture(((PrevLectureItem) pair.getValue()).getLecturePath(), ((LectureTransferObject) pair.getKey()).lectureUID);

                UploadTask uploadCourseContentZip = lectureZip.putBytes(slideData);
                uploadCourseContentZip.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if (listener != null) {
                            LectureTransferObject newLTO = (LectureTransferObject) pair.getKey();
                            tidyZipFolder();

                            listener.onDataLoadedInStorageEntireCourse(newLTO.getCourseUID(), newLTO.lectureUID,((PrevLectureItem)pair.getValue()).getLecturePath()) ;
                        }
                    }
                });

            }


        } else {
            Toast.makeText(activity, R.string.mustCreateAccount, Toast.LENGTH_SHORT).show();

        }

    }

    private  void addNecessaryFilesToLecture(String path, String key) {
        FileHandler.createFileForSlideContentAndReturnIt(path + FileSystemConstants.meta, null, activity.getContentResolver(), topicItem.getLanguage(), FileHandler.LANGUAGE_SELECTION);
        FileHandler.createFileForSlideContentAndReturnIt(path + FileSystemConstants.meta, null, activity.getContentResolver(), topicItem.getCategory(), FileHandler.CATEGORY_SELECTION);
        FileHandler.createFileForSlideContentAndReturnIt(path + FileSystemConstants.meta, null, activity.getContentResolver(), topicItem.getAuthorID(), FileHandler.AUTHOR);
        FileHandler.createFileForSlideContentAndReturnIt(path + FileSystemConstants.meta, null, activity.getContentResolver(), key, FileHandler.ONLINE_LECTURE_IDENTIFICATION);

    }



    private void tidyZipFolder() {
        File f = new File(activity.getExternalFilesDir(null) + FileSystemConstants.zip);

        File[] files = f.listFiles();

        for (File file : files) {
            file.delete();
        }
    }


    private PrevLectureItem retrieveLectureContent(File lecture) {
        String authorUID = user.getUid();
        String courseUID = topicItem.getCourseOnlineIdentification();
        String language = topicItem.getLanguage();
        String category = topicItem.getCategory();
        String lectureName = FileReaderHelper.readTextFromFile(lecture.getPath() + FileSystemConstants.meta + FileSystemConstants.title);
        String lectureUID = FileReaderHelper.readTextFromFile(lecture.getPath() + FileSystemConstants.meta + FileSystemConstants.lectureIdentifcation);
        String courseName = topicItem.getCourseName();

        prevLectureItem = new PrevLectureItem(lectureName, lectureUID, true);
        prevLectureItem.setCategory(category);
        prevLectureItem.setLanguage(language);
        prevLectureItem.setAuthorID(authorUID);
        prevLectureItem.setCourseIdentification(courseUID);
        prevLectureItem.setCourseName(courseName);
        prevLectureItem.setBluetoothLecture(FileReaderHelper.readTextFromFile(lecture.getPath() + FileSystemConstants.meta + FileSystemConstants.lectureBluetooth));
        prevLectureItem.setBluetoothCourse(FileReaderHelper.readTextFromFile(lecture.getPath() + FileSystemConstants.meta + FileSystemConstants.courseBluetooth));
        prevLectureItem.setVersion(UUID.randomUUID().toString());

        return prevLectureItem;
    }

    private int collectNumberSlides(File lecture) {
        File f = new File(lecture + FileSystemConstants.slides);
        return f.listFiles().length;
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
