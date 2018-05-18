package com.raffaele.jeanluc.playerlocator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jean-Luc on 1/24/2018.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener
{

    List<UserInfo> userInfoList;

    ConnectionClass connectionClass;

    Boolean connectionSuccess;
    final Geocoder geocoder = new Geocoder(this);
    String USER_INFOBOX_TITLE = "Your location!";
    String currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {


        userInfoList = new ArrayList<>();

        connectionClass = new ConnectionClass();
        GetMapData mapData = new GetMapData();

        try {
            mapData.execute("").get();
        }
        catch(InterruptedException | ExecutionException e)
        {
            Log.e("mapdata", "Error obtaining map data");
            //Toast.makeText(this, "Error obtaining map data", Toast.LENGTH_SHORT);

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

        googleMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
        googleMap.setOnInfoWindowClickListener(this);
        float zoom = 10;
        googleMap.addMarker(new MarkerOptions().position(myLatLng)
                .title(USER_INFOBOX_TITLE));

        //add marker for each user
        for (int i = 0; i < userInfoList.size(); i++)
        {
            UserInfo user = userInfoList.get(i);
            LatLng userLocation = user.LatLng;

            googleMap.addMarker(new MarkerOptions().position(userLocation)
                    .title(user.UserName))
                    .setSnippet("Skill: " + user.Skill + "\n" +
                                "Transportation: " + user.Transportation + "\n" +
                                "Smash setups: " + user.Setups);
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, zoom));
        //googleMap.setOnMarkerClickListener(this);

    }


    @Override
    public void onInfoWindowClick(Marker marker)
    {
        Intent i = new Intent(this, MainActivity.class);


        //if the user clicks on their own infobox
        if (marker.getTitle().equals(USER_INFOBOX_TITLE))
        {
            i.putExtra("profile_name", currentUser);
        }
        else
        {
            i.putExtra("profile_name", marker.getTitle());
        }
        startActivity(i);
    }


    @Override
    public boolean onMarkerClick(final Marker marker)
    {
        return true;
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }



    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter
    {
        private final View myContentsView;

        MyInfoWindowAdapter() { myContentsView = getLayoutInflater().inflate(R.layout.custom_info_contents, null);}

        @Override
        public View getInfoContents(Marker marker)
        {
           return null;
        }

        @Override
        public View getInfoWindow(Marker marker)
        {

            TextView tvTitle = ((TextView)myContentsView.findViewById(R.id.title));
            tvTitle.setText(marker.getTitle());
            TextView tvSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));
            tvSnippet.setText((marker.getSnippet()));

            return myContentsView;
        }
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

            List<Address> addresses = geocoder.getFromLocationName(myzip, 1);


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


    class UserInfo
    {
        LatLng LatLng;
        String UserName;
        String Transportation;
        String Setups;
        String Skill;

        public UserInfo()
        {
            UserName = null;
            LatLng = null;
            Transportation = null;
            Setups = null;
            Skill = null;
        }

        public UserInfo(LatLng latLng, String userName, String transportation, String setups, String skill)
        {
            LatLng = latLng;
            UserName = userName;
            Transportation = transportation;
            Setups = setups;
            Skill = skill;
        }

    }

    class GetMapData extends AsyncTask<String,String,String>
    {
        String z = "";



        @Override
        protected String doInBackground(String... params)
        {
            Random rand = new Random();
            Double lat_with_fudge;
            Double long_with_fudge;

            String userZip;
            String userName;
            String skill;
            String transportation;
            String setups;
            connectionSuccess = false;
            Boolean validResult = false;
            Resources res = getResources();
            TypedArray skillValues = res.obtainTypedArray(R.array.skillentries);

            try
            {
                Connection conn = connectionClass.CONN();

                if (conn == null)
                    z = "could not populate map";
                else
                {
                    //Get data from database -- only users who have showOnMap enabled
                    String query = "SELECT zip,username,skill,dob,transportation,setups FROM UserInfo join Users on UserInfo.id=Users.id WHERE showOnMap = 1";

                    Statement stmnt = conn.createStatement();
                    ResultSet rs = stmnt.executeQuery(query);



                    while (rs.next())
                    {
                        userZip = rs.getString("zip");
                        userName = rs.getString("username");
                        skill = rs.getString("skill");
                        transportation = rs.getString("transportation");
                        setups = rs.getString("setups");

                        SharedPreferences sharedPref = getSharedPreferences("PlayerLocator", MODE_PRIVATE);
                        currentUser = sharedPref.getString("username", "UNKNOWN");

                        List<Address> addresses = geocoder.getFromLocationName(userZip, 1);

                        if (addresses != null && !addresses.isEmpty() && !userName.equals(currentUser))
                        {
                            Address address = addresses.get(0);
                            lat_with_fudge = address.getLatitude();
                            long_with_fudge = address.getLongitude();

                            if (rand.nextBoolean())
                                lat_with_fudge += rand.nextDouble() % 0.05;
                            else
                                lat_with_fudge -= rand.nextDouble() % 0.05;

                            if (rand.nextBoolean())
                                long_with_fudge += rand.nextDouble() % 0.05;
                            else
                                long_with_fudge -= rand.nextDouble() % 0.05;


                            Log.d("maptest", Double.toString(lat_with_fudge));
                            Log.d("maptest", Double.toString(long_with_fudge));

                            LatLng coordinates = new LatLng(lat_with_fudge, long_with_fudge);

                            //Adding to userInfoList - skill values gets string from array resources
                            UserInfo info = new UserInfo(coordinates, userName, transportation, setups, skillValues.getString(Integer.parseInt(skill)));
                            userInfoList.add(info);

                        }
                    }
                    z = "Got map data";
                    connectionSuccess= true;
                    //for (UserInfo u : userInfoList)
                     //   Log.d("maptest", u.LatLng.toString());
                }
            }

            catch(Exception ex)
            {
                Log.d("maptest", "exception map");
                Log.d("maptest", ex.getMessage());
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
