package com.raffaele.jeanluc.playerlocator;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jean-Luc on 1/24/2018.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback
{
    List<String> Usernames;
    List<LatLng> LatLngs;

    ConnectionClass connectionClass;

    Boolean connectionSuccess;
    final Geocoder geocoder = new Geocoder(this);


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Usernames = new ArrayList<>();
        LatLngs = new ArrayList<>();
        connectionClass = new ConnectionClass();
        GetMapData mapData = new GetMapData();

        try {
            mapData.execute("").get();
        }
        catch(InterruptedException | ExecutionException e)
        {

        }


            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_maps);
            final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);



        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        LatLng myLatLng = getMyLatLng();

        float zoom = 10;
        googleMap.addMarker(new MarkerOptions().position(myLatLng)
                .title("Your Location!"));

        //add marker for each user
        for (int i = 0; i < LatLngs.size(); i++)
        {
            LatLng userLocation = LatLngs.get(i);

            googleMap.addMarker(new MarkerOptions().position(userLocation)
                    .title(Usernames.get(i)));
        }

        Log.d("maptest" , "In onmapready: useranme: " + Usernames.toString());
        Log.d("maptest", "In onmapready: locations: " + LatLngs.toString());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, zoom));


    }

    private LatLng getMyLatLng()
    {
        LatLng myLatLng = new LatLng(0,0);
        String myzip;
        try
        {
            //get user zipcode from preferences
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            myzip = sharedPref.getString("zipcode_preference", "");

            List<Address> addresses = geocoder.getFromLocationName(myzip, 5);

            Log.d("maptest" , "In: getMyLatLng: Address Listing: " + addresses.toString());
            if (addresses != null && !addresses.isEmpty())
            {
                Address address = addresses.get(0);
                myLatLng = new LatLng(address.getLatitude(), address.getLongitude());


            }
            else
            {
                Toast.makeText(MapActivity.this, "Unable to geocode zipcode", Toast.LENGTH_LONG).show();

            }


        }
        catch (IOException e)
        {
            Log.e("mapError", "error in getting latitude and longitude");
        }

        return myLatLng;
    }

    public class GetMapData extends AsyncTask<String,String,String>
    {
        String z = "";



        @Override
        protected String doInBackground(String... params)
        {
            String userZip;
            String userName;
            connectionSuccess = false;
            Boolean validResult = false;


            try
            {
                Connection conn = connectionClass.CONN();

                if (conn == null)
                    z = "could not populate map";
                else
                {
                    //Get username and zipcode from database
                    String query = "SELECT zip,username FROM UserInfo join Users on UserInfo.id=Users.id";

                    Statement stmnt = conn.createStatement();
                    ResultSet rs = stmnt.executeQuery(query);

                    SharedPreferences sharedPref = getSharedPreferences("PlayerLocator", MODE_PRIVATE);
                    String currentUser = sharedPref.getString("username", "UNKNOWN");

                    while (rs.next())
                    {
                        userZip = rs.getString("zip");
                        userName = rs.getString("username");
                        List<Address> addresses = geocoder.getFromLocationName(userZip, 1);
                        Log.d("maptest","In doinbackground: addresses: " + addresses.toString());
                         if(userName.equals(currentUser))
                             validResult = false;
                         else
                             validResult = true;
                        if (addresses != null && !addresses.isEmpty() && validResult)
                        {

                            Address address = addresses.get(0);
                            LatLngs.add(new LatLng(address.getLatitude(), address.getLongitude()));
                            Usernames.add(userName);
                        }
                    }
                    z = "Got map data";
                    connectionSuccess= true;
                }
            }

            catch(Exception ex)
            {

                z = "Exceptions";
            }

            return z;
        }

        @Override
        protected void onPostExecute(String types)
        {

        }
    }




}
