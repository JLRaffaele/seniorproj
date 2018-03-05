package com.raffaele.jeanluc.playerlocator;

/**
 * Created by Jean-Luc on 10/31/2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import android.preference.PreferenceManager;


public class LoginActivity extends Activity {
    ConnectionClass connectionClass;
    EditText edtuserid, edtpass;
    Button btnlogin;
    ProgressBar pbbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setting default screen to login.xml
        setContentView(R.layout.login);

        TextView registerScreen = (TextView) findViewById(R.id.link_to_register);

        // Listening to register new account link
        registerScreen.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // Switching to Register screen
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });

        connectionClass = new ConnectionClass();
        edtuserid = (EditText) findViewById(R.id.edtuserid);
        edtpass = (EditText) findViewById(R.id.edtpass);
        btnlogin = (Button) findViewById(R.id.btnLogin);
        pbbar = (ProgressBar) findViewById(R.id.pbbar);
        pbbar.setVisibility(View.GONE);

        btnlogin.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            DoLogin doLogin = new DoLogin();
                                            doLogin.execute("");
                                        }
                                    });

    }

    public class DoLogin extends AsyncTask<String,String,String>
    {
        String z = "";
        Boolean isSuccess = false;

        String userid = edtuserid.getText().toString();
        String password = edtpass.getText().toString();

        @Override
        protected void onPreExecute()
        {
            pbbar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String r)
        {
            pbbar.setVisibility(View.GONE);
            Toast.makeText(LoginActivity.this, r, Toast.LENGTH_SHORT).show();


            if (isSuccess)
            {
                //Add username to preferences
                //SharedPreferences prefs = getSharedPreferences("PlayerLocator", MODE_PRIVATE);
                //prefs.edit().putString("username", userid).commit();

                //start main activity
                Intent i = new Intent(LoginActivity.this, MainActivity.class );
                startActivity(i);
            }
        }

        @Override
        protected String doInBackground(String... params)
        {
            if(userid.trim().equals("") || password.trim().equals(""))
                z = "Please enter Username and Password";
            else
            {
                try
                {
                    Connection conn = connectionClass.CONN();

                    if (conn == null)
                        z = "Error in connection with SQL Server";

                    else
                    {
                        String query = "Select * from dbo.Users where username='" + userid + "'";
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(query);
                        rs.next();
                        String storedhash = rs.getString("pass");

                        Boolean matched = validatePassword(password.trim(), storedhash);
                        if (matched)
                        {
                            z = "Login successful";
                            isSuccess = true;
                        }
                        else
                        {
                            z = "Invalid Username/Password";
                            isSuccess = false;
                        }
                    }
                }
                catch(Exception ex)
                {
                    isSuccess = false;
                    Log.d("logintest", ex.getMessage());
                    z = "Invalid username/password";
                }
            }
            return z;
        }
    }



    private static boolean validatePassword(String originalPassword, String storedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException
    {

        String[] parts = storedPassword.split(":");
        int iterations = Integer.parseInt(parts[0]);
        byte[] salt = fromHex(parts[1]);
        byte[] hash = fromHex(parts[2]);



        PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), salt, iterations, hash.length * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] testHash = skf.generateSecret(spec).getEncoded();



        int diff = hash.length ^ testHash.length;
        for (int i = 0; i < hash.length && i < testHash.length; i++)
        {
            diff |= hash[i] ^ testHash[i];
        }

        return diff == 0;
    }

    private static byte[] fromHex(String hex) throws NoSuchAlgorithmException
    {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i<bytes.length; i++)
        {
            bytes[i] = (byte)Integer.parseInt(hex.substring(2 *i, 2 * i + 2), 16);
        }
        return bytes;
    }

}

