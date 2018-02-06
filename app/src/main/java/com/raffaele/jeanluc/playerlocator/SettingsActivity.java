package com.raffaele.jeanluc.playerlocator;

/**
 * Created by Jean-Luc on 1/3/2018.
 */
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Switch;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.prefs.Preferences;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_PREF_EXAMPLE_SWITCH = "example switch";
    ConnectionClass connectionClass;
    SharedPreferences sharedPref;
    SharedPreferences sharedPref2;


    private boolean PreferencesChanged = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        connectionClass = new ConnectionClass();
        sharedPref = getSharedPreferences("PlayerLocator", MODE_PRIVATE);
        sharedPref2 = PreferenceManager.getDefaultSharedPreferences(this);


        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        //getActionBar().setTitle("Settings");

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            //when back arrow is clicked
            case android.R.id.home:
                //Sync preferences to database if any are changed
                if (PreferencesChanged) {

                    DoPreferenceUpdate doPreferenceUpdate = new DoPreferenceUpdate();
                    doPreferenceUpdate.execute("");
                }


                NavUtils.navigateUpFromSameTask(this);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        PreferencesChanged = true;
    }

    public class DoPreferenceUpdate extends AsyncTask<String, String, String>
    {


        String z = "";
        Boolean isSuccess = false;
        String userid = "";

        @Override
        protected String doInBackground(String... params)
        {

            try
            {
                Connection conn = connectionClass.CONN();

                if (conn == null)
                    z = "could not update preferences";

                else
                {

                    userid = sharedPref.getString("username", "UNKNOWN");
                    Integer skill = Integer.parseInt(sharedPref2.getString("skill_preference", ""));

                    String query = "UPDATE UserInfo SET skill=" + skill
                            + " WHERE id = (SELECT id FROM Users WHERE username = '"
                            + userid + "')"
                            + " SELECT * FROM UserInfo";


                    Log.d("preftest", query);




                    Statement stmnt = conn.createStatement();
                    ResultSet rs = stmnt.executeQuery(query);

                    Log.d("preftest", "madeithere2");
                    z = "update successful";
                    isSuccess = true;
                }
            }

            catch(Exception ex)
            {
                isSuccess = false;
                Log.d("preftest", "error exception");
                z = "Exceptions";
            }
            return z;
        }

    }
}
