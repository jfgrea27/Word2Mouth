package com.imperial.word2mouth.teach.online.account;

import android.util.Patterns;

public class CredentialChecker {

    public static boolean credentialValid(String email, String password) {
        if (isEmail(email) && isStrongPassword(password)) {
            return true;
        } else {
            return false;
        }
    }

    private static boolean isStrongPassword(String passwordText) {
        // TODO
        return true;
    }


    private static boolean isEmail(String emailText) {
        return Patterns.EMAIL_ADDRESS.matcher(emailText).matches();
    }
}
