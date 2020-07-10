package com.imperial.word2mouth.shared;

public class StringEditor {


    public static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }
}
