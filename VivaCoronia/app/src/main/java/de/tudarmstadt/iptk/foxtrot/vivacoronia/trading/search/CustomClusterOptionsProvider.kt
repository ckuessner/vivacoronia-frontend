package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.search

import android.content.res.Resources
import android.graphics.*
import android.graphics.Paint.Align
import android.util.LruCache
import com.androidmapsextensions.ClusterOptions
import com.androidmapsextensions.ClusterOptionsProvider
import com.androidmapsextensions.Marker
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R

/** Adopted from:
 * https://github.com/mg6maciej/android-maps-extensions/blob/develop/android-maps-extensions-demo/src/main/java/pl/mg6/android/maps/extensions/demo/DemoClusterOptionsProvider.java
 */
class CustomClusterOptionsProvider(resources: Resources) :
    ClusterOptionsProvider {
    private val baseBitmaps: Array<Bitmap?>
    private val cache: LruCache<Int, BitmapDescriptor> = LruCache(128)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bounds = Rect()
    private val clusterOptions = ClusterOptions().anchor(0.5f, 0.5f)
    override fun getClusterOptions(markers: List<Marker>): ClusterOptions {
        val markersCount = markers.size
        val cachedIcon: BitmapDescriptor? = cache.get(markersCount)
        if (cachedIcon != null) {
            return clusterOptions.icon(cachedIcon)
        }
        var base: Bitmap?
        var i = 0
        do {
            base = baseBitmaps[i]
        } while (markersCount >= forCounts[i++])
        val bitmap = base!!.copy(Bitmap.Config.ARGB_8888, true)
        val text = markersCount.toString()
        paint.getTextBounds(text, 0, text.length, bounds)
        val x = bitmap.width / 2.0f
        val y = (bitmap.height - bounds.height()) / 2.0f - bounds.top
        val canvas = Canvas(bitmap)
        canvas.drawText(text, x, y, paint)
        val icon = BitmapDescriptorFactory.fromBitmap(bitmap)
        cache.put(markersCount, icon)
        return clusterOptions.icon(icon)
    }

    companion object {
        private val res =
            intArrayOf(R.drawable.cluster1, R.drawable.cluster2, R.drawable.cluster3, R.drawable.cluster4)
        private val forCounts = intArrayOf(5, 15, 30, Int.MAX_VALUE)
    }

    init {
        baseBitmaps = arrayOfNulls(res.size)
        for (i in res.indices) {
            baseBitmaps[i] = BitmapFactory.decodeResource(
                resources,
                res[i]
            )
        }
        paint.color = Color.WHITE
        paint.textAlign = Align.CENTER
        paint.textSize = resources.getDimension(R.dimen.text_size)
    }
}