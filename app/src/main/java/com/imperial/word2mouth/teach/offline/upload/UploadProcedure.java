package com.imperial.word2mouth.teach.offline.upload;

import android.app.Activity;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.teach.offline.upload.database.DataTransferObject;
import com.imperial.word2mouth.teach.offline.upload.storage.StorageUploadPreparation;

import java.io.File;

public class UploadProcedure {

    private String courseIdentification;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private FirebaseUser user;

    private String courseName;
    private final String coursePath;
    private Activity activity;

    private UploadListener listener;
    private DatabaseReference teacherDatabaseRef;
    private DataTransferObject dto;

    public UploadProcedure(String courseName, String coursePath, String courseIdentification, Activity activity) {

        this.courseName = courseName;
        this.coursePath = coursePath;
        this.activity = activity;
        this.courseIdentification = courseIdentification;

    }


    public void uploadCourse() {
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();


        uploadToDataBase();

        uploadToStorage();
    }
    private void uploadToStorage() {
        if (user != null) {

            StorageUploadPreparation prep = new StorageUploadPreparation(coursePath, activity.getApplicationContext());
            byte[] slideData = prep.getZippedCourse();

            String courseZip = courseName + ".zip";

            StorageReference teacherRef = storage.getReference(dto.getUserName());

            // if there is no reference for that teacher yet.
            if (teacherRef == null) {
                teacherRef = storage.getReference().child(dto.getUserName());
            }


            // Create file metadata including the name of the course
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setCustomMetadata("Course Name", courseName)
                    .build();

            StorageReference courseRef = teacherRef.child(courseName + dto.getKey());

            StorageReference courseSlidedZipped = courseRef.child(courseZip);

            StorageReference courseThumbnail = courseRef.child("Photo Thumbnail");
            StorageReference courseSound = courseRef.child("Sound Thumbnail");

            UploadTask uploadCourseContentZip = courseSlidedZipped.putBytes(slideData, metadata);

            // Photo
            File photo = new File(coursePath + DirectoryConstants.meta + DirectoryConstants.photoThumbnail);
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
            File sound  = new File(coursePath + DirectoryConstants.meta + DirectoryConstants.soundThumbnail);
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


            final DatabaseReference finalTeacherDatabaseRef = teacherDatabaseRef;
            uploadCourseContentZip.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    if (listener != null) {
                        dto.setCourseURL(courseRef.getDownloadUrl().toString());
                        finalTeacherDatabaseRef.child(dto.getKey()).child("courseURL").setValue(courseRef.getDownloadUrl().toString());
                        listener.onDataLoadedInStorage(dto.getKey());
                    }
                }
            });



        } else {
            Toast.makeText(activity, "Must create an Account", Toast.LENGTH_SHORT).show();

        }
    }

    private void uploadToDataBase() {
        if (user != null) {

            // New Course to upload
            if (courseIdentification == "") {
                dto = new DataTransferObject(user.getEmail(), courseName);


                teacherDatabaseRef = database.getReference(dto.getUserName());
                DatabaseReference courseRef = teacherDatabaseRef.push();
                courseIdentification = courseRef.getKey();
                dto.setFileKey(courseIdentification);
                dto.setCourseURL(" ");
                courseRef.setValue(dto);

                courseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listener.onDataLoadedInDatabase();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else {
                dto = new DataTransferObject(user.getEmail(), courseName);
                dto.setFileKey(courseIdentification);

                teacherDatabaseRef = database.getReference(dto.getUserName());
                listener.onDataLoadedInDatabase();

            }

        }

    }

    public void setListener(UploadListener listener) {
        this.listener = listener;
    }

    public interface UploadListener {
        public void onDataLoadedInDatabase();
        public void onDataLoadedInStorage(String courseIdentification);

    }
}
