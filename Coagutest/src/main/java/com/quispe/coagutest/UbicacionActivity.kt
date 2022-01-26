package com.quispe.coagutest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Futura implementacion incluyendo un fragment dentro de la app
 */
class UbicacionActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ubicacion)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        googleMap.isMyLocationEnabled = true
        // Add a marker and move the camera
        val farmaciaZamenhof = LatLng(41.55116214146715, 2.100585896303669)
        mMap.addMarker(MarkerOptions().position(farmaciaZamenhof).title("Farmacia Zamenhof"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(farmaciaZamenhof))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(farmaciaZamenhof,15f))
    }
}
