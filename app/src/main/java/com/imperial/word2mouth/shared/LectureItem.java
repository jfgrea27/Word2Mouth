package com.imperial.word2mouth.shared;

import java.io.File;

public class LectureItem {

    private String courseName = null;
    private String lectureName = null;
    private File thumbnail = null;
    private File audio = null;
    private String lecturePath = null;

    private String language = null;
    private String category = null;

    private String lectureIdentification = "";
    private String courseIdentification = "";

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
}
