package com.imperial.word2mouth.teach;

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

import com.imperial.word2mouth.R;
import com.imperial.word2mouth.teach.online.account.TeachLoginActivity;
import com.imperial.word2mouth.teach.online.account.TeachSignUpActivity;
import com.imperial.word2mouth.teach.ui.main.SectionsPagerAdapter;

public class TeachActivityMain extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teach_main_tabbed);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        setToolBar();

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        tabs.getTabAt(0).setIcon(R.drawable.ic_offline_online_0);
        tabs.getTabAt(1).setIcon(R.drawable.ic_offline_online_1);
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
    }



    private void setToolBar() {
        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.teach_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Word 2 Mouth");
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_teach, menu);
        return true;
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
            case R.id.sign_up:
                Intent signUpIntent = new Intent(getApplicationContext(), TeachSignUpActivity.class);
                startActivity(signUpIntent);
                return true;
            case R.id.setting:
                Toast.makeText(this, "Setting - TO DO", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}