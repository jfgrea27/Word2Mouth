package com.imperial.word2mouth.learn.main.online;

import com.imperial.word2mouth.shared.CourseItem;

import java.util.ArrayList;

public class Teacher {

    private ArrayList<CourseItem> courses = new ArrayList<>();



    private final String teacherEmail;

    public Teacher(String email) {
        teacherEmail = email;

    }

    public String getTeacherName() {
        return teacherEmail;
    }

}
