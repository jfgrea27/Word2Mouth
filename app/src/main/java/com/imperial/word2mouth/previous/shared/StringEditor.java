package com.imperial.word2mouth.previous.shared;

public class StringEditor {


    public static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }


    public static String[] splitString(String s) {
        String[] words = s.split("\\s+");
        for (int i = 0; i < words.length; i++) {
            // You may want to check for a non-word character before blindly
            // performing a replacement
            // It may also be necessary to adjust the character class
            words[i] = words[i].replaceAll("[^\\w]", "");
        }
        return words;
    }

    public static String[] lowerCase(String[] array) {
        String[] temp = array;

        for (int i = 0; i < array.length; i++) {
            temp[i] = array[i].toLowerCase();
        }
        return temp;
    }
}
