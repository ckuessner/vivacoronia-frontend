package de.tudarmstadt.iptk.foxtrot.vivacoronia.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import com.google.android.gms.maps.model.LatLng
import de.tudarmstadt.iptk.foxtrot.vivacoronia.PermissionHandler

object LocationUtility {
    fun getLastKnownLocation(context: Context): LatLng? {
        if (PermissionHandler.checkLocationPermissions(context)) {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            @SuppressLint("MissingPermission") // Check is in PermissionHandler
            var currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            @SuppressLint("MissingPermission")
            if (currentLocation == null) {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            }
            if (currentLocation != null)
                return LatLng(currentLocation.latitude, currentLocation.longitude)
        }
        return null
    }
}