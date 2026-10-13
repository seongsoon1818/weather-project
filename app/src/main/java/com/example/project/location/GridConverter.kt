package com.example.project.location

import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.tan

data class GridPoint(
    val x: Int,
    val y: Int
)

object GridConverter {
    fun convert(lat: Double, lon: Double): GridPoint{
        val re = 6317.00877
        val grid = 5.0
        val slat1 = 30.0
        val slat2 = 60.0
        val olon = 126.0
        val olat = 38.0
        val xo = 43.0
        val yo = 136.0

        val degrad = Math.PI / 180.0

        val reGrid = re / grid
        val slat1Rad = slat1 * degrad
        val slat2Rad = slat2 * degrad
        val olonRad = olon * degrad
        val olatRad = olat * degrad

        var sn = tan(Math.PI * 0.25 + slat2Rad * 0.5) /
                tan(Math.PI * 0.25 + slat1Rad * 0.5)
        sn = ln(cos(slat1Rad) / cos(slat2Rad)) / ln(sn)

        var sf = tan(Math.PI * 0.25 + slat1Rad * 0.5)
        sf = sf.pow(sn) * cos(slat1Rad) / sn

        var ro = tan(Math.PI * 0.25 + olatRad * 0.5)
        ro = reGrid * sf / ro.pow(sn)

        var ra = tan(Math.PI * 0.25 + lat * degrad * 0.5)
        ra = reGrid * sf / ra.pow(sn)

        var theta = lon * degrad - olonRad

        if (theta > Math.PI) {
            theta -= 2.0 * Math.PI
        }

        if (theta < -Math.PI) {
            theta += 2.0 * Math.PI
        }

        theta *= sn

        val x = floor(ra * sin(theta) + xo + 0.5).toInt()
        val y = floor(ro - ra * cos(theta) + yo + 0.5).toInt()

        return GridPoint(x, y)
    }
}