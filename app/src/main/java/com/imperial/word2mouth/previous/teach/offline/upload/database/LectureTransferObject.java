package com.imperial.word2mouth.previous.teach.offline.upload.database;

import com.imperial.word2mouth.previous.shared.PrevLectureItem;

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


    public LectureTransferObject(PrevLectureItem prevLectureItem) {
        this.authorUID = prevLectureItem.getAuthorUID();
        this.courseName = prevLectureItem.getCourseName();
        this.language = prevLectureItem.getLanguage();
        this.category = prevLectureItem.getCategory();
        this.lectureName = prevLectureItem.getLectureName();
        this.lectureUID = prevLectureItem.getLectureIdentification();
        this.courseUID = prevLectureItem.getCourseIdentification();
        bluetoothCourse = prevLectureItem.getBluetoothCourse();
        bluetoothLecture = prevLectureItem.getBluetoothLecture();

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
