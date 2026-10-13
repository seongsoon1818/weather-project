package com.example.project.location

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale


data class LocationRegion(
    val latitude: Double,
    val longitude: Double,
    val regionName: String
)

class LocationRepository (
    context: Context
) {
    private val appContext = context.applicationContext

    suspend fun getLocationRegion(
        latitude: Double,
        longitude: Double
    ): LocationRegion {
        val regionName = getRegionName(latitude, longitude)

        return LocationRegion(
            latitude = latitude,
            longitude = longitude,
            regionName = regionName
        )
    }

    suspend fun getRegionName(
        latitude: Double,
        longitude: Double
    ): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(appContext, Locale.KOREA)

                @Suppress("DEPRECATION")
                val address = geocoder.getFromLocation(latitude, longitude, 1)
                    ?.firstOrNull()

                if (address == null) {
                    return@withContext "지역 정보 없음"
                }

                val sido = normalizeSido(address.adminArea)
                val subAdminArea = address.subAdminArea
                val locality = address.locality
                val subLocality = address.subLocality

                buildRegionName(
                    sido = sido,
                    subAdminArea = subAdminArea,
                    locality = locality,
                    subLocality = subLocality
                )
            } catch (e: Exception) {
                "지역 정보 조회 실패"
            }
        }
    }

    private fun normalizeSido(adminArea: String?): String {
        if (adminArea.isNullOrBlank()) {
            return ""
        }

        return when {
            adminArea.contains("서울") -> "서울시"
            adminArea.contains("부산") -> "부산시"
            adminArea.contains("대구") -> "대구시"
            adminArea.contains("인천") -> "인천시"
            adminArea.contains("광주") -> "광주시"
            adminArea.contains("대전") -> "대전시"
            adminArea.contains("울산") -> "울산시"
            adminArea.contains("세종") -> "세종시"
            else -> adminArea
        }
    }

    private fun buildRegionName(
        sido: String,
        subAdminArea: String?,
        locality: String?,
        subLocality: String?
    ): String {
        val parts = mutableListOf<String>()

        if (sido.isNotBlank()) {
            parts.add(sido)
        }

        addRegionPart(parts, subAdminArea)
        addRegionPart(parts, locality)
        addRegionPart(parts, subLocality)

        return if (parts.isEmpty()) {
            "지역 정보 없음"
        } else {
            parts.joinToString(" ")
        }
    }

    private fun addRegionPart(
        parts: MutableList<String>,
        value: String?
    ) {
        if (value.isNullOrBlank()) {
            return
        }

        val normalizedValue = normalizeSido(value)

        val alreadyIncluded = parts.any { part ->
            part == normalizedValue ||
                    part.contains(normalizedValue) ||
                    normalizedValue.contains(part)
        }

        if (!alreadyIncluded) {
            parts.add(normalizedValue)
        }
    }
}