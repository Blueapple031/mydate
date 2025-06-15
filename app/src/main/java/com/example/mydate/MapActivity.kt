package com.example.mydate

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import java.io.IOException
import java.io.OutputStream

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private val markers = mutableListOf<MarkerOptions>()
    private val LOCATION_PERMISSION_REQUEST = 1
    private var course: Course? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        val captureButton: ImageButton = findViewById(R.id.captureButton)
        supportActionBar?.hide()
        course = intent.getParcelableExtra("course")

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        captureButton.setOnClickListener {
            val rootView = findViewById<View>(R.id.map) // 캡처할 뷰 (예: 지도 화면)
            val bitmap = captureScreen(rootView) // 화면을 캡처한 Bitmap

            // 갤러리에 이미지 저장
            saveImageToGallery(bitmap, this)
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

    private fun captureScreen(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)  // 화면을 Bitmap으로 변환
        return bitmap
    }

    // 갤러리에 이미지 저장
    private fun saveImageToGallery(bitmap: Bitmap, context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 안드로이드 10 (API 29) 이상에서는 MediaStore를 사용하여 저장
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "captured_image.png") // 파일 이름
                put(MediaStore.Images.Media.MIME_TYPE, "image/png") // MIME 타입
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "Pictures/MyApp"
                ) // 저장할 폴더 (Pictures/앱 이름)
            }

            val contentUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val imageUri: Uri? = context.contentResolver.insert(contentUri, contentValues)

            if (imageUri != null) {
                try {
                    val outputStream: OutputStream? =
                        context.contentResolver.openOutputStream(imageUri)
                    outputStream?.let {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) // PNG 형식으로 저장
                        it.close()
                    } ?: run {
                        Toast.makeText(context, "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                    Toast.makeText(context, "이미지가 갤러리에 저장되었습니다.", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(context, "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // 안드로이드 9 (API 28) 이하에서는 legacy 방법 사용
            val file = context.getExternalFilesDir(null)?.let {
                java.io.File(it, "captured_image.png")
            }
            try {
                val outputStream = file?.let { java.io.FileOutputStream(it) }
                outputStream?.let {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    it.flush()
                    it.close()
                    Toast.makeText(context, "이미지가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                } ?: run {
                    Toast.makeText(context, "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, "이미지 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}