package com.imperial.word2mouth.previous.shared;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;


public class CourseItem implements Parcelable {

    private String courseBluetooth;

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    private String courseName = null;
    private File thumbnail = null;
    private File audio = null;


    private String coursePath = null;

    private String language = null;
    private String category = null;


    private String authorID = "";
    private String courseOnlineIdentification = null;

    // For offline purposes
    public CourseItem(String courseName, String path) {
        this.courseName = courseName;
        this.coursePath = path;

        thumbnail = new File(path + "/meta" + "/thumbnail.jpg");

        audio = new File(path + "/meta" + "/audio.3gp");

        language = FileReaderHelper.readTextFromFile(path + DirectoryConstants.meta + DirectoryConstants.language);
        category = FileReaderHelper.readTextFromFile(path + DirectoryConstants.meta + DirectoryConstants.category);

    }

    // For online purposes
    public CourseItem(String courseName, String id, boolean online) {
        this.courseName = courseName;
        this.courseOnlineIdentification = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public File getThumbnail() {
        return thumbnail;
    }

    public File getAudio() {
        return audio;
    }

    public String getCoursePath() {
        return coursePath;

    }

    public String getCourseOnlineIdentification() {
        return courseOnlineIdentification;
    }

    public void setCourseOnlineIdentification(String courseOnlineIdentification) {
        this.courseOnlineIdentification = courseOnlineIdentification;
    }

    public String getLanguage() {
        return language;
    }

    public String getCategory() {
        return category;
    }


    public void setLanguage(String language) {
        this.language = language;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) {
        this.authorID = authorID;
    }

    public void setCoursePath(String coursePath) {
        this.coursePath = coursePath;
    }


    public String getBluetoothCourse() {
        return courseBluetooth;
    }

    public void setCourseBluetooth(String courseBluetooth) {
        this.courseBluetooth = courseBluetooth;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.courseBluetooth);
        dest.writeString(this.courseName);
        dest.writeSerializable(this.thumbnail);
        dest.writeSerializable(this.audio);
        dest.writeString(this.coursePath);
        dest.writeString(this.language);
        dest.writeString(this.category);
        dest.writeString(this.authorID);
        dest.writeString(this.courseOnlineIdentification);
    }

    protected CourseItem(Parcel in) {
        this.courseBluetooth = in.readString();
        this.courseName = in.readString();
        this.thumbnail = (File) in.readSerializable();
        this.audio = (File) in.readSerializable();
        this.coursePath = in.readString();
        this.language = in.readString();
        this.category = in.readString();
        this.authorID = in.readString();
        this.courseOnlineIdentification = in.readString();
    }

    public static final Parcelable.Creator<CourseItem> CREATOR = new Parcelable.Creator<CourseItem>() {
        @Override
        public CourseItem createFromParcel(Parcel source) {
            return new CourseItem(source);
        }

        @Override
        public CourseItem[] newArray(int size) {
            return new CourseItem[size];
        }
    };
}
