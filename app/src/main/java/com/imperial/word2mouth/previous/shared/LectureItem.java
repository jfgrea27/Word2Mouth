package com.imperial.word2mouth.previous.shared;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class LectureItem implements Parcelable {

    private String courseName = null;
    private String lectureName = null;
    private File thumbnail = null;
    private File audio = null;

    public void setLecturePath(String lecturePath) {
        this.lecturePath = lecturePath;
    }

    private String lecturePath = null;

    private String language = null;
    private String category = null;
    private String bluetoothCourse;
    private String bluetoothLecture;

    private String lectureIdentification = "";
    private String courseIdentification = "";

    private String version = "";
    private String authorUID = "";


    public LectureItem(String courseName, String lectureName) {
        this.courseName = courseName;
        this.lectureName = lectureName;
    }

    public LectureItem(String lectureName, String lectureIdentification, boolean online) {
        this.lectureIdentification = lectureIdentification;
        this.lectureName = lectureName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseName() {
        return courseName;
    }

    public File getThumbnail() {
        thumbnail = new File(lecturePath + "/meta" + "/thumbnail.jpg");
        return thumbnail;
    }

    public File getAudio() {
        audio = new File(lecturePath + "/meta" + "/audio.3gp");
        return audio;
    }


    public String getLectureName() {
        return lectureName;
    }

    public String getLecturePath() {
        return lecturePath;
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

    public String getCourseIdentification() {
        return courseIdentification;
    }

    public void setCourseIdentification(String courseIdentification) {
        this.courseIdentification = courseIdentification;
    }

    public String getLectureIdentification() {
        return lectureIdentification;
    }

    public void setLectureIdentification(String lectureIdentification) {
        this.lectureIdentification = lectureIdentification;
    }

    public void setPath(String lecturePath) {
        this.lecturePath = lecturePath;
    }

    public void setAuthorID(String uid) {
        this.authorUID = uid;
    }
    public String getAuthorUID() {
        return authorUID;
    }

    public void setBluetoothCourse(String bluetoothCourse) {
        this.bluetoothCourse = bluetoothCourse;
    }

    public void setBluetoothLecture(String bluetoothLecture) {
        this.bluetoothLecture = bluetoothLecture;
    }

    public String getBluetoothCourse() {
        return bluetoothCourse;

    }

    public String getBluetoothLecture() {
        return bluetoothLecture;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.courseName);
        dest.writeString(this.lectureName);
        dest.writeSerializable(this.thumbnail);
        dest.writeSerializable(this.audio);
        dest.writeString(this.lecturePath);
        dest.writeString(this.language);
        dest.writeString(this.category);
        dest.writeString(this.bluetoothCourse);
        dest.writeString(this.bluetoothLecture);
        dest.writeString(this.lectureIdentification);
        dest.writeString(this.courseIdentification);
        dest.writeString(this.authorUID);
    }

    protected LectureItem(Parcel in) {
        this.courseName = in.readString();
        this.lectureName = in.readString();
        this.thumbnail = (File) in.readSerializable();
        this.audio = (File) in.readSerializable();
        this.lecturePath = in.readString();
        this.language = in.readString();
        this.category = in.readString();
        this.bluetoothCourse = in.readString();
        this.bluetoothLecture = in.readString();
        this.lectureIdentification = in.readString();
        this.courseIdentification = in.readString();
        this.authorUID = in.readString();
    }

    public static final Parcelable.Creator<LectureItem> CREATOR = new Parcelable.Creator<LectureItem>() {
        @Override
        public LectureItem createFromParcel(Parcel source) {
            return new LectureItem(source);
        }

        @Override
        public LectureItem[] newArray(int size) {
            return new LectureItem[size];
        }
    };

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
