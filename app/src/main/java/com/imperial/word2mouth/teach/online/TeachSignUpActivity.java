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
import com.google.firebase.auth.SignInMethodQueryResult;
import com.imperial.word2mouth.R;

public class TeachSignUpActivity extends AppCompatActivity {



    private EditText email;
    private EditText password;
    private ImageButton signUp;
    private ImageButton logout;

    private String emailText;
    private String passwordText;


    private FirebaseUser user;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private boolean signedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_sign_up);

        configureUI();

        checkAlreadyLoggedIn();
        configureSignUpButton();

        configureLogOutButton();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // UI

    private void configureUI() {
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signUp = findViewById(R.id.sign_up);
        logout = findViewById(R.id.logout_button);
    }



    private void updateUI() {
        if (signedIn) {
            signUp.setVisibility(View.INVISIBLE);
            logout.setVisibility(View.VISIBLE);
            email.setText(user.getEmail());
            email.setEnabled(false);
            password.setVisibility(View.INVISIBLE);
        } else {
            signUp.setVisibility(View.VISIBLE);
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

    ///////// Sign Up

    private void configureSignUpButton() {

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailText = email.getText().toString();
                passwordText = password.getText().toString();
                if (CredentialChecker.credentialValid(emailText, passwordText)) {
                    mAuth.createUserWithEmailAndPassword(emailText, passwordText).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                user = mAuth.getCurrentUser();
                                signedIn = true;
                                updateUI();
                            } else {
                                Toast.makeText(TeachSignUpActivity.this, "Account could not be created", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });
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
}