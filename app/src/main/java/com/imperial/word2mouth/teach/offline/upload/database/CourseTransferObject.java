package com.imperial.word2mouth.teach.offline.upload.database;

import com.imperial.word2mouth.shared.CourseItem;

public class CourseTransferObject {




    public final String type = "Course";

    private final String authorUID;
    private final String language;
    private final String category;
    public int followersCounter = 0;
    private String courseUID;

    private String bluetoothCourse;


    private final String courseName;
    public String low_Language;
    public String low_Category;
    public String low_CourseName;

    public CourseTransferObject(CourseItem courseItem) {
        this.authorUID = courseItem.getAuthorID();
        this.language = courseItem.getLanguage();
        this.category = courseItem.getCategory();
        this.courseName = courseItem.getCourseName();
        this.bluetoothCourse = courseItem.getBluetoothCourse();
    }

    public String getAuthorUID() {
        return authorUID;
    }


    public String getBluetoothCourse() {
        return bluetoothCourse;
    }

    public void setBluetoothCourse(String bluetoothCourse) {
        this.bluetoothCourse = bluetoothCourse;
    }

    public static String userNameRetrieving(String email) {
        String username = null;
        int indexAt = email.indexOf('@');

        if (indexAt != -1) {
            username = email.substring(0, indexAt);
        }
        return username;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCourseUID() {
        return courseUID;
    }

    public void setCourseUID(String courseUID) {
        this.courseUID = courseUID;
    }

    public String getLanguage() {
        return language;
    }

    public String getCategory() {
        return category;
    }


    public void setLowerCapital() {
        low_Language = language.toLowerCase();
        low_Category = category.toLowerCase();
        low_CourseName = courseName.toLowerCase();
    }
    public String getType() {
        return type;
    }

}
