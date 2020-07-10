package de.tudarmstadt.iptk.foxtrot.vivacoronia.googleMapFunctions

import android.graphics.Color
import android.location.Location
import androidx.core.graphics.ColorUtils
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*

object GoogleMapFunctions  {
    /**
     * @param center: coordinates of center point
     * @param currentColor: current color from color array
     * @return Circle options to be drawn onto the map
     */
    fun createCircleOptions(
        center: LatLng,
        currentColor: Int
    ): CircleOptions {
        val circleOptions = CircleOptions()
        circleOptions.center(center)
        circleOptions.radius(2.0)
        circleOptions.strokeColor(currentColor)
        circleOptions.fillColor(currentColor)
        circleOptions.strokeWidth(2f)
        return circleOptions
    }

    fun generateColors(amount: Int): List<List<Int>>{
        val colors = ArrayList<ArrayList<Int>>()
        for(i in 0 until amount){
            val rnd = Random()
            val color1 = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            val color2 = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            colors.add(arrayListOf(color1, color2))
        }
        return colors
    }

    /**
     * @param amount: amount of lines to be drawn/colored
     * @param startColor: color to start with
     * @param endColor: color to end with
     * @return ArrayList of colors for each line to be drawn interpolated between startColor and endColor
     */
    fun getColorArray(amount: Int, startColor: Int, endColor: Int): List<Int>{
        val fraction = 1f / amount
        val colorArray = ArrayList<Int>()
        for (i in 0 until amount){
            colorArray.add(ColorUtils.blendARGB(startColor, endColor, i * fraction))
        }
        return colorArray
    }

    /**
     * @param coordinates: list of unprocessed coordinates
     * @return list of lists containing coordinates which are closer to each other than the distance threshold and
     * the speed between them is greater than the speed threshold
     */
    fun preprocessedCoordinatesForDrawing(coordinates: List<Location>, speedThreshold: Float, distanceThreshold: Float): List<List<Location>>{
        val returnList = ArrayList<ArrayList<Location>>()
        for (coordinate in coordinates){
            when {
                returnList.isEmpty() -> {
                    returnList.add(arrayListOf(coordinate))
                }
                checkSpeedAndDistance(returnList.last().last(), coordinate, speedThreshold, distanceThreshold) -> {
                    returnList.last().add(coordinate)
                }
                else -> {
                    returnList.add(arrayListOf(coordinate))
                }
            }
        }
        return returnList
    }

    /**
     * @param start: location of starting point
     * @param end: location of end point
     * @return boolean whether the two points are closer to each other than the distance threshold and
     * the speed between them is greater than the speed threshold
     */
    private fun checkSpeedAndDistance(start: Location, end: Location, speedThreshold: Float, distanceThreshold: Float): Boolean {
        val startLatLng = start.getLatLong()
        val endLatLng = end.getLatLong()
        val isTimeRelevant = isSpeedOnPathGreaterThanThreshold(start.time, end.time, startLatLng, endLatLng, speedThreshold)
        val isDistanceRelevant = isCoordinateDistanceLessOrEqualThanThreshold(startLatLng, endLatLng, distanceThreshold)
        return isTimeRelevant && isDistanceRelevant
    }

    /**
     * @return LatLng containing the latitude and longitude of the given location
     */
    fun Location.getLatLong(): LatLng {
        val lat = this.latitude
        val long = this.longitude
        return LatLng(lat, long)
    }

    /**
     * @param start: location of starting point
     * @param end: location of end point
     * @return boolean whether the distance between start and end is smaller/equal than the given threshold
     */
    private fun isCoordinateDistanceLessOrEqualThanThreshold(start: LatLng, end: LatLng, distanceThreshold: Float): Boolean {
        val distance = getCoordinateDistanceOnSphere(start, end)
        return distance <= distanceThreshold
    }

    /**
     * @param startTime: timestamp of starting point
     * @param endTime: timestamp of end point
     * @param startLocation: location of starting point
     * @param endLocation: location of end point
     * @return boolean whether the average speed between start and end is greater than the given threshold
     */
    private fun isSpeedOnPathGreaterThanThreshold(startTime: Long, endTime: Long, startLocation: LatLng, endLocation: LatLng, speedThreshold: Float): Boolean {
        val distance = getCoordinateDistanceOnSphere(startLocation, endLocation)
        val timeDifference = (endTime - startTime) / (1000f * 60 * 60)
        val speed = distance / timeDifference
        return speed > speedThreshold
    }

    /**
     * @param startLocation: location of starting point
     * @param endLocation: location of end point
     * @return the distance between the two given locations on a sphere with the size of earth
     */
    private fun getCoordinateDistanceOnSphere(
        startLocation: LatLng,
        endLocation: LatLng
    ): Double {
        val lon1 = Math.toRadians(startLocation.longitude)
        val lat1 = Math.toRadians(startLocation.latitude)
        val lon2 = Math.toRadians(endLocation.longitude)
        val lat2 = Math.toRadians(endLocation.latitude)

        //Haversine formula, determines the great-circle distance between two points on a sphere with given longitude and latitude
        val deltaLon = lon2 - lon1
        val deltaLat = lat2 - lat1
        val innerFormula =
            sin(deltaLat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(deltaLon / 2).pow(2.0)
        val outerFormula = 2 * asin(sqrt(innerFormula))

        //radius of the earth in kilometers
        val radius = 6371
        return outerFormula * radius
    }
}