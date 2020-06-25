package com.imperial.word2mouth.learn.main;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.imperial.word2mouth.shared.DirectoryConstants;
import com.imperial.word2mouth.R;
import com.imperial.word2mouth.learn.main.ui.SectionsPagerAdapter;
import com.imperial.word2mouth.teach.TeachActivityMain;

import java.io.File;

public class LearnActivityMain extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_main_tabbed);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);


        setToolBar();

        fileManagement();

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        tabs.getTabAt(0).setIcon(R.drawable.ic_offline_online_0);
        tabs.getTabAt(1).setIcon(R.drawable.ic_offline_online_1);
    }

    private void fileManagement() {
        File f = new File(getExternalFilesDir(null).getPath() + DirectoryConstants.zip);
        if (!f.exists()) {
            f.mkdirs();
        }

        f = new File(getExternalFilesDir(null).getPath() + DirectoryConstants.offline);
        if (!f.exists()) {
            f.mkdirs();
        }

        f = new File(getExternalFilesDir(null).getPath() + DirectoryConstants.online);
        if (!f.exists()) {
            f.mkdirs();
        }
    }


    private void setToolBar() {
        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.learn_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Word 2 Mouth");
    }


    @Override
    protected void onRestart() {
        super.onRestart();
       recreate();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_learn, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.teach:
                Intent intent = new Intent(LearnActivityMain.this, TeachActivityMain.class);
                startActivity(intent);
                return true;
            case R.id.setting:
                Toast.makeText(this, "Setting - TO DO", Toast.LENGTH_SHORT).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}