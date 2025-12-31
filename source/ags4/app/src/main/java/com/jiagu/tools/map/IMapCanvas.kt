package com.jiagu.tools.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import com.jiagu.api.ext.dp2px
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.math.Point2D
import com.jiagu.api.model.MapBlock
import com.jiagu.api.model.MapRing
import com.jiagu.api.model.MapTrack
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

abstract class IMapCanvas(val context: Context) {

    interface MapReadyListener {
        fun onMapReady(canvas: IMapCanvas)
    }
    interface MapChangeListener {
        fun onCameraChange(bearing: Float)
    }
    interface MapClickListener {
        fun onClick(pt: GeoHelper.LatLng)
    }
    interface MapLongClickListener {
        fun onLongClick(pt: GeoHelper.LatLng)
    }
    interface MapMarkerSelectListener {
        fun onMarkerSelect(marker: String)
    }

    interface MarkerDragListener {
        fun onDrag(name: String, pt: GeoHelper.LatLng)
        fun onDragStart(name: String, pt: GeoHelper.LatLng)
        fun onDragFinish(name: String, pt: GeoHelper.LatLng)
    }

    abstract class MarkerOption(
        val anchor: Int,
        val textColor: Int,
        val textSize: Float,
    ) {
        abstract fun draw(title: String, color: Int): Bitmap
    }
    protected var markerOption = object : MarkerOption(
        Params.ANCHOR_BOTTOM_CENTER,
        Color.WHITE,
        14f,
    ) {
        override fun draw(title: String, color: Int): Bitmap {
            return getMarkView(title, color)
        }
    }

    fun applyMarkerOption(option: MarkerOption) {
        markerOption = option
    }

    class LatLngBound(val north: Double, val east: Double, val south: Double, val west: Double)

    val phoneLocation = MutableLiveData<GeoHelper.LatLng>()
    protected var isDragEnable = false

    private var readyListener: MapReadyListener? = null
    private val clickListener = mutableListOf<MapClickListener>()
    private val longClickListener = mutableListOf<MapLongClickListener>()
    private val markerListener = mutableListOf<MapMarkerSelectListener>()
    private val changeListeners = mutableListOf<MapChangeListener>()
    private val markerDragListeners = mutableListOf<MarkerDragListener>()

    private fun checkIsDragEnable() {
        isDragEnable = markerDragListeners.size > 0
    }

    protected fun setMapReadyListener(l: MapReadyListener) {
        readyListener = l
    }

    protected fun mapReady() {
        readyListener?.onMapReady(this)
    }

    fun addMarkerDragListener(l: MarkerDragListener) {
        markerDragListeners.add(l)
        checkIsDragEnable()
    }

    fun removeMarkerDragListener(l: MarkerDragListener) {
        markerDragListeners.remove(l)
        checkIsDragEnable()
    }

    protected fun markerDragging(name: String, pt: GeoHelper.LatLng) {
        for (l in markerDragListeners) {
            l.onDrag(name, pt)
        }
    }

    protected fun markerDragFinished(name: String, pt: GeoHelper.LatLng) {
        for (l in markerDragListeners) {
            l.onDragFinish(name, pt)
        }
    }

    protected fun markerDragStart(name: String, pt: GeoHelper.LatLng) {
        for (l in markerDragListeners) {
            l.onDragStart(name, pt)
        }
    }

    fun addClickListener(l: MapClickListener) {
        clickListener.add(l)
    }

    fun removeClickListener(l: MapClickListener) {
        clickListener.remove(l)
    }

    protected fun mapClicked(pt: GeoHelper.LatLng) {
        for (l in clickListener) {
            l.onClick(pt)
        }
    }

    fun addLongClickListener(l: MapLongClickListener) {
        longClickListener.add(l)
    }

    fun removeLongClickListener(l: MapLongClickListener) {
        longClickListener.remove(l)
    }

    protected fun mapLongClicked(pt: GeoHelper.LatLng) {
        for (l in longClickListener) {
            l.onLongClick(pt)
        }
    }

    fun addMarkClickListener(l: MapMarkerSelectListener) {
        markerListener.add(l)
    }

    fun removeMarkClickListener(l: MapMarkerSelectListener) {
        markerListener.remove(l)
    }

    protected fun markerSelected(marker: String) {
        for (l in markerListener) {
            l.onMarkerSelect(marker)
        }
    }

    fun addChangeListener(l: MapChangeListener) {
        changeListeners.add(l)
    }

    fun removeChangeListener(l: MapChangeListener) {
        changeListeners.remove(l)
    }

    protected fun cameraChange(bearing: Float) {
        for (l in changeListeners) {
            l.onCameraChange(bearing)
        }
    }

    protected var located = false
    protected var dontMove = false

    abstract fun moveMap(lat: Double, lng: Double, zoom: Float)
    abstract fun moveMap(lat: Double, lng: Double)
    abstract fun fixMap()

    abstract fun clear()
    abstract fun remove(name: String)
    abstract fun findBlock(pt: GeoHelper.LatLng): List<String>

    abstract fun updateEdge(name: String, hasEdge: Boolean)
    abstract fun drawMarker(name: String, lat: Double, lng: Double, resourceId: Int, z: Int, anchor: Int = Params.ANCHOR_CENTER)
    abstract fun drawLetterMarker(name: String, lat: Double, lng: Double, letter: String, color: Int)

    abstract fun highlightLetterMarker(name: String, letter: String, color: Int, highlight: Boolean)
    abstract fun highlightBlock(name: String, highlight: Boolean)

    abstract fun drawPoints(pts: List<MapRing>, color: List<Int>)
    abstract fun removePoints()

    abstract fun fit()
    abstract fun fit(names: List<String>)
    abstract fun toLatLng(x: Int, y: Int): GeoHelper.LatLng
    abstract fun toPoint(lat: Double, lng: Double): Point2D
    abstract fun screenshot(file: File, complete: () -> Unit, edit: ((Bitmap) -> Unit)? = null)

    abstract val angle: Float
    abstract val centerPoint: GeoHelper.LatLng?
    abstract val boundingBox: LatLngBound

    protected abstract fun drawPolygon(name: String, block: MapRing, strokeColor: Int, width: Float, fillColor: Int, z: Int)
    protected abstract fun drawPolygonWithHoles(name: String, block: MapBlock, strokeColor: Int, width: Float, fillColor: Int, z: Int)
    protected abstract fun drawLines(name: String, pts: MapTrack, color: Int, width: Float, z: Int, isRing: Boolean = false)
    protected abstract fun drawCircle(name: String, c: GeoHelper.LatLng, r: Float, strokeColor: Int, width: Float, fillColor: Int, z: Int)

    protected val objMap = mutableMapOf<String, Any>()

    private var numbers = 0
    fun drawNumberMarker(pts: List<GeoHelper.LatLng>) {
        for ((idx, pt) in pts.withIndex()) {
            drawLetterMarker("____$idx", pt.latitude, pt.longitude, (idx+1).toString(),
                Params.MARKER_OTHER_COLOR
            )
        }
        for (idx in pts.size until numbers) {
            remove("____$idx")
        }
        numbers = pts.size
    }

    fun clearNumberMarker() {
        for (idx in 0 until numbers) {
            remove("____$idx")
        }
        numbers = 0
    }

    fun indexOfNumberMarker(name: String): Int {
        try {
            if (name.startsWith("____")) {
                val str = name.substring(4)
                return str.toInt()
            }
            return -1
        } catch (e: NumberFormatException) {
            return -1
        }
    }

    fun nameOfNumberMarker(index: Int): String {
        return "____$index"
    }

    private fun getMarkView(title: String, color: Int): Bitmap {
        val paint = Paint()
        val w = context.dp2px(24f).toFloat()
        val h = w * 1.207
        val bitmap = Bitmap.createBitmap(w.toInt(), h.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        paint.style = Paint.Style.FILL
        paint.color = color
        val path = Path()
        val dx = w / 2f * 0.707f
        path.moveTo(w/2f, h.toFloat())
        path.lineTo(w/2f-dx, w/2f+dx)
        path.arcTo(0f, 0f, w, w, 135f, 270f, false)
        path.close()
        canvas.drawPath(path, paint)
        if (title.isNotBlank()) {
            paint.textSize = w * 0.6f
            paint.color = Color.WHITE
            val bounds = Rect()
            paint.getTextBounds(title, 0, title.length, bounds)
            canvas.drawText(title, (w - bounds.width()) / 2f - bounds.left, (w + bounds.height()) / 2f, paint)
        }
        return bitmap
    }

    protected fun saveScreenshot(bitmap: Bitmap, file: File): Boolean {
        var success = false
        try {
            val fos = FileOutputStream(file)
            success = bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return success
    }

    fun drawBlock(
        name: String, block: MapRing, hasEdge: Boolean,
        color: Int = Params.BLOCK_FILL_COLOR,
        strokeColor: Int = Params.BLOCK_STROKE_COLOR,
        strokeWidth: Float = Params.BLOCK_WIDTH
    ) {
        val width = if (hasEdge) strokeWidth else 0f
        drawPolygon(name, block, strokeColor, width, color, Z_BLOCK)
    }

    fun drawForbiddenZone(name: String, block: MapRing) {
        drawPolygon(name, block,
            Params.ZONE_STROKE_COLOR,
            Params.BLOCK_WIDTH,
            Params.ZONE_FILL_COLOR, Z_ZONE
        )
    }

    fun drawBlockWithHoles(name: String, block: MapBlock) {
        drawPolygonWithHoles(name, block,
            Params.BLOCK_STROKE_COLOR,
            Params.BLOCK_WIDTH,
            Params.BLOCK_FILL_COLOR, Z_BLOCK
        )
    }

    fun drawBarrier(name: String, barrier: MapRing, hasEdge: Boolean) {
        val width = if (hasEdge) Params.BLOCK_WIDTH else 0f
        drawPolygon(name, barrier,
            Params.BLOCK_STROKE_COLOR, width,
            Params.BARRIER_FILL_COLOR, Z_BARRIER
        )
    }

    fun drawEdge(name: String, pts: MapTrack) {
        drawLines(name, pts, Params.BLOCK_STROKE_COLOR, Params.BLOCK_WIDTH, Z_LINE)
    }

    fun drawTrack(name: String, pts: MapTrack) {
        drawLines(name, pts, Params.TRACK_COLOR, Params.TRACK_WIDTH, Z_LINE)
    }

    fun drawRing(name: String, pts: MapRing, color: Int) {
        drawLines(name, pts, color, 2f, Z_LINE, true)
    }

    fun drawPolyline(name: String, pts: MapTrack, color: Int, width: Float) {
        drawLines(name, pts, color, width, Z_LINE)
    }

    fun drawCompletion(name: String, pts: MapTrack) {
        drawLines(name, pts, Params.COMPLETION_COLOR, Params.COMPLETION_WIDTH, Z_HL_LINE)
    }

    fun drawLine(name: String, pts: MapTrack, color: Int, z: Int = Z_LINE, width: Float = Params.TRACK_WIDTH) {
        drawLines(name, pts, color, width, z)
    }

    fun drawCircle(name: String, c: GeoHelper.LatLng, r: Float) {
        val width = Params.BLOCK_WIDTH
        drawCircle(name, c, r, Params.BLOCK_STROKE_COLOR, width, Params.BLOCK_FILL_COLOR, Z_BLOCK)
    }

    fun drawAngleLine(name: String, pts: MapTrack, color: Int) {
        drawLines(name, pts, color, Params.COMPLETION_WIDTH, Z_HL_LINE)
    }

    companion object {
        const val Z_BLOCK = 10
        const val Z_BARRIER = 30
        const val Z_HL_BLOCK = 20
        const val Z_ZONE = 5
        const val Z_LINE = 100
        const val Z_HL_LINE = 110
        const val Z_MARKER = 200
        const val Z_HL_MARKER = 210

        fun applyTheme(
            mclr: Int, hlclr: Int, // marker
            bclr: Int, bWidth: Float, // block
            hbclr: Int, // highlight block
        ) {
            Params.MARKER_OTHER_COLOR = mclr
            Params.MARKER_HL_COLOR = hlclr

            Params.BLOCK_FILL_COLOR = bclr
            Params.BLOCK_WIDTH = bWidth

            Params.BLOCK_HL_FILL_COLOR = hbclr
        }
    }

    object Params {
        val TRACK_WIDTH = 3f
        val TRACK_COLOR = Color.RED
        val COMPLETION_WIDTH = 6f
        val COMPLETION_COLOR = Color.GREEN

        var MARKER_HL_COLOR = 0x7FFF0000
        val MARKER_CALIB_COLOR = 0x7F00FF00
        var MARKER_OTHER_COLOR = 0x7f00bfff
        var MARKER_AUX_COLOR = 0xFFFF9800

        var BLOCK_WIDTH = 2f
        val BLOCK_STROKE_COLOR = Color.WHITE
        var BLOCK_FILL_COLOR = 0x7F4B7DFF
        val BLOCK_HL_STROKE_COLOR = Color.rgb(230, 20, 20)
        var BLOCK_HL_FILL_COLOR = Color.argb(200, 254, 168, 91)

        val ZONE_STROKE_COLOR = Color.YELLOW
        val ZONE_FILL_COLOR = Color.argb(200, 200, 200, 0)

        val BARRIER_FILL_COLOR = -0x7f01c7e3

        const val ANCHOR_CENTER = 0
        const val ANCHOR_BOTTOM_CENTER = 1
    }

    protected fun onCreate(savedState: Bundle?) {
        savedState?.apply { dontMove = getBoolean("noMove") }
    }

    // lifecycle
    open fun onStart() {}
    open fun onStop() {}
    open fun onResume() {}
    open fun onPause() {}
    open fun onDestroy() {}
    open fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("noMove", dontMove)
    }
}
