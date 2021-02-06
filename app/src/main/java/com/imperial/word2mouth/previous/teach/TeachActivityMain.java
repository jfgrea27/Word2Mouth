package com.imperial.word2mouth.previous.teach;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.Word2Mouth;
import com.imperial.word2mouth.previous.background.ConnectivityReceiver;
import com.imperial.word2mouth.previous.teach.online.account.TeachLoginActivity;
import com.imperial.word2mouth.previous.teach.ui.main.SectionsPagerAdapter;

import java.util.Locale;

public class TeachActivityMain extends AppCompatActivity implements ConnectivityReceiver.ConnectivityReceiverListener  {


    private TextToSpeech textToSpeech;
    private TabLayout tabs;
    private Toolbar toolbar;
    private Menu menu;
    private FirebaseUser user;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_main_tabbed);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        setToolBar();

        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        tabs.getTabAt(0).setIcon(R.drawable.ic_teach_offline_online_0);
        tabs.getTabAt(1).setIcon(R.drawable.ic_teach_offline_online_1);


        configureSignInDialog();
        configureTextToSpeech();
        configureOnLongClicks();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void configureSignInDialog() {
        if (isNetworkConnected()) {
            user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                SignInDialog signInDialog = new SignInDialog();
                signInDialog.show(getSupportFragmentManager(), "signInDialog");
            }

        }
    }


    private void configureOnLongClicks() {
        toolbar.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.teacher));
                return true;
            }
        });


        tabs.getTabAt(0).view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.onDevice));
                return false;
            }
        });

        tabs.getTabAt(1).view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.onInternet));
                return false;
            }
        });


    }



    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
    }



    private void setToolBar() {
        toolbar = findViewById(R.id.teach_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.teacher));
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        this.menu = menu;
        inflater.inflate(R.menu.menu_teach, menu);
//        configureMenuOnLongClicks();
        return true;
    }

    private void configureMenuOnLongClicks() {
        menu.getItem(0).getActionView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.learner));
                return false;
            }
        });

        menu.getItem(1).getActionView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                speak(getString(R.string.login));
                return false;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.learn_menu:
                finish();
                return true;
            case R.id.login:
                Intent loginIntent = new Intent(getApplicationContext(), TeachLoginActivity.class);
                startActivity(loginIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register connection status listener
        Word2Mouth.activityResumed();
        Word2Mouth.getInstance().setConnectivityListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Word2Mouth.activityPaused();
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }


/////////////////////////////////////////////////////////////////////////////////////////////


    public void speak(String string) {
        textToSpeech.speak(string, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void configureTextToSpeech() {
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.getDefault());

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(TeachActivityMain.this, R.string.languageNotSupported, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(TeachActivityMain.this, R.string.initializationFailedSST, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////


    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if(isConnected){
            user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                // Sign in dialog
                SignInDialog signInDialog = new SignInDialog();
                signInDialog.show(getSupportFragmentManager(), "signInDialog");
            }
        } else {
            Toast.makeText(TeachActivityMain.this,
                    getString(R.string.internetNotConnected), Toast.LENGTH_SHORT).show();
        }
    }


}