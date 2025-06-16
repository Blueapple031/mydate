package com.example.mydate

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mydate.data.model.Course
import com.example.mydate.ui.MapUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import android.content.Intent
import android.net.Uri

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private val markers = mutableListOf<MarkerOptions>()
    private val LOCATION_PERMISSION_REQUEST = 1
    private var course: Course? = null
    private lateinit var captureButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        captureButton = findViewById(R.id.captureButton)
        supportActionBar?.hide()
        course = intent.getParcelableExtra("course")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        captureButton.setOnClickListener {
            openWebPage("https://www.google.com")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 초기 카메라 위치 설정 (서울)
        val seoul = LatLng(37.5665, 126.9780)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 12f))

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

        course?.let { processCourse(it) }

        mMap?.setOnMarkerClickListener { marker ->
            val searchQuery = marker.title?.substringAfter(": ") ?: ""
            if (searchQuery.isNotEmpty()) {
                openWebPage("https://www.google.com/search?q=$searchQuery")
            }
            true
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

    private fun processCourse(course: Course) {
        mMap?.let { map ->
            map.clear()
            markers.clear()

            // 오전 장소
            course.morningLatLng?.let { latLng ->
                addMarker(latLng, "오전", course.morning)
            }

            // 점심 장소
            course.lunchLatLng?.let { latLng ->
                addMarker(latLng, "점심", course.lunch)
            }

            // 오후 장소
            course.afternoonLatLng?.let { latLng ->
                addMarker(latLng, "오후", course.afternoon)
            }

            // 저녁 장소
            course.eveningLatLng?.let { latLng ->
                addMarker(latLng, "저녁", course.evening)
            }

            // 모든 마커가 보이도록 카메라 이동
            if (markers.isNotEmpty()) {
                val builder = LatLngBounds.builder()
                markers.forEach { marker ->
                    builder.include(marker.position)
                }
                
                // 마커 주변을 보여주기 위해 패딩 추가
                val padding = 100 // 패딩 값 (픽셀)
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding))
                
                // 경로 그리기
                if (markers.size >= 2) {
                    MapUtils.drawRoute(map, markers)
                }
            }
        }
    }

    private fun addMarker(latLng: LatLng, timeOfDay: String, location: String) {
        val marker = MarkerOptions()
            .position(latLng)
            .title("$timeOfDay: $location")
            .snippet("클릭하여 상세 정보 확인")
        markers.add(marker)
        mMap?.addMarker(marker)
    }
    private fun openWebPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}