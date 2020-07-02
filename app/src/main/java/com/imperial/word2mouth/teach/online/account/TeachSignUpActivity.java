package com.imperial.word2mouth.teach.online.account;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.teach.offline.create.video.ImageDialog;
import com.imperial.word2mouth.teach.offline.upload.database.DataTransferObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TeachSignUpActivity extends AppCompatActivity implements ImageDialog.OnInputListener  {


    // Permission
    private final int CAMERA_PERMISSION = 1;

    // Camera Selection
    public final int CAMERA_ROLL_SELECTION = 0;
    public final int GALLERY_SELECTION = 1;

    private EditText email;
    private EditText password;
    private ImageButton signUp;
    private ImageButton logout;
    private ImageButton thumbnail;

    private String emailText;
    private String passwordText;

    private boolean hasCameraPermission = false;


    private FirebaseUser user;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private boolean signedIn = false;
    private Bitmap imageBitmap = null;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_sign_up);


        // Permissions
        getPermissions();

        configureUI();

        checkAlreadyLoggedIn();
        configureSignUpButton();
        configureImageButton();
        configureLogOutButton();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Permission

    private void getPermissions() {
        if (!(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "Please allow access to Camera", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        } else{
            hasCameraPermission = true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION) {
            if(permissions[0].equals(Manifest.permission.CAMERA) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasCameraPermission = true;
            }
        }
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
            setProfilePicture();
        } else {
            signUp.setVisibility(View.VISIBLE);
            logout.setVisibility(View.INVISIBLE);
            email.clearComposingText();
            email.setEnabled(true);
            password.setVisibility(View.VISIBLE);
            thumbnail.setImageResource(R.drawable.ic_account);
        }

    }

    private void setProfilePicture() {
        if (user != null) {
            StorageReference ref = FirebaseStorage.getInstance().getReference(DataTransferObject.userNameRetrieving(user.getEmail()) + "/profilePicture/" + "pp.jpg");
            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getApplicationContext()).load(uri).into(thumbnail);
                }
            });
        }
    }

    private void updateImageUI(Uri uri) {
      if (signedIn) {
          Glide.with(getApplicationContext()).load(uri).into(thumbnail);
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

    private void addImageToUser(int selection) {
        if (user != null) {

            switch (selection) {
                case GALLERY_SELECTION:
                    if (imageUri != null) {
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference(DataTransferObject.userNameRetrieving(user.getEmail())).child("/profilePicture/").child("pp.jpg");
                        storageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        updateImageUI(uri);
                                    }
                                });
                            }
                        });
                    }

                    break;

                case CAMERA_ROLL_SELECTION:
                    if (imageBitmap != null) {
                        File f = new File(getBaseContext().getExternalFilesDir(null) + DirectoryConstants.zip + "pp.jpg");
                        if (!f.exists()) {
                            try {
                                f.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }


                        try (FileOutputStream out = new FileOutputStream(f)) {
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        StorageReference storageRef = FirebaseStorage.getInstance().getReference(DataTransferObject.userNameRetrieving(user.getEmail())).child("/profilePicture/").child("pp.jpg");
                        storageRef.putFile(Uri.fromFile(f)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        updateImageUI(uri);
                                        f.delete();
                                    }
                                });
                            }
                        });
                    }
                    break;
            }

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

    ////////////////////////////////////////////////////////////////////////////////////////////////


    private void configureImageButton() {
        thumbnail = findViewById(R.id.thumbnail);

        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasCameraPermission) {
                    ImageDialog imageDialog = new ImageDialog();
                    imageDialog.show(getSupportFragmentManager(), "Profile Picture Selection");
                } else {
                    Toast.makeText(TeachSignUpActivity.this, "Need the Camera Permission", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    @Override
    public void sendInput(int choice) {
        switch (choice) {
            case GALLERY_SELECTION: {
                Toast.makeText(this, "Opening Galleries", Toast.LENGTH_SHORT).show();
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"), GALLERY_SELECTION);
                break;
            }
            case CAMERA_ROLL_SELECTION: {
                Toast.makeText(this, "Opening Camera Roll", Toast.LENGTH_SHORT).show();
                Intent rollIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                rollIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
                rollIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
                rollIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                startActivityForResult(rollIntent, CAMERA_ROLL_SELECTION);
                break;
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case CAMERA_ROLL_SELECTION:
                    imageBitmap = (Bitmap) data.getExtras().get("data");
                    addImageToUser(CAMERA_ROLL_SELECTION);
                    break;
                case GALLERY_SELECTION:
                    imageUri = data.getData();
                    addImageToUser(GALLERY_SELECTION);
                    break;
            }

        }
    }

}