package com.imperial.slidepassertrial.shared;

import java.io.File;


public class CourseItem  {

    private String courseName = null;
    private File thumbnail = null;
    private File audio = null;

    public CourseItem(String courseName, String coursePath) {
        this.courseName = courseName;

        thumbnail = new File(coursePath + "/meta" + "/thumbnail.jpg");

        audio = new File(coursePath + "/meta" + "/audio.3gp");
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
}
