package com.example.mydate.ui

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

object MapUtils {
    fun drawRoute(map: GoogleMap, markers: List<MarkerOptions>) {
        val polylineOptions = PolylineOptions()
        markers.forEach { marker ->
            polylineOptions.add(marker.position)
        }
        map.addPolyline(polylineOptions)

        // 모든 마커가 보이도록 카메라 이동
        val builder = LatLngBounds.builder()
        markers.forEach { marker ->
            builder.include(marker.position)
        }
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100))
    }
}