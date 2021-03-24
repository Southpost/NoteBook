package com.example.notebook;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setNightMode();
    }

    public void setNightMode(){
         setTheme(R.style.DayTheme);

    }
    public boolean isNightMode(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return sharedPreferences.getBoolean("nightMode", false);
    }


}
