package com.imperial.word2mouth.helpers;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.imperial.word2mouth.model.CourseItem;
import com.imperial.word2mouth.model.LectureItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

import static com.imperial.word2mouth.helpers.JSONHelper.getJSONFromString;

public class FileSystemHelper {


    ////////////////////////////////////////////////////////////////////////////////////////////
    // Create Course File System
    ////////////////////////////////////////////////////////////////////////////////////////////
    public static CourseItem createCourseFileSystem(CourseItem courseItem, Context context) {
        // Create Course root folder
        String courseRootPath = FileSystemConstants.offline + courseItem.getUuidCourse().toString();
        File courseRootFile = createFolder(context.getExternalFilesDir(null),courseRootPath);
        courseItem.setCourseRootPath(courseRootFile.getAbsolutePath());
        // Create Lecture Folder
        File lectureFolderFile = createFolder(courseRootFile, FileSystemConstants.lectures);
        courseItem.setCourseLecturePath(lectureFolderFile.getAbsolutePath());
        // Creating JSON meta file
        File courseMetaDataFile = createFile(courseRootFile, FileSystemConstants.metaFile);
        courseItem.setCourseJSONMetaDataPath(courseMetaDataFile.getAbsolutePath());
        // Write to JSON meta file
        String jsonMetaData = JSONHelper.prepareMetadataCourse(courseItem).toString();
        writeTextToFile(courseMetaDataFile, jsonMetaData);
        // Create Image Thumbnail file
        File courseImageThumbnailFile = createFile(courseRootFile, FileSystemConstants.photoThumbnail);
        courseItem.setCourseImageThumbnailPath(courseImageThumbnailFile.getPath());
        // Create Audio Thumbnail file
        File courseAudioThumbnailFile = createFile(courseRootFile, FileSystemConstants.audioThumbnail);
        courseItem.setCourseAudioThumbnailPath(courseAudioThumbnailFile.getPath());
        return courseItem;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Create Lecture File System
    ///////////////////////////////////////////////////////////////////////////////////////////////
    public static LectureItem createLectureFileSystem(LectureItem lectureItem, Context context) {
        File lectureDir = new File(lectureItem.getCourseItem().getCourseLecturePath());
        // Create Lecture root folder
        String lectureRootPath = lectureItem.getUuidLecture().toString();
        File lectureRootFile = createFolder(lectureDir, lectureRootPath);
        lectureItem.setLecturePath(lectureRootFile.getAbsolutePath());
        // Create Slide Folder
        File slideFolderFile = createFolder(lectureRootFile, FileSystemConstants.slides);
        lectureItem.setSlidePath(slideFolderFile.getAbsolutePath());
        // Creating JSON meta file
        File lectureMetaDataFile = createFile(lectureRootFile, FileSystemConstants.metaFile);
        // Write to JSON meta file
        String jsonMetaData = JSONHelper.prepareMetaDataLectureCreation(lectureItem).toString();
        writeTextToFile(lectureMetaDataFile, jsonMetaData);
        // Create Image Thumbnail file
        File courseImageThumbnailFile = createFile(lectureRootFile, FileSystemConstants.photoThumbnail);
        lectureItem.setLectureImageThumbnailPath(courseImageThumbnailFile.getPath());
        // Create Audio Thumbnail file
        File courseAudioThumbnailFile = createFile(lectureRootFile, FileSystemConstants.audioThumbnail);
        lectureItem.setLectureAudioThumbnailPath(courseAudioThumbnailFile.getPath());
        return lectureItem;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // GET JSON
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public static ArrayList<JSONObject> getJSON(String parentStringDirectory, Context context) throws JSONException {
        File parentDirectory = new File(context.getExternalFilesDir(null), parentStringDirectory);
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        File[] files = parentDirectory.listFiles();
        for (File file: files) {
            File jsonFile = new File(file, FileSystemConstants.metaFile);
            JSONObject courseJSON = getJSONFromString(readFile(jsonFile));
            jsonObjects.add(courseJSON);
        }
        return jsonObjects;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Helpers
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private static File createFolder(File parent, String child) {
        File file = new File(parent, child);

        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    private static File createFile(File parent, String child) {
        File file = new File(parent, child);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private static void writeTextToFile(File file, String script) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(script);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Reading file
    private static String readFile(File file) {
        String output = "";
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        scanner.useDelimiter(" ");
        while(scanner.hasNext()){
            output += scanner.next();
        }
        scanner.close();
        return output;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Copying file
    public static File saveImageVideoFile(String outputAddress, Uri originalUri, ContentResolver content) {
        InputStream in = null;
        try {
            in = content.openInputStream(originalUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        try {
            return saveData(in, outputAddress);
        } catch (IOException e) {
            e.printStackTrace();
            return  null;
        }
    }

    public static void saveImageVideoFile(String outputAddress, Bitmap bitmap) {
        try {
            FileOutputStream out = new FileOutputStream(outputAddress);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private static File saveData(InputStream in, String desiredLocation) throws IOException {
        File outputFile = new File(desiredLocation);

        FileOutputStream fos = new FileOutputStream(outputFile);

        copyStream(in, fos);

        return outputFile;
    }

    private static void copyStream(InputStream input, OutputStream output)
            throws IOException {

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    public static void saveAudioFile(String outputAddress, Uri audioUri, ContentResolver contentResolver) {
        // TODO Figure this out
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Deleting file
    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

}
