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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.teach.offline.create.video.ImageDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeachLoginActivity extends AppCompatActivity implements ImageDialog.OnInputListener  {

    // Permission
    private final int CAMERA_PERMISSION = 1;

    private boolean hasCameraPermission = false;


    // Camera Selection
    public final int CAMERA_ROLL_SELECTION = 0;
    public final int GALLERY_SELECTION = 1;



    // Sign in Code
    private static final int SIGN_IN = 10;


    // UI
    private ImageButton login;
    private ImageButton logout;
    private ImageButton thumbnail;
    private TextView welcome;
    private TextView userName;

    // Firebase
    private FirebaseUser user;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // Model
    private boolean signedIn = false;
    private Bitmap imageBitmap = null;
    private Uri imageUri = null;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    ///////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_login);

        configureUI();

        // Permissions
        getPermissions();


        checkAlreadyLoggedIn();

        // Login Button
        configureLogInButton();
        configureLogOutButton();

        configureImageButton();
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
        login = findViewById(R.id.login_button);
        logout = findViewById(R.id.logout_button);
        thumbnail = findViewById(R.id.thumbnail);

        welcome = findViewById(R.id.welcomeMessage);
        userName = findViewById(R.id.userName);

    }

    private void updateUI() {
        if (signedIn) {
            login.setVisibility(View.INVISIBLE);
            logout.setVisibility(View.VISIBLE);
            setProfilePicture();
            setWelcome(true);
        } else {
            login.setVisibility(View.VISIBLE);
            logout.setVisibility(View.INVISIBLE);
            thumbnail.setImageResource(R.drawable.ic_account);
            setWelcome(false);
        }

    }

    private void setWelcome(boolean b) {
        if (b) {
            String temp = user.getDisplayName();
            if (temp .isEmpty() || temp.equals("")) {
                temp = user.getEmail();
            }

            userName.setText(temp);
            userName.setVisibility(View.VISIBLE);
            welcome.setVisibility(View.VISIBLE);
        } else {
            userName.setText("");
            userName.setVisibility(View.INVISIBLE);
            welcome.setVisibility(View.INVISIBLE);
        }
    }


    private void setProfilePicture() {
        if (user != null) {
            StorageReference ref = FirebaseStorage.getInstance().getReference("/users/" + user.getUid() + "/profilePicture/pp.jpg");
            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(getApplicationContext()).load(uri).into(thumbnail);
                }
            });
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

                List<AuthUI.IdpConfig> providers = Arrays.asList(
                        new AuthUI.IdpConfig.EmailBuilder().build());

                startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder().setAvailableProviders(providers).build(), SIGN_IN);
            }
        });

    }

    private void storeEmail() {
        if (user != null) {
            Map<String, String> teacher = new HashMap<>();
            teacher.put("UID", user.getUid());
            teacher.put("email", user.getEmail());
            teacher.put("name", user.getDisplayName());

            db.collection("users").document(user.getUid()).set(teacher).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(TeachLoginActivity.this, "Details saved", Toast.LENGTH_SHORT).show();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(TeachLoginActivity.this, "Details not saved", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////


    private void configureImageButton() {
        thumbnail = findViewById(R.id.thumbnail);

        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasCameraPermission) {
                    if (signedIn) {
                        ImageDialog imageDialog = new ImageDialog(ImageDialog.PROFILE);
                        imageDialog.show(getSupportFragmentManager(), "Profile Picture Selection");
                    } else {
                        Toast.makeText(TeachLoginActivity.this, "Need to be signed in", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(TeachLoginActivity.this, "Need the Camera Permission", Toast.LENGTH_SHORT).show();
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


    private void updateImageUI(Uri uri) {
        if (signedIn) {
            Glide.with(getApplicationContext()).load(uri).into(thumbnail);
        }
    }



    private void addImageToUser(int selection) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            switch (selection) {
                case GALLERY_SELECTION:
                    if (imageUri != null) {
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("/users/" + user.getUid() + "/profilePicture/pp.jpg");
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


                        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("/users/" + user.getUid() + "/profilePicture/pp.jpg");
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





    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SIGN_IN:
                    IdpResponse response = IdpResponse.fromResultIntent(data);
                    user = FirebaseAuth.getInstance().getCurrentUser();

                    // Store the UID and email in 'users'
                    storeEmail();
                    signedIn = true;
                    updateUI();
                    break;
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