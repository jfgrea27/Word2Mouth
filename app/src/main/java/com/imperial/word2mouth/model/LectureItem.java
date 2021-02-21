package com.imperial.word2mouth.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

public class LectureItem implements Parcelable {

    /////////
    // File Storage data
    private UUID uuidLecture;

    /////////////////////////////
    // Lecture Meta data
    private final String lectureName;
    // CourseItem
    private final CourseItem courseItem;

    ///////////////////////////////////////////
    // File System
    private String lecturePath;
    private String slidePath;
    private String lectureImageThumbnailPath;
    private String lectureAudioThumbnailPath;


    ////////////////////////////////////////////
    // Firebase stuff here
    private String lectureFirebaseAuthorID;
    private String lectureFirebaseLectureID;

    public LectureItem(CourseItem courseItem, String lectureName) {
        this.courseItem = courseItem;
        this.lectureName = lectureName;
        uuidLecture = UUID.randomUUID();
    }



    public static final Creator<LectureItem> CREATOR = new Creator<LectureItem>() {
        @Override
        public LectureItem createFromParcel(Parcel in) {
            return new LectureItem(in);
        }

        @Override
        public LectureItem[] newArray(int size) {
            return new LectureItem[size];
        }
    };

    public String getLectureFirebaseAuthorID() {
        return lectureFirebaseAuthorID;
    }

    public void setLectureFirebaseAuthorID(String lectureFirebaseAuthorID) {
        this.lectureFirebaseAuthorID = lectureFirebaseAuthorID;
    }

    public String getLectureFirebaseLectureID() {
        return lectureFirebaseLectureID;
    }

    public void setLectureFirebaseLectureID(String lectureFirebaseLectureID) {
        this.lectureFirebaseLectureID = lectureFirebaseLectureID;
    }

    public String getLecturePath() {
        return lecturePath;
    }

    public void setLecturePath(String lecturePath) {
        this.lecturePath = lecturePath;
    }

    public String getSlidePath() {
        return slidePath;
    }

    public void setSlidePath(String slidePath) {
        this.slidePath = slidePath;
    }

    public String getLectureImageThumbnailPath() {
        return lectureImageThumbnailPath;
    }

    public void setLectureImageThumbnailPath(String lectureImageThumbnailPath) {
        this.lectureImageThumbnailPath = lectureImageThumbnailPath;
    }

    public String getLectureAudioThumbnailPath() {
        return lectureAudioThumbnailPath;
    }

    public void setLectureAudioThumbnailPath(String lectureAudioThumbnailPath) {
        this.lectureAudioThumbnailPath = lectureAudioThumbnailPath;
    }

    public CourseItem getCourseItem() {
        return this.courseItem;
    }

    public UUID getUuidLecture() {
        return this.uuidLecture;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.lectureName);
        dest.writeString(this.uuidLecture.toString());
        dest.writeParcelable(this.courseItem, 0);
        dest.writeString(this.lecturePath);
        dest.writeString(this.slidePath);
        dest.writeString(this.lectureImageThumbnailPath);
        dest.writeString(this.lectureAudioThumbnailPath);
        dest.writeString(this.lectureFirebaseAuthorID);
        dest.writeString(this.lectureFirebaseLectureID);
    }

    protected LectureItem(Parcel in) {
        lectureName = in.readString();
        uuidLecture= UUID.fromString(in.readString());
        courseItem = (CourseItem) in.readParcelable(CourseItem.class.getClassLoader());
        lecturePath = in.readString();
        slidePath = in.readString();
        lectureImageThumbnailPath = in.readString();
        lectureAudioThumbnailPath = in.readString();
        lectureFirebaseAuthorID = in.readString();
        lectureFirebaseLectureID = in.readString();
    }
}

