package com.imperial.word2mouth.teach.offline.upload.database;

public class DataTransferObject {



    private final String userUID;
    private final String language;
    private final String category;
    private String fileKey;


    private final String courseName;
    public String low_Language;
    public String low_Category;
    public String low_CourseName;

    public DataTransferObject(String userUID, String name, String language, String category) {
        this.userUID = userUID;
        courseName = name;
        this.language = language;
        this.category = category;
    }

    public String getUserUID() {
        return userUID;
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
}
