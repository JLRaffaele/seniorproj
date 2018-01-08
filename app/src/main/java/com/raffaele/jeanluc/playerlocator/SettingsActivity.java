package com.raffaele.jeanluc.playerlocator;

/**
 * Created by Jean-Luc on 1/3/2018.
 */
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    public static final String KEY_PREF_EXAMPLE_SWITCH = "example switch";
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // setting default screen to login.xml
        //setContentView(R.layout.activity_settings);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
        //getActionBar().setTitle("Settings");
    }
}
