package com.example.mydate

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mydate.data.GeocodingResponse
import com.example.mydate.data.GeocodingService
import com.example.mydate.ui.MapUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private val markers = mutableListOf<MarkerOptions>()
    private val LOCATION_PERMISSION_REQUEST = 1
    private var pendingCourse: List<Pair<String, String>>? = null

    private val geocodingService: GeocodingService by lazy {
        Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeocodingService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        // 코스 데이터를 먼저 저장
        pendingCourse = intent.getSerializableExtra("course") as? List<Pair<String, String>>

        // 지도 초기화
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 초기 카메라 위치 설정 (서울)
        val seoul = LatLng(37.5665, 126.9780)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 12f))

        // 위치 권한 확인 및 요청
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap?.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }

        // 저장된 코스 데이터가 있다면 처리
        pendingCourse?.let { course ->
            processCourseLocations(course)
            pendingCourse = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    mMap?.isMyLocationEnabled = true
                }
            } else {
                Toast.makeText(
                    this,
                    "위치 권한이 필요합니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun processCourseLocations(course: List<Pair<String, String>>) {
        mMap?.let { map ->
            map.clear()
            markers.clear()

            course.forEach { (timeOfDay, location) ->
                if (timeOfDay != "제목") {
                    geocodeLocation(location, timeOfDay)
                }
            }
        }
    }

    private fun geocodeLocation(location: String, timeOfDay: String) {
        val apiKey = "AIzaSyCAXShO8-9hrp9Gnea3T6voB81WQESYzZM"
        geocodingService.getLocation(location, apiKey).enqueue(object : Callback<GeocodingResponse> {
            override fun onResponse(call: Call<GeocodingResponse>, response: Response<GeocodingResponse>) {
                if (response.isSuccessful) {
                    val result = response.body()?.results?.firstOrNull()
                    if (result != null) {
                        val lat = result.geometry.location.lat
                        val lng = result.geometry.location.lng
                        val latLng = LatLng(lat, lng)

                        val marker = MarkerOptions()
                            .position(latLng)
                            .title("$timeOfDay: $location")
                        markers.add(marker)
                        mMap?.addMarker(marker)

                        if (markers.size == 4) {
                            mMap?.let { map ->
                                MapUtils.drawRoute(map, markers)
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                Toast.makeText(
                    this@MapActivity,
                    "위치 정보를 가져오는데 실패했습니다: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}

data class GeocodingResponse(
    val results: List<GeocodingResult>
)

data class GeocodingResult(
    val geometry: Geometry
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)