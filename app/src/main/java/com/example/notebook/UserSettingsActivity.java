package com.example.notebook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;


import androidx.appcompat.widget.Toolbar;

public class UserSettingsActivity extends BaseActivity  {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usersetting_layout);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Intent intent = getIntent();
        initView();

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void initView() {

    }
}