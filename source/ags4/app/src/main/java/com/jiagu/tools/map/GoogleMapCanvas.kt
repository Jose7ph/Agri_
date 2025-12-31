package com.jiagu.tools.map

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Point
import android.location.Location
import android.os.Bundle
import android.widget.FrameLayout
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.jiagu.api.helper.GeoHelper
import com.jiagu.api.math.Point2D
import com.jiagu.api.math.Polygon2D
import com.jiagu.api.model.MapBlock
import com.jiagu.api.model.MapRing
import com.jiagu.api.model.MapTrack
import java.io.File

class GoogleMapCanvas(container: FrameLayout, private val autoLocate: Boolean, state: Bundle?, l: MapReadyListener) :
    IMapCanvas(container.context!!), OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMyLocationChangeListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnMarkerDragListener {

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap) {
        gmap = map
        map.mapType = GoogleMap.MAP_TYPE_HYBRID
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isMyLocationButtonEnabled = false
        map.setOnCameraMoveListener(this@GoogleMapCanvas)
        map.setOnMapClickListener(this@GoogleMapCanvas)
        map.setOnMapLongClickListener(this@GoogleMapCanvas)
        map.setOnMarkerClickListener(this@GoogleMapCanvas)
        map.setOnMarkerDragListener(this@GoogleMapCanvas)
        if (autoLocate) {
            map.isMyLocationEnabled = true
            map.setOnMyLocationChangeListener(this@GoogleMapCanvas)
        }
//        val option = TileOverlayOptions().tileProvider(GoogleMapOfflineTileProvider(context)).zIndex(0f)
//        addTileOverlay(option)
        gmap?.setOnMapLoadedCallback {
            mapReady()
            for (b in deferred) {
                b(map)
            }
            deferred.clear()
        }
    }

    override fun onCameraMove() {
        gmap?.apply {
            cameraChange(cameraPosition.bearing)
        }
    }

    override fun onMapClick(location: LatLng) {
        mapClicked(GeoHelper.LatLng(location.latitude, location.longitude))
    }

    override fun onMapLongClick(location: LatLng) {
        mapLongClicked(GeoHelper.LatLng(location.latitude, location.longitude))
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val name = marker.tag as String?
        if (name != null && !name.startsWith("--")) {
            markerSelected(name)
        }
        return true
    }

    override fun onMyLocationChange(location: Location) {
        gmap?.apply {
            if (!located) {
                if (!dontMove) {
                    animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f))
                    dontMove = true
                }
                located = true
            }
            phoneLocation.value = GeoHelper.LatLng(location.latitude, location.longitude)
        }
    }

    override fun onMarkerDrag(marker: Marker) {
        val name = marker.tag as String?
        if (name != null && !name.startsWith("--")) {
            markerDragging(name, GeoHelper.LatLng(marker.position.latitude, marker.position.longitude))
        }
    }

    override fun onMarkerDragEnd(marker: Marker) {
        val name = marker.tag as String?
        if (name != null && !name.startsWith("--")) {
            markerDragFinished(name, GeoHelper.LatLng(marker.position.latitude, marker.position.longitude))
        }
    }

    override fun onMarkerDragStart(marker: Marker) {
        val name = marker.tag as String?
        if (name != null && !name.startsWith("--")) {
            markerDragStart(name, GeoHelper.LatLng(marker.position.latitude, marker.position.longitude))
        }
    }

    private val view = MapView(context)
    private var gmap: GoogleMap? = null
    private val boundMap = mutableMapOf<String, LatLngBounds>()
    init {
        super.onCreate(state)
        setMapReadyListener(l)
        view.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        container.addView(view)
        view.onCreate(state)
        view.getMapAsync(this)
    }

    private val deferred = mutableListOf<(GoogleMap) -> Unit>()
    private fun execute(block: (GoogleMap) -> Unit) {
        if (gmap == null) deferred.add(block)
        else block(gmap!!)
    }

    private fun moveMap(camera: CameraUpdate) {
        located = true
        gmap?.animateCamera(camera)
    }

    override fun moveMap(lat: Double, lng: Double, zoom: Float) {
        execute {
            located = true
            it.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), zoom))
        }
    }

    override fun moveMap(lat: Double, lng: Double) {
        execute {
            located = true
            it.animateCamera(CameraUpdateFactory.newLatLng(LatLng(lat, lng)))
        }
    }

    override fun fixMap() {
        gmap?.uiSettings?.apply {
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
        }
    }

    override fun clear() {
        clearNumberMarker()
        for (pair in objMap) {
            when (val obj = pair.value) {
                is Polygon -> obj.remove()
                is Polyline -> obj.remove()
                is Circle -> obj.remove()
                is Marker -> obj.remove()
            }
        }
        objMap.clear()
        boundMap.clear()
    }

    override fun remove(name: String) {
        when (val obj = objMap[name]) {
            is Polygon -> obj.remove()
            is Polyline -> obj.remove()
            is Circle -> obj.remove()
            is Marker -> obj.remove()
        }
        objMap.remove(name)
        boundMap.remove(name)
    }

    private fun convertRing(pts: MapRing): MutableList<LatLng> {
        val list = mutableListOf<LatLng>()
        for (pt in pts) {
            list.add(LatLng(pt.latitude, pt.longitude))
        }
        return list
    }

    private fun convertBlock(poly: MapBlock): List<List<LatLng>> {
        val list = mutableListOf<List<LatLng>>()
        for (pts in poly) {
            val coords = convertRing(pts)
            list.add(coords)
        }
        return list
    }

    private fun buildBound(pts: List<LatLng>): LatLngBounds {
        val builder = LatLngBounds.Builder()
        pts.forEach { builder.include(it) }
        return builder.build()
    }

    override fun drawPolygon(name: String, block: MapRing, strokeColor: Int, width: Float, fillColor: Int, z: Int) {
        if (block.size < 3) {
            remove(name)
            return
        }
        val pts = convertRing(block)
        execute {
            val poly = objMap[name]
            if (poly != null && poly is Polygon) {
                poly.points = pts
                poly.strokeWidth = width
            } else {
                val options = PolygonOptions()
                    .strokeWidth(width)
                    .strokeColor(strokeColor)
                    .fillColor(fillColor)
                    .addAll(pts)
                    .zIndex(z.toFloat())
                objMap[name] = it.addPolygon(options)
            }
        }
        boundMap[name] = buildBound(pts)
    }

    override fun drawPolygonWithHoles(name: String, block: MapBlock, strokeColor: Int, width: Float, fillColor: Int, z: Int) {
        val blk = convertBlock(block)
        execute {
            val poly = objMap[name]
            if (poly != null && poly is Polygon) {
                val holes = mutableListOf<List<LatLng>>()
                for ((i, v) in blk.withIndex()) {
                    if (i == 0) poly.points = v
                    else if (v.size >= 3) holes.add(v)
                }
                if (holes.isNotEmpty()) poly.holes = holes
            } else {
                val options = PolygonOptions()
                    .strokeWidth(width)
                    .strokeColor(strokeColor)
                    .fillColor(fillColor)
                    .addAll(blk[0])
                    .zIndex(z.toFloat())
                for ((i, v) in blk.withIndex()) {
                    if (i > 0 && v.size >= 3) {
                        options.addHole(v)
                    }
                }
                objMap[name] = it.addPolygon(options)
            }
        }
        boundMap[name] = buildBound(blk[0])
    }

    override fun updateEdge(name: String, hasEdge: Boolean) {
        val poly = objMap[name] as Polygon
        poly.strokeWidth = if (hasEdge) Params.BLOCK_WIDTH else 0f
    }

    private fun drawMarker(
        name: String, lat: Double, lng: Double, angle: Float?, z: Int, anchor: Int?,
        updateIcon: Boolean, block: () -> BitmapDescriptor) {
        execute {
            val marker = objMap[name]
            val pos = LatLng(lat, lng)
            if (marker != null && marker is Marker) {
                marker.position = pos
                angle?.let { marker.rotation = it }
                marker.zIndex = z.toFloat()
                if (updateIcon) marker.setIcon(block())
            } else {
                val option = MarkerOptions()
                    .position(pos)
                    .icon(block())
                    .zIndex(z.toFloat())
                    .draggable(isDragEnable)
                if (anchor != null) {
                    when (anchor) {
                        Params.ANCHOR_CENTER -> option.anchor(0.5f, 0.5f)
                        Params.ANCHOR_BOTTOM_CENTER -> option.anchor(0.5f, 1f)
                    }
                }
                angle?.let { option.rotation(it) }
                val mark = it.addMarker(option) ?: return@execute
                mark.tag = name
                objMap[name] = mark
            }
        }
    }

    override fun drawMarker(name: String, lat: Double, lng: Double, resourceId: Int, z: Int, anchor: Int) {
        drawMarker(name, lat, lng, null, z, Params.ANCHOR_CENTER, true) {
            BitmapDescriptorFactory.fromResource(resourceId)
        }
    }

    override fun drawLetterMarker(name: String, lat: Double, lng: Double, letter: String, color: Int) {
        drawMarker(name, lat, lng, null, Z_MARKER, markerOption.anchor, false) {
            BitmapDescriptorFactory.fromBitmap(markerOption.draw(letter, color))
        }
    }

    override fun highlightLetterMarker(name: String, letter: String, color: Int, highlight: Boolean) {
        val marker = objMap[name] ?: return
        if (marker is Marker) {
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(markerOption.draw(letter, color)))
            marker.zIndex = if (highlight) Z_HL_MARKER.toFloat() else Z_MARKER.toFloat()
        }
    }

    private fun ptInBlock(pt: GeoHelper.LatLng, block: List<LatLng>): Boolean {
        val poly = mutableListOf<Point2D>()
        for (p in block) {
            poly.add(Point2D(p.longitude, p.latitude))
        }
        return Polygon2D(poly).contains(Point2D(pt.longitude, pt.latitude))
    }

    private fun ptInCircle(converter: GeoHelper.GeoCoordConverter, pt: Point2D, circle: Circle): Boolean {
        val center = circle.center
        val c = converter.convertLatLng(center.latitude, center.longitude)
        return pt.distance(c) < circle.radius
    }

    override fun findBlock(pt: GeoHelper.LatLng): List<String> {
        val out = mutableListOf<String>()
        val converter = GeoHelper.GeoCoordConverter()
        for ((k, b) in objMap) {
            val inside = when (b) {
                is Polygon -> ptInBlock(pt, b.points)
                is Circle -> {
                    val p = converter.convertLatLng(pt.latitude, pt.longitude)
                    ptInCircle(converter, p, b)
                }
                else -> false
            }
            if (inside) out.add(k)
        }
        return out
    }

    override fun highlightBlock(name: String, highlight: Boolean) {
        when (val block = objMap[name]) {
            is Polygon -> {
                block.fillColor = if (highlight) Params.BLOCK_HL_FILL_COLOR else Params.BLOCK_FILL_COLOR
                block.strokeColor = if (highlight) Params.BLOCK_HL_STROKE_COLOR else Params.BLOCK_STROKE_COLOR
                block.zIndex = if (highlight) Z_HL_BLOCK.toFloat() else Z_BLOCK.toFloat()
            }
            is Circle -> {
                block.fillColor = if (highlight) Params.BLOCK_HL_FILL_COLOR else Params.BLOCK_FILL_COLOR
                block.strokeColor = if (highlight) Params.BLOCK_HL_STROKE_COLOR else Params.BLOCK_STROKE_COLOR
                block.zIndex = if (highlight) Z_HL_BLOCK.toFloat() else Z_BLOCK.toFloat()
            }
        }
    }

    override fun drawPoints(pts: List<MapRing>, color: List<Int>) {
        TODO("Not yet implemented")
    }

    override fun removePoints() {
        TODO("Not yet implemented")
    }

    override fun drawLines(name: String, pts: MapTrack, color: Int, width: Float, z: Int, isRing: Boolean) {
        if (pts.size < 2) {
            remove(name)
            return
        }
        val coords = convertRing(pts)
        execute {
            if (isRing) coords.add(coords[0])
            val lines = objMap[name]
            if (lines != null && lines is Polyline) {
                lines.points = coords
                lines.width = width
                lines.color = color
                lines.zIndex = z.toFloat()
            } else {
                val options = PolylineOptions()
                    .zIndex(z.toFloat())
                    .width(width)
                    .color(color)
                    .addAll(coords)
                objMap[name] = it.addPolyline(options)
            }
        }
        boundMap[name] = buildBound(coords)
    }

    private fun buildCircleBound(c: LatLng, r: Double): LatLngBounds {
        val builder = LatLngBounds.Builder()
        val converter = GeoHelper.GeoCoordConverter(c.latitude, c.longitude)
        val lb = converter.convertPoint(-r, -r)
        val rt = converter.convertPoint(r, r)
        builder.include(LatLng(lb.latitude, lb.longitude))
        builder.include(LatLng(rt.latitude, rt.longitude))
        return builder.build()
    }

    override fun drawCircle(name: String, c: GeoHelper.LatLng, r: Float, strokeColor: Int, width: Float, fillColor: Int, z: Int) {
        val pt = LatLng(c.latitude, c.longitude)
        val circle = objMap[name]
        if (circle != null && circle is Circle) {
            circle.center = pt
            circle.radius = r.toDouble()
        } else {
            val options = CircleOptions()
                .zIndex(z.toFloat())
                .center(pt)
                .radius(r.toDouble())
                .strokeColor(strokeColor)
                .strokeWidth(width)
                .fillColor(fillColor)
            execute { objMap[name] = it.addCircle(options) }
        }
        boundMap[name] = buildCircleBound(pt, r.toDouble())
    }

    override fun fit() {
        execute {
            val builder = LatLngBounds.Builder()
            var count = 0
            for ((k, v) in objMap) {
                when (v) {
                    is Polygon, is Polyline, is Circle -> {
                        boundMap[k]?.apply {
                            builder.include(northeast)
                            builder.include(southwest)
                        }
                    }
                    is Marker -> builder.include(v.position)
                    else -> count--
                }
                count++
            }
            if (count > 0) {
                val update = CameraUpdateFactory.newLatLngBounds(builder.build(), 50)
                moveMap(update)
            }
        }
    }

    override fun fit(names: List<String>) {
        execute {
            val builder = LatLngBounds.Builder()
            var count = 0
            for (name in names) {
                when (objMap[name]) {
                    is Polygon, is Polyline, is Circle -> {
                        boundMap[name]?.apply {
                            builder.include(northeast)
                            builder.include(southwest)
                        }
                    }
                    else -> count--
                }
                count++
            }
            if (count > 0) {
                val update = CameraUpdateFactory.newLatLngBounds(builder.build(), 100)
                moveMap(update)
            }
        }
    }

    override val angle: Float
        get() = gmap?.cameraPosition?.bearing ?: 0f

    override val centerPoint: GeoHelper.LatLng?
        get() = gmap?.let { GeoHelper.LatLng(it.cameraPosition.target.latitude, it.cameraPosition.target.longitude) }

    override val boundingBox: LatLngBound
        get() {
            val b = gmap ?: return LatLngBound(0.0, 0.0, 0.0, 0.0)
            val nw = b.projection.fromScreenLocation(Point(0, 0))
            val se = b.projection.fromScreenLocation(Point(view.width, view.height))
            return LatLngBound(nw.latitude, se.longitude, se.latitude, nw.longitude)
        }

    override fun toLatLng(x: Int, y: Int): GeoHelper.LatLng {
        val p = gmap ?: return GeoHelper.LatLng(0.0, 0.0)
        val pt = p.projection.fromScreenLocation(Point(x, y))
        return GeoHelper.LatLng(pt.latitude, pt.longitude)
    }

    override fun toPoint(lat: Double, lng: Double): Point2D {
        val p = gmap ?: return Point2D(-1000.0, -1000.0)
        val pt = p.projection.toScreenLocation(LatLng(lat, lng))
        return Point2D(pt.x.toDouble(), pt.y.toDouble())
    }

    override fun screenshot(file: File, complete: () -> Unit, edit: ((Bitmap) -> Unit)?) {
        gmap?.apply {
            snapshot { bmp ->
                bmp?.let {
                    edit?.invoke(it)
                    saveScreenshot(it, file)
                    complete()
                }
            }
        }
    }

    override fun onResume() { view.onResume() }
    override fun onStart() { view.onStart() }
    override fun onStop() { view.onStop() }
    override fun onPause() { view.onPause() }
    override fun onDestroy() { view.onDestroy() }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        view.onSaveInstanceState(outState)
    }
}
