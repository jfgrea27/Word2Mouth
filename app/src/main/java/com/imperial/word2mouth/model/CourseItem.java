package com.imperial.word2mouth.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.UUID;

public class CourseItem implements Parcelable, Serializable {

    ////////////////////////////////////////////
    // File storage data
    private UUID uuidCourse;

    ////////////////////////////////////////////
    // Course metadata
    private final String courseName;
    private final String courseLanguage;
    private final String courseTopic;

    ///////////////////////////////////////////
    // File System
    private String coursePath;
    private String courseAudioThumbnailPath;
    private String courseImageThumbnailPath;
    private String courseLecturePath;
    private String courseJSONMetaDataPath;

    ////////////////////////////////////////////
    // Firebase stuff here
    private String courseFirebaseAuthorID;
    private String courseFirebaseCourseID;

    //TODO complete this when you get to it


    public CourseItem(String courseName, String courseLanguage, String courseTopic) {
        this.uuidCourse = UUID.randomUUID();
        this.courseName = courseName;
        this.courseLanguage = courseLanguage;
        this.courseTopic = courseTopic;
    }

    public CourseItem(String courseUUID, String courseName, String courseLanguage, String courseTopic) {
        this.uuidCourse = UUID.fromString(courseUUID);
        this.courseName = courseName;
        this.courseLanguage = courseLanguage;
        this.courseTopic = courseTopic;
    }


    protected CourseItem(Parcel in) {
        courseName = in.readString();
        uuidCourse = UUID.fromString(in.readString());
        courseLanguage = in.readString();
        courseTopic = in.readString();
        coursePath = in.readString();
        courseLecturePath = in.readString();
        courseJSONMetaDataPath = in.readString();
        courseAudioThumbnailPath = in.readString();
        courseImageThumbnailPath = in.readString();
        courseFirebaseAuthorID = in.readString();
        courseFirebaseCourseID = in.readString();
    }

    public static final Creator<CourseItem> CREATOR = new Creator<CourseItem>() {
        @Override
        public CourseItem createFromParcel(Parcel in) {
            return new CourseItem(in);
        }

        @Override
        public CourseItem[] newArray(int size) {
            return new CourseItem[size];
        }
    };

    public void setCourseAudioThumbnailPath(String courseAudioThumbnailPath) {
        this.courseAudioThumbnailPath = courseAudioThumbnailPath;
    }

    public void setCourseImageThumbnailPath(String courseImageThumbnailPath) {
        this.courseImageThumbnailPath = courseImageThumbnailPath;
    }

    public UUID getUuidCourse() {
        return this.uuidCourse;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCourseLanguage() {
        return courseLanguage;
    }

    public String getCourseTopic() {
        return courseTopic;
    }

    public String getCourseAudioThumbnailPath() {
        return courseAudioThumbnailPath;
    }

    public String getCourseImageThumbnailPath() {
        return courseImageThumbnailPath;
    }

    public String getCourseFirebaseAuthorID() {
        return courseFirebaseAuthorID;
    }

    public void setCourseFirebaseAuthorID(String courseFirebaseAuthorID) {
        this.courseFirebaseAuthorID = courseFirebaseAuthorID;
    }

    public String getCourseFirebaseCourseID() {
        return courseFirebaseCourseID;
    }

    public void setCourseFirebaseCourseID(String courseFirebaseCourseID) {
        this.courseFirebaseCourseID = courseFirebaseCourseID;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.courseName);
        dest.writeString(this.uuidCourse.toString());
        dest.writeString(this.courseLanguage);
        dest.writeString(this.courseTopic);
        dest.writeString(this.coursePath);
        dest.writeString(this.courseLecturePath);
        dest.writeString(this.courseJSONMetaDataPath);
        dest.writeString(this.courseAudioThumbnailPath);
        dest.writeString(this.courseImageThumbnailPath);
        dest.writeString(this.courseFirebaseAuthorID);
        dest.writeString(this.courseFirebaseCourseID);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void setCourseRootPath(String coursePath) {
        this.coursePath = coursePath;
    }

    public String getCoursePath() {
        return this.coursePath;
    }

    public void setCourseLecturePath(String lecturePath) {
        this.courseLecturePath = lecturePath;
    }

    public void setCourseJSONMetaDataPath(String metaPath) {
        this.courseJSONMetaDataPath = metaPath;
    }

    public String getCourseLecturePath() {
        return this.courseLecturePath;
    }

    public String getCourseJSONMetaDataPath() {
        return this.courseJSONMetaDataPath;
    }
}


