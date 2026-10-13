package com.example.project

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.project.location.LocationRepository
import com.example.project.ui.WeatherAdapter
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var tvLocationTitle: TextView
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private lateinit var tvRegionName: TextView
    private lateinit var weatherViewPager: ViewPager2
    private lateinit var locationRepository: LocationRepository
    private var locationPerMissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions ->

            val fineLocationGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

            val coarseLocationGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineLocationGranted || coarseLocationGranted) {
                getCurrentLocation()
            } else {
                updateLocationText("위치 권한이 없어 현재 위치의 날씨를 가져올 수 없습니다.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        tvLocationTitle = findViewById(R.id.tvLocationTitle)
        tvLatitude = findViewById(R.id.tvLatitude)
        tvLongitude = findViewById(R.id.tvLongitude)
        tvRegionName = findViewById(R.id.tvRegionName)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        weatherViewPager = findViewById(R.id.weatherViewPager)

        locationRepository = LocationRepository(this)

        if(hasLocationPermission()) {
            getCurrentLocation()
        }
        else {
            requestLocationPermission()
        }
    }

    private fun hasLocationPermission(): Boolean {
        val finePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val coarsePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        return finePermission == PackageManager.PERMISSION_GRANTED ||
                coarsePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        locationPerMissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }

        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationProviderClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener {
            location ->
            if(location != null) {
                val latitude = location.latitude
                val longitude = location.longitude

                lifecycleScope.launch {
                    val locationRegion = locationRepository.getLocationRegion(
                        latitude = latitude,
                        longitude = longitude
                    )

                    tvLocationTitle.text = "현재 위치"
                    tvLatitude.text = "위도: ${formatLocationValue(locationRegion.latitude)}"
                    tvLongitude.text = "경도: ${formatLocationValue(locationRegion.longitude)}"
                    tvRegionName.text = locationRegion.regionName

                    weatherViewPager.adapter = WeatherAdapter(
                        activity = this@MainActivity,
                        latitude = latitude,
                        longitude = longitude
                    )
                }


            }
            else {
                updateLocationText("""
                    현재 위치를 가져올 수 없습니다.
                """.trimIndent())
            }

        }.addOnFailureListener {
            exception ->
            updateLocationText("""
                위치 조회 실패 ${exception.message}
            """.trimIndent())
        }
    }

    private fun formatLocationValue(value: Double): String {
        return String.format(java.util.Locale.KOREA, "%.7f", value)
    }

    private fun updateLocationText(text: String) {
        tvLocationTitle.text = text
        tvLatitude.text = "위도: --"
        tvLongitude.text = "경도: --"
        tvRegionName.text = "지역 정보 없음"
    }
}