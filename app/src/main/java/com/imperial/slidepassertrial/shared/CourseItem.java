package com.imperial.slidepassertrial.shared;

import java.io.File;


public class CourseItem  {

    private String courseName = null;
    private File thumbnail = null;
    private File audio = null;
    private String coursePath = null;

    public CourseItem(String courseName, String path) {
        this.courseName = courseName;

        thumbnail = new File(path + "/meta" + "/thumbnail.jpg");

        audio = new File(path + "/meta" + "/audio.3gp");

        coursePath = path;
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
}
