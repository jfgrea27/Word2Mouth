package com.imperial.slidepassertrial.teach.offline;

import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DirectoryHandler {


    public static final int TITLE = 100;
    public static final int VIDEO = 101;
    public static final int INSTRUCTIONS = 102;
    public static final int AUDIO = 103;

    public static File createDirectoryForCourseAndReturnIt(String courseName, Context context) {
        File file = new File(context.getExternalFilesDir(null), courseName);

        if (file.exists()) {
            int directoryNumber = 0;
            while (file.exists()) {
                directoryNumber++;
                file = new File(context.getExternalFilesDir(null), "/" + courseName + " (" + directoryNumber + ")");
            }
        }
        file.mkdirs();
        return file;
    }

    public static File createDirectoryForSlideAndReturnIt(String coursePath, int slideNumber) {
        File file = new File(coursePath, "/" + slideNumber);
        file.mkdirs();
        return file;
    }

    public static File retrieveSlideDirectoryByNumber(String coursePath, int slideNumber) {
        File slideDirectory = new File(coursePath, "/" + slideNumber);
        if (slideDirectory.exists()) {
            return slideDirectory;
        } else {
            return null;
        }
    }

    public static File createFileForSlideContentAndReturnIt(String slidePath, @Nullable Uri originalUriPath, ContentResolver content, @Nullable String script, int requestCode) {
        String outputAddress = slidePath;
        switch (requestCode) {
            case TITLE:
                outputAddress += "/title.txt";
                return copyTextToFile(outputAddress, script);
            case INSTRUCTIONS:
                outputAddress += "/instructions.txt";
                return copyTextToFile(outputAddress, script);

            case VIDEO:
                outputAddress += "/video.3gp";
                return copyVideoToFile(outputAddress, originalUriPath, content);
            default:
                return null;
        }
    }



    // Video
    private static File copyTextToFile(String outputPath, String script) {
        File textFile = new File(outputPath);
        try {
            FileWriter writer = new FileWriter(textFile);
            writer.write(script);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textFile;
    }

    private static File copyVideoToFile(String outputAddress, Uri originalUri, ContentResolver content) {
        InputStream in = null;
        try {
            in = content.openInputStream(originalUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        try {
            return copyFile(in, outputAddress);
        } catch (IOException e) {
            e.printStackTrace();
            return  null;
        }
    }

    private static File copyFile(InputStream in, String desiredLocation) throws IOException {
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

}
