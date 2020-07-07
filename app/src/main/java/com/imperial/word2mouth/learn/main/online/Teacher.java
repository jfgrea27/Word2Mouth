package com.imperial.word2mouth.learn.main.online;

import com.imperial.word2mouth.shared.CourseItem;

import java.util.ArrayList;

public class Teacher {



    private  String UID;
    private final String teacherEmail;
    private String name;

    public Teacher(String email) {
        teacherEmail = email;
    }
    
    public Teacher(String email, String name, String UID) {
        teacherEmail = email;
        this.name = name;
        this.UID = UID;
        
    }

    public String getTeacherEmail() {
        return teacherEmail;
    }

    public String getUID() {
        return UID;
    }

    public String getName() {
        return name;
    }

    public String getUserName() {
        String temp = null;
        if (teacherEmail != null) {
            int iend = teacherEmail.indexOf("@");
            if (iend > -1) {
                temp = teacherEmail.substring(0, iend);
            }
        }
        return temp;
    }

}
