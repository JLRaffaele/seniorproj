package com.raffaele.jeanluc.playerlocator;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

/**
 * Created by Jean-Luc on 1/24/2018.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback
{
    String myzip = "";
    double latitude, longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        final Geocoder geocoder = new Geocoder(this);
        try
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

            myzip = sharedPref.getString("zipcode_preference", "");
            List<Address> addresses = geocoder.getFromLocationName(myzip, 1);
            if (addresses != null && !addresses.isEmpty())
            {
                Address address = addresses.get(0);
                latitude = address.getLatitude();
                longitude = address.getLongitude();
            }
            else
            {
                Toast.makeText(MapActivity.this, "Unable to geocode zipcode", Toast.LENGTH_LONG).show();
            }
        }
        catch (IOException e)
        {
            Log.d("preftest", "error in getting lat and long");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        LatLng userLocation = new LatLng(latitude, longitude);
        float zoom = 10;
        googleMap.addMarker(new MarkerOptions().position(userLocation)
                .title("Your Location!"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, zoom));

    }

}
