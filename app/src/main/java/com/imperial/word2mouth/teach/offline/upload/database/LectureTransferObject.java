package com.imperial.word2mouth.teach.offline.upload.database;

import com.imperial.word2mouth.shared.LectureItem;

public class LectureTransferObject {

    public final String type = "Lecture";
    public final String authorUID;
    public final String courseName;
    public final String language;
    public final String category;
    public final String lectureName;
    public int downloadCounter = 0;
    public String versionUID = null;
    public  String lectureUID = "";

    public String getBluetoothCourse() {
        return bluetoothCourse;
    }

    public void setBluetoothCourse(String bluetoothCourse) {
        this.bluetoothCourse = bluetoothCourse;
    }

    public String bluetoothCourse;

    public String getBluetoothLecture() {
        return bluetoothLecture;
    }

    public void setBluetoothLecture(String bluetoothLecture) {
        this.bluetoothLecture = bluetoothLecture;
    }

    public String bluetoothLecture;

    private String courseUID = "";


    public LectureTransferObject(LectureItem lectureItem) {
        this.authorUID = lectureItem.getAuthorUID();
        this.courseName = lectureItem.getCourseName();
        this.language = lectureItem.getLanguage();
        this.category = lectureItem.getCategory();
        this.lectureName = lectureItem.getLectureName();
        this.lectureUID = lectureItem.getLectureIdentification();
        this.courseUID = lectureItem.getCourseIdentification();
        bluetoothCourse = lectureItem.getBluetoothCourse();
        bluetoothLecture = lectureItem.getBluetoothLecture();

    }

    public String getCourseUID() {
        return courseUID;
    }
    public void setCourseUID(String courseUID) {
        this.courseUID = courseUID;
    }


    public String getLectureUID() {
        return lectureUID;
    }

    public void setLectureUID(String lectureUID) {
        this.lectureUID = lectureUID;
    }

    public String getType() {
        return type;
    }


}
