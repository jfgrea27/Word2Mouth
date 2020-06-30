package com.imperial.word2mouth.shared;

import java.io.File;


public class CourseItem  {

    private String courseName = null;
    private File thumbnail = null;
    private File audio = null;
    private String coursePath = null;
    private String courseOnlineIdentification = null;

    // For offline purposes
    public CourseItem(String courseName, String path) {
        this.courseName = courseName;

        thumbnail = new File(path + "/meta" + "/thumbnail.jpg");

        audio = new File(path + "/meta" + "/audio.3gp");

        coursePath = path;
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
}
