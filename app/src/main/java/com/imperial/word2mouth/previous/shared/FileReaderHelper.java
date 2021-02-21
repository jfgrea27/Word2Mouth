package com.imperial.word2mouth.previous.shared;

import android.content.Context;
import android.util.Log;

import com.imperial.word2mouth.helpers.FileSystemConstants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileReaderHelper {


    public static String readTextFromFile(String path) {
        String ret = "";

        try {
            File file = new File(path);
            FileInputStream inputStream = new FileInputStream(file);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        if (!ret.isEmpty()) {
            if (ret.charAt(0) == '\n' && ret.length() > 1) {
                ret = ret.substring(1);
            }
        }
        return ret;
    }


    public static boolean removesAnyLineMatchingPatternInFile(Context c, File f, String pattern) throws IOException {
        File inputFile = new File(f.getPath());
        File tempFile = new File(c.getExternalFilesDir(null) + FileSystemConstants.cache + "temp");
        tempFile.createNewFile();

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

        String lineToRemove = pattern;
        String currentLine;

        while((currentLine = reader.readLine()) != null) {
            // trim newline when comparing with lineToRemove
            String trimmedLine = currentLine.trim();
            if(trimmedLine.equals(lineToRemove)) continue;
            writer.write(currentLine + System.getProperty("line.separator"));
        }
        writer.close();
        reader.close();
        boolean successful = tempFile.renameTo(inputFile);
        return successful;
    }
}
