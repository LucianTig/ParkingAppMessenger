package com.luciantig.parkingapp.ui.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.luciantig.parkingapp.R
import com.luciantig.parkingapp.others.Constants.BLACK_COLOR_STRNG
import com.luciantig.parkingapp.others.Constants.DEFAULT_ZOOM
import com.luciantig.parkingapp.others.Constants.FASTEST_INTERVAL
import com.luciantig.parkingapp.others.Constants.INTERVAL
import com.luciantig.parkingapp.others.Constants.KEY_CAMERA_POSITION
import com.luciantig.parkingapp.others.Constants.KEY_LOCATION
import com.luciantig.parkingapp.others.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.luciantig.parkingapp.others.MapUtility
import com.luciantig.parkingapp.ui.MainActivity
import com.luciantig.parkingapp.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.fragment_map.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.util.*


open class MapFragment : Fragment(R.layout.fragment_map), EasyPermissions.PermissionCallbacks{

    private lateinit var viewModel: MainViewModel

    private var map: GoogleMap? = null

    private lateinit var mFusedLocationProviderClient : FusedLocationProviderClient
    private lateinit var  geocoder : Geocoder
    private lateinit var lastKnownLocation : Location
    private lateinit var cameraPosition : Location

    private val TAG = "MapFragment"

    private lateinit var mLocationRequest: LocationRequest

    private var isYourLocationDrawn = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel

        if(!viewModel.userIsLogged()){
            findNavController().navigate(R.id.action_mapFragment_to_loginFragment)
        }

        Log.d(TAG, "savedInstanceState $savedInstanceState")
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)!!
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)!!
            animateMoveCamera(lastKnownLocation, DEFAULT_ZOOM)
        }
        requestPermissions()

        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync {
            map = it
            onMapClickListener()
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        geocoder  = Geocoder(requireActivity(), Locale.getDefault())


        ic_gps.setOnClickListener{

            if (this::lastKnownLocation.isInitialized){
                Log.d(TAG, "lst "+ lastKnownLocation.accuracy)
                addYourLocationMarker()
                animateMoveCamera(lastKnownLocation, DEFAULT_ZOOM)
            }
            startLocationUpdates()
            val aniRotate: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.clockwise)
            ic_gps.startAnimation(aniRotate)
        }

        ic_car.setOnClickListener{
            viewModel.firebaseRepository.findUserCar()
        }

        viewModel.carSavedPostionLiveData.observe(viewLifecycleOwner, Observer{
            if(it.latitude == -1.0 && it.longitude == -1.0){
                Toast.makeText(activity, "You don't have your car location saved.", Toast.LENGTH_SHORT).show()
            }else{
                addYourCarMarker(it)
                map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.latitude,
                            it.longitude), DEFAULT_ZOOM))
            }
        })

    }

    private fun onMapClickListener(){
        map?.setOnMapLongClickListener {
            Log.d(TAG, "From setOnMapClickListener")
            addYourCarMarker(it)

            val addresses: List<Address>
            try {
                addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)

                viewModel.addUserCarLocation(it, addresses[0].countryName, addresses[0].locality, addresses[0].locality)
                Toast.makeText(
                    requireContext(),
                    "Location car saved on " + addresses[0].thoroughfare,
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: IOException) {
                Toast.makeText(requireContext(), "Nothing here", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    , 11)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
        val alert: AlertDialog  = builder.create()
        alert.show()
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    fun onLocationChanged(location: Location) {
        lastKnownLocation = location
        Log.d(TAG, "lastKnowLocation="+ lastKnownLocation.accuracy)
        if (lastKnownLocation != null) {

            if(lastKnownLocation.accuracy < 100 && lastKnownLocation.accuracy > 30){
                if(!isYourLocationDrawn){
                    addYourLocationMarker()
                }
            }
            if(lastKnownLocation.accuracy < 30){
                map?.clear()
                animateMoveCamera(lastKnownLocation, DEFAULT_ZOOM)
                addYourLocationMarker()

                stoplocationUpdates()
                ic_gps.clearAnimation()
            }
        }
    }

    private fun addYourLocationMarker(){
        map?.addMarker(
            MarkerOptions().position(LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)).icon(
                MapUtility.vectorToBitmap(
                    R.drawable.ic_men,
                    Color.parseColor(BLACK_COLOR_STRNG),
                    resources
                )
            )
        )
    }

    private fun addYourCarMarker(latLng: LatLng){
        map?.addMarker(
            MarkerOptions().position(LatLng(latLng.latitude, latLng.longitude)).icon(
                MapUtility.vectorToBitmap(
                    R.drawable.car_icon,
                    Color.parseColor(BLACK_COLOR_STRNG),
                    resources
                )
            )
        )
    }

    private fun animateMoveCamera(latLng: Location, defaultZoom: Float){
        map?.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(latLng.latitude,
                    latLng.longitude), defaultZoom))
    }

    private fun startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        settingsClient.checkLocationSettings(locationSettingsRequest)

        if (ActivityCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions()
            return
        }
        if(!MapUtility.isLocationEnabled(requireContext())){
            buildAlertMessageNoGps()
            Log.d(TAG, "isLocationEnabled = "+MapUtility.isLocationEnabled(requireContext()))
        }else {
            mFusedLocationProviderClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
            )
        }
    }

    private fun stoplocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
    }

    private fun requestPermissions()  {
        if (MapUtility.hasLocationPermissions(requireContext())) {
            return
        }
        EasyPermissions.requestPermissions(
            this,
            "You need to accept location permissions to use this app.",
            REQUEST_CODE_LOCATION_PERMISSION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onResume() {
        mapView?.onResume()
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onStart() {
        mapView?.onStart()
        super.onStart()
        Log.d(TAG, "onStart")
    }

    override fun onStop() {
        mapView?.onStop()
        super.onStop()
        Log.d(TAG, "onStop")
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
        Log.d(TAG, "onPause")
    }

    override fun onLowMemory() {
        mapView?.onLowMemory()
        super.onLowMemory()
        Log.d(TAG, "onLowMemory")
    }

    override fun onDestroy() {
        mapView?.onDestroy()
        super.onDestroy()
        Log.d(TAG, "onDestory")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState")
        map?.let { map ->
            //outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            //outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}