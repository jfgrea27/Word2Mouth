package com.imperial.word2mouth.teach.offline.upload.database;

public class DataTransferObject {

    private final String userEmail;
    private String fileKey;

    private final String userName;



    private final String courseName;

    private String courseURL;

    public DataTransferObject(String email, String name) {
        userEmail = email;
        courseName = name;
        userName = userNameRetrieving(email);
    }


    public String getUserName() {
        return userName;
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

    public String getKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getCourseURL() {
        return courseURL;
    }

    public void setCourseURL(String courseURL) {
        this.courseURL = courseURL;
    }


}
