package com.raffaele.jeanluc.playerlocator;

/**
 * Created by Jean-Luc on 1/3/2018.
 */
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;



import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.Statement;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    ConnectionClass connectionClass;
    SharedPreferences userFile;
    SharedPreferences sharedPref;
    Boolean validPref = true;
    private boolean PreferencesChanged = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        connectionClass = new ConnectionClass();

        userFile = getSharedPreferences("PlayerLocator", MODE_PRIVATE);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);


        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

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
                    if (validPref) {
                        DoPreferenceUpdate doPreferenceUpdate = new DoPreferenceUpdate();
                        doPreferenceUpdate.execute("");

                        //don't leave settings until they are valid

                        NavUtils.navigateUpFromSameTask(this);
                    }
                    else
                        Toast.makeText(SettingsActivity.this, "Invalid settings", Toast.LENGTH_SHORT).show();
                }
                else
                    NavUtils.navigateUpFromSameTask(this);


                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        Log.d("settings_debug", "pref changed");

        PreferencesChanged = true;
        if ("zipcode_preference".equals(key))
        {
            String val = sharedPreferences.getString("zipcode_preference", "");
            if (val.length() != 5)
            {
                validPref = false;
                Toast.makeText(SettingsActivity.this, "Invalid Zipcode", Toast.LENGTH_SHORT).show();
            }
            else
                validPref = true;
        }

        if ("bio_preference".equals(key) || "contact_preference".equals(key))
        {
            String val = sharedPreferences.getString(key, "");
            if (val.length() > 250)
            {
                validPref = false;
                Toast.makeText(SettingsActivity.this, "This is too long! ", Toast.LENGTH_SHORT).show();
            }
            else
                validPref = true;
        }

    }

    @Override
    public void onBackPressed()
    {
        //use back arrow
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

                    userid = userFile.getString("username", "UNKNOWN");
                    String skill = sharedPref.getString("skill_preference", "");
                    String zip = sharedPref.getString("zipcode_preference", "");
                    String transportation = sharedPref.getString("transportation_preference", "");
                    String setups = sharedPref.getString("setup_preference", "");
                    int showOnMap = sharedPref.getBoolean("show_on_map_preference", true) ? 1 : 0;  //gets the bool and converts to 1 or 0
                    String bio = sharedPref.getString("bio_preference", "").trim();                 //trim strips beginning and ending space
                    String contact = sharedPref.getString("contact_preference", "").trim();         //trim strips beginning and ending space



                    String query = "UPDATE UserInfo SET skill=" + skill + ", zip= " + zip + ", transportation= " + "'" + transportation + "'" + ", setups= " + setups
                            + ", showOnMap= " + showOnMap + ", bio= " + "'" + bio + "'" + ", contactInfo= " + "'" + contact + "'"
                            + " WHERE id = (SELECT id FROM Users WHERE username = '"
                            + userid + "')";

                    Log.d("preftest", query);

                    Statement stmnt = conn.createStatement();
                    stmnt.executeUpdate(query);

                    z = "update successful";
                    isSuccess = true;
                }
            }

            catch(Exception ex)
            {
                isSuccess = false;
                Log.e("settings_error", ex.getMessage());
                z = "Error";
            }
            return z;
        }


    }
}
