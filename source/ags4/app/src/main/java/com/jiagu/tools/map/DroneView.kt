package com.jiagu.tools.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.jiagu.api.ext.dp2px
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.math.Point2D
import com.jiagu.tools.ext.UnitHelper
import java.util.Locale

class DroneView : View, IMapCanvas.MapChangeListener {

    override fun onCameraChange(bearing: Float) {
        this.bearing = bearing
        invalidate()
    }

    private class GeoText(val lat: Double, val lng: Double, val text: String)

    private var openEnhanced = false
    private var enhancedYaw: Int? = null
    private val paintFill = Paint()
    private val paintStroke = Paint()
    private val paintText = Paint()
    private val paintDashLine = Paint()
    private var fanHeight = 0f
    private val drone = Path()
    private var bearing = 0f
    private var dronePosition: GeoHelper.LatLng? = null
    private val textList = mutableListOf<GeoText>()
    private val doseList = mutableListOf<GeoText>()
    private var droneYaw = 20f
    private var droneAcc: Short = 5
    private var fan1Status = 0
    private var fan2Status = 0
    private var fan1Dist = 20f
    private var fan2Dist = 10f
    private var locator = false

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private fun init(context: Context) {
        paintFill.style = Paint.Style.FILL
        paintFill.isAntiAlias = true
        paintStroke.style = Paint.Style.STROKE
        paintStroke.isAntiAlias = true
        paintStroke.strokeWidth = 10f
        fanHeight = context.dp2px(30f).toFloat()
        paintText.color = Color.WHITE

        paintDashLine.color = Color.YELLOW
        paintDashLine.isAntiAlias = true
        paintDashLine.strokeWidth = 5f
        paintDashLine.style = Paint.Style.STROKE
        paintDashLine.pathEffect = DashPathEffect(floatArrayOf(10f, 15f), 0f)

        val h = fanHeight / 6
        drone.moveTo(0f, -h)
        drone.lineTo(-h * 0.8f, h)
        drone.lineTo(0f, h * 0.667f)
        drone.lineTo(h * 0.8f, h)
        drone.close()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paintText.textSize = 28f
        for (t in textList) {
            drawText(canvas, t)
        }
        for (t in doseList) {
            drawText(canvas, t)
        }
        if (showDashLine) drawDashLine(canvas)
        dronePosition?.let { drawDrone(canvas, it) }
        if (locator) drawLocator(canvas)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        invalidate()
    }

    private fun pointDistance(a: Point2D, b: Point2D): Double {
        val dx = (a.x - b.x)
        val dy = (a.y - b.y)
        return Math.sqrt(dx * dx + dy * dy)
    }

    private fun drawLocator(canvas: Canvas) {
        val cx = measuredWidth / 2f
        val cy = measuredHeight / 2f
        val w = context.dp2px(24f).toFloat()
        val h = w * 1.207f
        paintFill.color = Color.GREEN
        val path = Path()
        val dx = w / 2f * 0.707f
        path.moveTo(cx, cy)
        path.lineTo(cx - dx, cy - dx)
        val left = cx - w / 2
        val top = cy - h
        path.arcTo(left, top, left + w, top + w, 135f, 270f, false)
        path.close()
        canvas.drawPath(path, paintFill)
    }

    private fun drawText(canvas: Canvas, text: GeoText) {
        mapCanvas?.apply {
            val pt = toPoint(text.lat, text.lng)
            paintStroke.color = Color.WHITE
//            val bounds = Rect()
//            paintText.getTextBounds(text.text, 0, text.text.length, bounds)
//            canvas.drawText(text.text, pt.x - bounds.width() / 2f - bounds.left, pt.y + bounds.height() / 2f, paintText)
            canvas.drawText(text.text, pt.x.toFloat(), pt.y.toFloat(), paintText)
        }
    }

    private fun drawDrone(canvas: Canvas, pos: GeoHelper.LatLng) {
        mapCanvas?.apply {
            val pt = toPoint(pos.latitude, pos.longitude)

            canvas.save()
            canvas.translate(pt.x.toFloat(), pt.y.toFloat())
            canvas.rotate(-bearing + droneYaw)

            drawFan(canvas, fan1Status, true)
            drawFan(canvas, fan2Status, false)

            paintStroke.color = Color.BLUE
            canvas.drawPath(drone, paintStroke)
            paintFill.color = if (droneAcc >= 4) Color.GREEN else Color.RED
            canvas.drawPath(drone, paintFill)

            canvas.restore()

            if(openEnhanced && enhancedYaw != null) enhancedMode(canvas, pt, enhancedYaw!!)
        }
    }

    //增强模式
    private fun enhancedMode(canvas: Canvas, pt: Point2D, yaw: Int) {
        canvas.save()
        canvas.translate(pt.x.toFloat(), pt.y.toFloat())
        canvas.rotate(-bearing + yaw)
        // 新增半圆绘制（先填充后描边）
        paintFill.style = Paint.Style.FILL
        // 前侧半圆填充色（浅蓝色带透明度）
        paintFill.color = Color.argb(120, 0, 128, 255)
        canvas.drawArc(-fanHeight, -fanHeight, fanHeight, fanHeight, 270f, 180f, false, paintFill)

        // 后侧半圆填充色（浅橘黄色带透明度）
        paintFill.color = Color.argb(120, 255, 128, 0)
        canvas.drawArc(-fanHeight, -fanHeight, fanHeight, fanHeight, 90f, 180f, false, paintFill)
        canvas.restore()
    }


    private fun drawFan(canvas: Canvas, status: Int, front: Boolean) {
        if ((status and 1) != 0) {
            var color = Color.argb(80, 0, 0, 0)
            when {
                (status and 8) != 0 -> color = Color.argb(100, 200, 0, 0)
                (status and 4) != 0 -> color = Color.argb(100, 200, 200, 0)
                (status and 2) != 0 -> color = Color.argb(100, 0, 200, 0)
            }
            val start = if (front) 210f else 30f
            paintFill.color = color
            canvas.drawArc(
                -fanHeight,
                -fanHeight,
                fanHeight,
                fanHeight,
                start,
                120f,
                true,
                paintFill
            )
        }
    }

    private var mapCanvas: IMapCanvas? = null
    fun setMapCanvas(canvas: IMapCanvas) {
        mapCanvas = canvas
        canvas.addChangeListener(this)
        bearing = canvas.angle
    }

    fun setDronePosition(pt: GeoHelper.LatLng) {
        dronePosition = pt
        invalidate()
    }

    fun setProperties(angle: Float, accuracy: Short) {
        droneYaw = angle
        droneAcc = accuracy
        invalidate()
    }

    fun setDroneState(fan1: Int, fan2: Int, dist1: Float, dist2: Float) {
        fan1Status = fan1
        fan2Status = fan2
        fan1Dist = dist1
        fan2Dist = dist2
        invalidate()
    }

    fun clear() {
        dronePosition = null
        invalidate()
    }

    fun drawDistance(pts: List<GeoHelper.LatLng>, closed: Boolean) {
        textList.clear()
        if (pts.size >= 2) {
            var (o, start) = if (closed && pts.size > 2) pts.last() to 0 else pts.first() to 1
            for (i in start until pts.size) {
                val p = pts[i]
                addDistance(o, p)
                o = p
            }
        }
        invalidate()
    }

    private fun addDistance(o: GeoHelper.LatLng, p: GeoHelper.LatLng) {
        val dist = GeoHelper.distanceVincenty(o.latitude, o.longitude, p.latitude, p.longitude)
        if (dist < 0) return
        val lat = (o.latitude + p.latitude) / 2
        val lng = (o.longitude + p.longitude) / 2
//        textList.add(GeoText(lat, lng, "${dist.toString(0)}m"))
        textList.add(
            GeoText(
                lat,
                lng,
                UnitHelper.transLength(dist.toFloat()) + UnitHelper.lengthUnit()
            )
        )
    }

    fun clearText() {
        textList.clear()
        doseList.clear()
        invalidate()
    }

    fun drawEdge(pts: List<GeoHelper.LatLng>, closed: Boolean) {
        textList.clear()
        if (pts.size >= 2) {
            for (start in 0 until pts.size) {
                var end = start + 1
                if (end > pts.size - 1 && closed) {
                    end = 0
                    addEdge(pts[start], pts[end], start)
                } else {
                    addEdge(pts[start], pts[end], start)
                }
            }
        }
        invalidate()
    }

    private fun addEdge(o: GeoHelper.LatLng, p: GeoHelper.LatLng, i: Int) {
        val dist = GeoHelper.distanceVincenty(o.latitude, o.longitude, p.latitude, p.longitude)
        if (dist < 1) return
        val lat = (o.latitude + p.latitude) / 2
        val lng = (o.longitude + p.longitude) / 2
        textList.add(GeoText(lat, lng, "E${i + 1}"))
    }

    fun showLocator(show: Boolean) {
        if (locator != show) {
            locator = show
            invalidate()
        }
    }

    private var showDashLine = false
    private var homePosition: GeoHelper.LatLng? = null
    fun setDashLine(h: GeoHelper.LatLng? = null) {
        if (h == null || dronePosition == null) {
            showDashLine = false
        } else {
            homePosition = h
            showDashLine = true
        }
        invalidate()
    }

    private fun drawDashLine(canvas: Canvas) {
        if (homePosition == null || dronePosition == null) return
        mapCanvas?.apply {
            val p1 = toPoint(homePosition!!.latitude, homePosition!!.longitude)
            val p2 = toPoint(dronePosition!!.latitude, dronePosition!!.longitude)
            val path = Path()
            path.moveTo(p1.x.toFloat(), p1.y.toFloat())
            path.lineTo(p2.x.toFloat(), p2.y.toFloat())
            canvas.drawPath(path, paintDashLine)
        }
    }

    class DoseText(val dose: Double, val data: List<GeoHelper.LatLng>)

    fun drawDose(data: List<DoseText>) {
//        doseList.clear()
        for (d in data) {
            val avgLat = getAverageLatLng(d.data)
            avgLat?.let { p ->
                doseList.add(GeoText(p.latitude, p.longitude, String.format(Locale.US, "%.1f", d.dose / 1000.0)))
            }
        }

        invalidate()
    }

    fun clearDoseText() {
        doseList.clear()
    }

    fun getAverageLatLng(pts: List<GeoHelper.LatLng>): GeoHelper.LatLng? {
        if (pts.isEmpty()) return null
        val avgLat = pts.sumOf { it.latitude } / pts.size
        val avgLng = pts.sumOf { it.longitude } / pts.size
        return GeoHelper.LatLng(avgLat, avgLng)
    }

    //打开m+模式
    fun setOpenEnhanced(isOpen: Boolean) {
        openEnhanced = isOpen
        invalidate()
    }

    fun setEnhanceYaw(yaw: Int?) {
        enhancedYaw = yaw
        invalidate()
    }

}
