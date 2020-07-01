package com.imperial.word2mouth.learn.main.online;

import com.imperial.word2mouth.shared.CourseItem;

import java.util.ArrayList;

public class Teacher {

    private ArrayList<CourseItem> courses = new ArrayList<>();



    private final String teacherName;

    public Teacher(String n) {
        teacherName = n;

    }

    public String getTeacherName() {
        return teacherName;
    }

    private void addCourse(CourseItem i) {
        courses.add(i);
    }
}
