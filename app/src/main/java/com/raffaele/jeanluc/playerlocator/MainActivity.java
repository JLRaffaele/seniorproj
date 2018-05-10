package com.raffaele.jeanluc.playerlocator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;



public class MainActivity extends AppCompatActivity {

    String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        //logged in users' name
        username = getSharedPreferences("PlayerLocator", MODE_PRIVATE).getString("username", "UNKNOWN");

        //Name of any viewable profile
        Intent i = getIntent();
        String profile_name = i.getExtras().getString("profile_name");


        TextView profile_name_text = (TextView)findViewById(R.id.main_activity_username);
        profile_name_text.setText(profile_name);

        Button btn = (Button)findViewById(R.id.main_activity_button);
        Button backbtn = (Button)findViewById(R.id.main_activity_back_button);

        //display map button if we are on our own profile -- else display challenge button
        if(username.equals(profile_name))
        {
            btn.setText("View the map!");
            backbtn.setVisibility(View.GONE);
        }
        else
            btn.setText("Challenge " + profile_name + "!");

        btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(i);
            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle app bar item clicks here. The app bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId())
        {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_logout:
                finish();
                break;
            case R.id.action_my_profile:
                Intent i = getIntent();
                i.putExtra("profile_name", username);
                finish();
                startActivity(i);
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {

    }
}
