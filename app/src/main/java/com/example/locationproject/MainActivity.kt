package com.example.locationproject

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps_raw)

        // Initialize the map fragment
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        try {
            // Apply the custom map style from the raw resource
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json)
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }

        // Define locations (for example, Sydney and New York)
        val sydney = LatLng(-34.0, 151.0)
        val newYork = LatLng(40.7128, -74.0060)
        val london = LatLng(51.5074, -0.1278)

        // Add markers for each location
        googleMap.addMarker(MarkerOptions().position(sydney).title("Sydney"))
        googleMap.addMarker(MarkerOptions().position(newYork).title("New York"))
        googleMap.addMarker(MarkerOptions().position(london).title("London"))

        // Move camera to the first marker (Sydney) as a default view
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 10f))
    }


    companion object {
        private const val TAG = "MapsActivityRaw"
    }
}
