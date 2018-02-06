package com.raffaele.jeanluc.playerlocator;

/**
 * Created by Jean-Luc on 10/31/2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class RegisterActivity extends Activity {
    ConnectionClass connectionClass;
    Button btnRegister;
    EditText edtuserid, edtpass;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set View to register.xml
        setContentView(R.layout.register);

        TextView loginScreen = (TextView) findViewById(R.id.link_to_login);

        // Listening to Login Screen link
        loginScreen.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                // Closing registration screen
                // Switching to Login Screen/closing register screen

                finish();
            }
        });

        connectionClass = new ConnectionClass();
        edtuserid = (EditText) findViewById(R.id.edtuserid);
        edtpass = (EditText) findViewById(R.id.edtpass);
        btnRegister = (Button) findViewById(R.id.btnRegister);


        btnRegister.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                            DoRegister doRegister = new DoRegister();
                                            doRegister.execute("");
                                       }
                                });
    }

    public class DoRegister extends AsyncTask<String, String, String> {
        String z = "";
        Boolean isSuccess = false;

        String userid = edtuserid.getText().toString();
        String password = edtpass.getText().toString();

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(String r) {
            Toast.makeText(RegisterActivity.this, r, Toast.LENGTH_SHORT).show();

            if (isSuccess) {
                finish();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            if (userid.trim().equals("") || password.trim().equals(""))
                z = "Please enter a username and password";

            //Check for special characters in username
            Pattern p = Pattern.compile("[^a-z0-9 ] ", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(userid.trim());
            Boolean b = m.find();

            if (b)
                z = "Invalid username";

            else {
                try {
                    Connection conn = connectionClass.CONN();
                    if (conn == null)
                        z = "Error in connection with SQL Server";

                    else {
                        //Check to see if name is already taken
                        String query = "select * from Users where username= '" + userid + "'";
                        Statement stmt = conn.createStatement();
                        ResultSet rs = stmt.executeQuery(query);
                        z = "Invalid username";

                        //If name is not taken
                        if (!rs.next()) {
                            String hashedpass = generateStoringPasswordHash(password.trim());

                            query = "insert into Users (username, pass) " +
                                    "values ('" + userid.trim() + "', '" + hashedpass + "') " +
                                    "select * from Users where username='" + userid + "'";
                            stmt = conn.createStatement();
                            rs = stmt.executeQuery(query);

                            z = "Registration successful";
                            isSuccess = true;
                        }
                    }
                } catch (Exception ex) {
                    isSuccess = false;
                    Log.d("sql test" , ex.getMessage());
                    z = "Exceptions";
                }

            }
            return z;
        }
    }




    //hashing password

    private static String generateStoringPasswordHash(String password) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        int iterations = 1000;
        char[] chars = password.toCharArray();
        byte[] salt = getSalt();



        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();

        return iterations + ":" + toHex(salt) + ":" + toHex(hash);

    }

    private static byte[] getSalt() throws NoSuchAlgorithmException
    {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

    private static String toHex(byte[] array) throws NoSuchAlgorithmException
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if (paddingLength > 0)
        {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        }
        else
        {
            return hex;
        }
    }
}