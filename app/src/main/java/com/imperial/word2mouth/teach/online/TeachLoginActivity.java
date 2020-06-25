package com.imperial.word2mouth.teach.online;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.imperial.word2mouth.R;

public class TeachLoginActivity extends AppCompatActivity {

    private static final int IN = 0;
    private static final int OUT = 1;

    private EditText email;
    private EditText password;
    private ImageButton login;
    private ImageButton logout;

    private String emailText;
    private String passwordText;

    private FirebaseUser user;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private boolean validCredential = false;
    private boolean signedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_login);

        configureUI();


        checkAlreadyLoggedIn();

        // Login Button
        configureLogInButton();

        configureLogOutButton();
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////

    // UI
    private void configureUI() {
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login_button);
        logout = findViewById(R.id.logout_button);

    }

    private void updateUI() {
        if (signedIn) {
            login.setVisibility(View.INVISIBLE);
            logout.setVisibility(View.VISIBLE);
            email.setText(user.getEmail());
            email.setEnabled(false);
            password.setVisibility(View.INVISIBLE);
        } else {
            login.setVisibility(View.VISIBLE);
            logout.setVisibility(View.INVISIBLE);
            email.clearComposingText();
            email.setEnabled(true);
            password.setVisibility(View.VISIBLE);
        }

    }



    ///////////////////////////////////////////////////////////////////////////////////////////////


    // Authentication

    private void checkAlreadyLoggedIn() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            signedIn = true;
            updateUI();
        }
    }



    /////////// Log Out Button
    private void configureLogOutButton() {
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (signedIn) {
                    mAuth.signOut();
                    signedIn = false;
                    updateUI();
                }

            }
        });
    }

    /////////// Log In Button

    private void configureLogInButton() {

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailText = email.getText().toString();
                passwordText = password.getText().toString();

                if (CredentialChecker.credentialValid(emailText, passwordText)) {
                    mAuth.signInWithEmailAndPassword(emailText, passwordText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                signedIn = true;
                                user = mAuth.getCurrentUser();
                                updateUI();
                            } else {
                                Toast.makeText(TeachLoginActivity.this, "Could not Login", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(TeachLoginActivity.this, "Credentials are Invalid", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}