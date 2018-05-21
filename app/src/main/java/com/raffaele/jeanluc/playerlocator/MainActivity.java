package com.raffaele.jeanluc.playerlocator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.widget.Toast;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {

    String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        //Name of any viewable profile
        Intent i = getIntent();
        String profile_name = i.getExtras().getString("profile_name");

        String bio_text = LoadBio(profile_name);
        TextView bio_text_view = (TextView)findViewById(R.id.main_activity_bio);
        bio_text_view.setText(bio_text);

        //logged in users' name
        username = getSharedPreferences("PlayerLocator", MODE_PRIVATE).getString("username", "UNKNOWN");



        //set the profile name text
        TextView profile_name_text = (TextView)findViewById(R.id.main_activity_username);
        profile_name_text.setText(profile_name);

        Button main_button = (Button)findViewById(R.id.main_activity_button);
        Button backbtn = (Button)findViewById(R.id.main_activity_back_button);

        //if we are on our own profile
        if(username.equals(profile_name))
        {
            main_button.setText("View the map!");

            //don't show back button on our own profile
            backbtn.setVisibility(View.GONE);

            main_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(), MapActivity.class);
                    startActivity(i);
                }

            });
        }
        else
        {
            main_button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    String toast_text = "";
                    try {
                        SendChallenge sendChallenge = new SendChallenge();
                        toast_text = sendChallenge.execute().get();

                    }
                    catch(InterruptedException | ExecutionException e)
                    {
                        Log.e("challenge_debug", "Error sending challenge");
                        //Toast.makeText(this, "Error obtaining map data", Toast.LENGTH_SHORT);

                    }

                    Toast.makeText(MainActivity.this, toast_text, Toast.LENGTH_LONG).show();
                }

            });

            backbtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getApplicationContext(), MapActivity.class);
                    startActivity(i);
                }

            });
            main_button.setText("Challenge " + profile_name + "!");
        }





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
                Intent intent_logout = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent_logout);
                finish();
                break;
            case R.id.action_my_profile:
                Intent intent1 = getIntent();
                intent1.putExtra("profile_name", username);
                finish();
                startActivity(intent1);
                break;
            case R.id.action_challenges:
                Intent intent2 = new Intent(MainActivity.this, ChallengeActivity.class );
                intent2.putExtra("username", username);
                startActivity(intent2);
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {

    }


    public String LoadBio(String name)
    {
        String bio_text = "";
        ConnectionClass connectionClass;
        connectionClass = new ConnectionClass();

        try {
            Connection conn = connectionClass.CONN();

            if (conn != null) {


                //Check to see if challenge exists
                String query = "SELECT bio FROM UserInfo JOIN Users ON UserInfo.id = Users.id WHERE username = "
                        + "'" + name + "'";
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                rs.next();
                bio_text = rs.getString("bio");

            }
        }
        catch(Exception ex)
        {
            Log.e("bio_error", ex.getMessage());
        }
        return bio_text;
    }







    class SendChallenge extends AsyncTask<String, String, String>
    {
        ConnectionClass connectionClass;
        String z = "";




        @Override
        protected String doInBackground(String... params)
        {
            connectionClass = new ConnectionClass();
            TextView profile_name_text = (TextView)findViewById(R.id.main_activity_username);
            String challengee = profile_name_text.getText().toString();
            Boolean validChallenge = true;
            try
            {
                Connection conn = connectionClass.CONN();

                if (conn == null)
                    z = "Could not send challenge";
                else
                {
                    //TODO: major refactoring of this code

                    //Check to see if challenge exists
                    String query = "SELECT challenger,challengee FROM Challenges WHERE challenger= '" + username + "' AND challengee= '" + challengee + "'";
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    //Check to see if they have been challenged by the person
                    if (rs.next())
                        validChallenge = false;

                    query = "SELECT challenger,challengee FROM Challenges WHERE challenger= '" + challengee + "' AND challengee= '" + username + "'";
                    stmt = conn.createStatement();
                    rs = stmt.executeQuery(query);



                    if (!rs.next() && validChallenge) {

                        query = "INSERT INTO Challenges (challenger, challengee) VALUES " +
                                "('" + username + "','" + challengee + "');";

                        Log.d("challenge_debug", query);
                        stmt = conn.createStatement();
                        stmt.executeUpdate(query);

                        z = "Challenge Sent!";
                    }

                    else
                        z = "Challenge already exists!";

                }
            }
            catch (Exception ex)
            {
                z = "Error sending challenge";
                Log.e("challenge_error", ex.getMessage());
            }

            return z;
        }
    }
}
