package com.imperial.slidepassertrial.shared;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.imperial.slidepassertrial.DirectoryConstants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public class FileHandler {


    public static final int TITLE = 100;
    public static final int VIDEO = 101;
    public static final int INSTRUCTIONS = 102;
    public static final int AUDIO = 103;
    private static final int IMAGE = 104;


    public static File createDirectoryForCourseAndReturnIt(String courseName, Context context) {
        File file = new File(context.getExternalFilesDir(null), DirectoryConstants.offline + courseName);

        if (file.exists()) {
            int directoryNumber = 0;
            while (file.exists()) {
                directoryNumber++;
                file = new File(context.getExternalFilesDir(null), DirectoryConstants.offline + courseName + " (" + directoryNumber + ")");
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

    public static File createDirectoryForMetaData(String coursePath) {
        File file = new File(coursePath, "/" + "meta");
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
            case AUDIO:
                outputAddress += "/audio.3gp";
                return new File(outputAddress);
            case VIDEO:
                outputAddress += "/video.3gp";
                return copyVideoToFile(outputAddress, originalUriPath, content);
            case IMAGE:
                outputAddress += "/thumbnail.jpg";
                return copyVideoToFile(outputAddress, originalUriPath, content);
            default:
                return null;
        }
    }

    // Text

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

    // Video

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

    public static File copyFile(InputStream in, String desiredLocation) throws IOException {
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

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    public static void copyDirectoryOneLocationToAnotherLocation(File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            } else {
                boolean newDirectoryName = false;
                int counter = 1;
                while (!newDirectoryName) {
                    String newLocation = targetLocation.getPath() + " " + counter;
                    File newLocationFile = new File(newLocation);
                    if (newLocationFile.exists()) {
                        counter++;
                    } else {
                        targetLocation = newLocationFile;
                        newDirectoryName = true;
                    }
                }
                targetLocation.mkdirs();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {

                copyDirectoryOneLocationToAnotherLocation(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);

            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }

    }

    public static void addTree(File file, Collection<File> all) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                all.add(child);
                addTree(child, all);
            }
        }
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
