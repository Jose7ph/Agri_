//package com.jiagu.tools.map
//
//import android.annotation.SuppressLint
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.graphics.Color
//import android.os.Bundle
//import android.util.Log
//import android.widget.FrameLayout
//import com.google.gson.Gson
//import com.jiagu.api.helper.GeoHelper
//import com.jiagu.api.math.Point2D
//import com.jiagu.api.math.Polygon2D
//import com.jiagu.api.model.MapBlock
//import com.jiagu.api.model.MapRing
//import com.jiagu.api.model.MapTrack
//import com.mapbox.geojson.BoundingBox
//import com.mapbox.geojson.Point
//import com.mapbox.maps.CameraOptions
//import com.mapbox.maps.CoordinateBounds
//import com.mapbox.maps.EdgeInsets
//import com.mapbox.maps.MapInterface
//import com.mapbox.maps.MapView
//import com.mapbox.maps.MapboxMap
//import com.mapbox.maps.ScreenCoordinate
//import com.mapbox.maps.Style
//import com.mapbox.maps.extension.observable.eventdata.CameraChangedEventData
//import com.mapbox.maps.extension.style.StyleContract
//import com.mapbox.maps.extension.style.expressions.dsl.generated.get
//import com.mapbox.maps.extension.style.layers.addLayer
//import com.mapbox.maps.extension.style.layers.generated.circleLayer
//import com.mapbox.maps.extension.style.layers.generated.rasterLayer
//import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
//import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
//import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
//import com.mapbox.maps.extension.style.sources.addSource
//import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
//import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
//import com.mapbox.maps.extension.style.sources.generated.rasterSource
//import com.mapbox.maps.extension.style.sources.getSourceAs
//import com.mapbox.maps.extension.style.style
//import com.mapbox.maps.plugin.animation.flyTo
//import com.mapbox.maps.plugin.annotation.Annotation
//import com.mapbox.maps.plugin.annotation.annotations
//import com.mapbox.maps.plugin.annotation.generated.CircleAnnotation
//import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
//import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationDragListener
//import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
//import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
//import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
//import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotation
//import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationManager
//import com.mapbox.maps.plugin.annotation.generated.PolygonAnnotationOptions
//import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation
//import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
//import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
//import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
//import com.mapbox.maps.plugin.annotation.generated.createPolygonAnnotationManager
//import com.mapbox.maps.plugin.annotation.generated.createPolylineAnnotationManager
//import com.mapbox.maps.plugin.attribution.attribution
//import com.mapbox.maps.plugin.compass.compass
//import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
//import com.mapbox.maps.plugin.gestures.OnMapClickListener
//import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
//import com.mapbox.maps.plugin.gestures.addOnMapClickListener
//import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
//import com.mapbox.maps.plugin.gestures.gestures
//import com.mapbox.maps.plugin.locationcomponent.location
//import com.mapbox.maps.plugin.scalebar.scalebar
//import java.io.File
//import java.lang.reflect.Field
//import java.lang.reflect.Method
//
//class MapboxCanvas(container: FrameLayout, private val autoLocate: Boolean, style: String, state: Bundle?, l: MapReadyListener) :
//    IMapCanvas(container.context!!), OnCameraChangeListener, OnMapClickListener, OnMapLongClickListener,
//    OnPointAnnotationClickListener, OnPointAnnotationDragListener {
//
//    companion object {
//        const val STYLE_GOOGLE = "google"
//        const val STYLE_MAPBOX = "mapbox"
//    }
//
//    private lateinit var mapStyle: Style
//    private lateinit var symbolManager: PointAnnotationManager
//    private lateinit var fillManager: PolygonAnnotationManager
//    private lateinit var lineManager: PolylineAnnotationManager
//
//    private fun buildMapboxStyle(): StyleContract.StyleExtension {
//        // TODO: Change tile url
//        return style(styleUri = Style.SATELLITE_STREETS) {
//            +rasterSource("hi-tile") {
//                tileSize(256)
//                tileSet("", listOf("http://xxxxxx/tile/{z}/{x}/{y}.webp")) {}
//            }
//            +rasterLayer("hi-layer", "hi-tile") {}
//        }
//    }
//
//    private fun buildCustomStyle(url: String): StyleContract.StyleExtension {
//        val strs = url.split("|")
//        return style(styleUri = Style.MAPBOX_STREETS) {
//            +rasterSource("custom-tile") {
//                tileSize(256)
//                tileSet("", listOf(strs[0])) {}
//                if (strs.size > 1) {
//                    maxzoom(strs[1].toLong())
//                }
//            }
//            +rasterLayer("custom-layer", "custom-tile") {}
//        }
//    }
//
//    private fun buildGoogleStyle(): StyleContract.StyleExtension {
//        val index = Math.floor(Math.random() * 4).toInt()
//        val url = "https://khms${index}.google.com/kh/v=979?x={x}&y={y}&z={z}"
//        return style(styleUri = Style.MAPBOX_STREETS) {
//            +rasterSource("google-tile") {
//                tileSize(256)
//                tileSet("", listOf(url)) {}
//            }
//            +rasterLayer("google-layer", "google-tile") {}
//        }
//    }
//
//
//    private fun buildStyle(style: String): StyleContract.StyleExtension {
//        return when (style) {
//            STYLE_GOOGLE -> buildGoogleStyle()
//            STYLE_MAPBOX -> buildMapboxStyle()
//            else -> buildCustomStyle(style)
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun enableMyLocation() {
//        view.location.updateSettings {
//            enabled = true
//            pulsingEnabled = true
//        }
//        view.location.addOnIndicatorPositionChangedListener {
//            if (!located) {
//                if (!dontMove) {
//                    moveMap(it.latitude(), it.longitude(), 15f)
//                    dontMove = true
//                }
//                located = true
//            }
//            phoneLocation.value = GeoHelper.LatLng(it.latitude(), it.longitude())
//        }
//    }
//
//    override fun onCameraChanged(eventData: CameraChangedEventData) {
//        cameraChange(bmap.cameraState.bearing.toFloat())
//    }
//
//    override fun onMapClick(point: Point): Boolean {
//        mapClicked(fromMapboxPoint(point.latitude(), point.longitude()))
//        return false
//    }
//
//    override fun onMapLongClick(point: Point): Boolean {
//        mapLongClicked(fromMapboxPoint(point.latitude(), point.longitude()))
//        return true
//    }
//
//    override fun onAnnotationClick(annotation: PointAnnotation): Boolean {
//        val name = gson.fromJson(annotation.getData(), String::class.java)
//        if (name != null && !name.startsWith("--")) {
//            markerSelected(name)
//        }
//        return true
//    }
//
//    override fun onAnnotationDrag(annotation: Annotation<*>) {
////        val current = System.currentTimeMillis()
////        if (current - dragStartTime < 100) return
//        when (annotation) {
//            is PointAnnotation -> {
//                val name = gson.fromJson(annotation.getData(), String::class.java)
//                if (name != null && !name.startsWith("--")) {
//                    markerDragging(name, fromMapboxPoint(annotation.point.latitude(), annotation.point.longitude()))
//                }
//            }
//            is PolygonAnnotation -> {}
//            is PolylineAnnotation -> {}
//            is CircleAnnotation -> {}
//        }
//    }
//
//    override fun onAnnotationDragFinished(annotation: Annotation<*>) {
////        val current = System.currentTimeMillis()
////        if (current - dragStartTime < 100) return
//        when (annotation) {
//            is PointAnnotation -> {
//                val name = gson.fromJson(annotation.getData(), String::class.java)
//                if (name != null && !name.startsWith("--")) {
//                    markerDragFinished(name, fromMapboxPoint(annotation.point.latitude(), annotation.point.longitude()))
//                }
//            }
//            is PolygonAnnotation -> {}
//            is PolylineAnnotation -> {}
//            is CircleAnnotation -> {}
//        }
//    }
//
//    private var dragStartTime = 0L
//    override fun onAnnotationDragStarted(annotation: Annotation<*>) {
////        dragStartTime = System.currentTimeMillis()
//        when (annotation) {
//            is PointAnnotation -> {
//                val name = gson.fromJson(annotation.getData(), String::class.java)
//                if (name != null && !name.startsWith("--")) {
//                    markerDragStart(name, fromMapboxPoint(annotation.point.latitude(), annotation.point.longitude()))
//                }
//            }
//            is PolygonAnnotation -> {}
//            is PolylineAnnotation -> {}
//            is CircleAnnotation -> {}
//        }
//    }
//
//    private val gson = Gson()
//    private val view = MapView(context)
//    private val bmap: MapboxMap
//    private val boundMap = mutableMapOf<String, BoundingBox>()
//    init {
//        super.onCreate(state)
//        setMapReadyListener(l)
//        view.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
//        container.addView(view)
//        bmap = view.getMapboxMap()
//        bmap.loadStyle(buildStyle(style)) {
//            mapStyle = it
//            mapReady()
//            view.compass.enabled = false
//            it.addSource(geoJsonSource("pts"))
////            bmap.setMaxZoomPreference(21.0)
//            if (autoLocate) {
//                enableMyLocation()
//            }
//            view.scalebar.enabled = false
//            view.attribution.enabled = false
//            fillManager = view.annotations.createPolygonAnnotationManager()
//            lineManager = view.annotations.createPolylineAnnotationManager()
//            lineManager.lineCap = LineCap.ROUND
//            it.addLayer(circleLayer("pts-layer", "pts") {
//                circleRadius(5.0)
//                circleColor(get {literal("color")})
//                circleStrokeWidth(1.0)
//                circleStrokeColor(Color.WHITE)
//            })
//            symbolManager = view.annotations.createPointAnnotationManager()
//            symbolManager.iconAllowOverlap = true
//            symbolManager.addClickListener(this)
//            symbolManager.addDragListener(this)
//            bmap.addOnCameraChangeListener(this)
//            bmap.addOnMapClickListener(this)
//            bmap.addOnMapLongClickListener(this)
//
//            ready = true
//            deferred.forEach { it() }
//            deferred.clear()
//
//            state?.let {
//                val bearing = state.getDouble("bearing")
//                val zoom = state.getDouble("zoom")
//                val pitch = state.getDouble("pitch")
//                val paddingLeft = state.getDouble("padding.left")
//                val paddingRight = state.getDouble("padding.right")
//                val paddingTop = state.getDouble("padding.top")
//                val paddingBottom = state.getDouble("padding.bottom")
//                val centerString = state.getString("center")
//                if (centerString != null) {
//                    val center = Point.fromJson(centerString)
//                    bmap.setCamera(CameraOptions.Builder().center(center).bearing(bearing).zoom(zoom).pitch(pitch)
//                        .padding(EdgeInsets(paddingTop, paddingLeft, paddingBottom, paddingRight)).build())
//                } else {
//                    bmap.setCamera(CameraOptions.Builder().bearing(bearing).zoom(zoom).pitch(pitch)
//                        .padding(EdgeInsets(paddingTop, paddingLeft, paddingBottom, paddingRight)).build())
//                }
//            }
//        }
//    }
//
//    private val deferred = mutableListOf<() -> Unit>()
//    private var ready = false
//    private fun execute(block: () -> Unit) {
//        if (!ready) deferred.add(block)
//        else block()
//    }
//
//    private fun fromMapboxPoint(lat: Double, lng: Double): GeoHelper.LatLng {
//        return GeoHelper.LatLng(lat, lng)
//    }
//
//    private fun moveMap(camera: CameraOptions) {
//        execute {
//            located = true
//            bmap.flyTo(camera)
//        }
//    }
//
//    override fun moveMap(lat: Double, lng: Double, zoom: Float) {
//        val pt = toMapboxPoint(lat, lng)
//        moveMap(CameraOptions.Builder().center(pt).zoom(zoom.toDouble()).build())
//    }
//
//    override fun moveMap(lat: Double, lng: Double) {
//        val pt = toMapboxPoint(lat, lng)
//        moveMap(CameraOptions.Builder().center(pt).build())
//    }
//
//    override fun fixMap() {
//        view.gestures.apply {
//            rotateEnabled = false
//        }
//    }
//
//    override fun clear() {
//        if (objMap.isEmpty()) return
//        fillManager.deleteAll()
//        lineManager.deleteAll()
//        symbolManager.deleteAll()
//        objMap.clear()
//        boundMap.clear()
//    }
//
//    override fun remove(name: String) {
//        when (val obj = objMap[name]) {
//            is PolygonAnnotation -> fillManager.delete(obj)
//            is PolylineAnnotation -> lineManager.delete(obj)
//            is PointAnnotation -> symbolManager.delete(obj)
//        }
//        objMap.remove(name)
//        boundMap.remove(name)
//    }
//
//    private fun toMapboxPoint(lat: Double, lng: Double): Point {
//        return Point.fromLngLat(lng, lat)
//    }
//
//    private fun convertRing(pts: MapRing, close: Boolean): MutableList<Point> {
//        val list = mutableListOf<Point>()
//        for (pt in pts) {
//            val ll = toMapboxPoint(pt.latitude, pt.longitude)
//            list.add(ll)
//        }
//        if (close) {
//            list.add(list[0])
//        }
//        return list
//    }
//
//    private fun convertBlock(poly: MapBlock): List<List<Point>> {
//        val list = mutableListOf<List<Point>>()
//        for (pts in poly) {
//            if (pts.isNotEmpty()) {
//                val coords = convertRing(pts, true)
//                list.add(coords)
//            }
//        }
//        return list
//    }
//
//    private fun buildBound(pts: List<Point>): BoundingBox {
//        var w = pts[0].longitude()
//        var s = pts[0].latitude()
//        var e = pts[0].longitude()
//        var n = pts[0].latitude()
//        pts.forEach {
//            val lat = it.latitude()
//            val lng = it.longitude()
////            Log.d("yuhang", "v: $lat, $lng")
//            if (w > lng) w = lng
//            else if (e < lng) e = lng
//            if (s > lat) s = lat
//            else if (n < lat) n = lat
//        }
//        val box = BoundingBox.fromLngLats(w, s, e, n)
////        Log.d("yuhang", "box: $box")
//        return box
//    }
//
//    override fun drawPolygon(name: String, block: MapRing, strokeColor: Int, width: Float, fillColor: Int, z: Int) {
//        if (block.size < 3) {
//            remove(name)
//            return
//        }
//        val pts = convertRing(block, true)
//        execute {
//            val poly = objMap[name]
//            if (poly != null) {
//                if (poly is PolygonAnnotation) {
//                    poly.points = listOf(pts)
//                    fillManager.update(poly)
//                }
//            } else {
//                val options = PolygonAnnotationOptions()
//                    .withFillOutlineColor(strokeColor)
//                    .withFillColor(fillColor)
//                    .withPoints(listOf(pts))
//                    .withDraggable(false)
//                    .withFillSortKey(z.toDouble())
//                objMap[name] = fillManager.create(options)
//            }
//        }
//        boundMap[name] = buildBound(pts)
//    }
//
//    override fun drawPolygonWithHoles(name: String, block: MapBlock, strokeColor: Int, width: Float, fillColor: Int, z: Int) {
//        val blk = convertBlock(block)
//        execute {
//            val poly = objMap[name]
//            if (poly != null) {
//                if (poly is PolygonAnnotation) {
//                    poly.points = blk
//                    fillManager.update(poly)
//                }
//            } else {
//                val options = PolygonAnnotationOptions()
//                    .withFillOutlineColor(strokeColor)
//                    .withFillColor(fillColor)
//                    .withPoints(blk)
//                    .withDraggable(false)
//                    .withFillSortKey(z.toDouble())
//                objMap[name] = fillManager.create(options)
//            }
//        }
//        if (blk.isNotEmpty()) boundMap[name] = buildBound(blk[0])
//    }
//
//    override fun updateEdge(name: String, hasEdge: Boolean) {
//        val poly = objMap[name] as PolygonAnnotation
//        poly.fillOutlineColorInt = if (hasEdge) Params.BLOCK_STROKE_COLOR else 0
//        fillManager.update(poly)
//    }
//
//    private val textOff = listOf(0.0, -0.6)
//    private fun drawMarker(
//        name: String, lat: Double, lng: Double, text: String, angle: Float?, z: Int, anchor: Int?,
//        updateIcon: Boolean, block: (Style) -> String) {
//        execute {
//            val marker = objMap[name]
//            val pos = toMapboxPoint(lat, lng)
//            if (marker != null) {
//                if (marker is PointAnnotation) {
//                    marker.point = pos
//                    angle?.let { marker.iconRotate = it.toDouble() }
//                    marker.symbolSortKey = z.toDouble()
//                    if (updateIcon) marker.iconImage = block(mapStyle)
//                    symbolManager.update(marker)
//                }
//            } else {
//                val center = when (anchor) {
//                    Params.ANCHOR_BOTTOM_CENTER -> IconAnchor.BOTTOM
//                    else -> IconAnchor.CENTER
//                }
//                val option = PointAnnotationOptions()
//                    .withPoint(pos)
//                    .withIconAnchor(center)
//                    .withData(gson.toJsonTree(name))
//                    .withDraggable(isDragEnable)
//                    .withSymbolSortKey(z.toDouble())
//                if (text.isNotBlank()) {
//                    option.withTextField(text)
//                        .withTextSize(14.0)
//                        .withTextColor(markerOption.textColor)
//                        .apply {
//                            when (markerOption.anchor) {
//                                Params.ANCHOR_CENTER -> withTextAnchor(TextAnchor.CENTER)
//                                Params.ANCHOR_BOTTOM_CENTER -> {
//                                    withTextAnchor(TextAnchor.BOTTOM)
//                                    withTextOffset(textOff)
//                                }
//                            }
//                        }
//                }
//                angle?.let { option.withIconRotate(it.toDouble()) }
//                option.withIconImage(block(mapStyle))
//                objMap[name] = symbolManager.create(option)
//            }
//        }
//    }
//
//    private fun buildMarkerIcon(style: Style, key: String, color: Int): String {
//        val bmp = style.getStyleImage(key)
//        if (bmp == null) {
//            val image = markerOption.draw("", color)
//            style.addImage(key, image)
//        }
//        return key
//    }
//
//    override fun drawMarker(name: String, lat: Double, lng: Double, resourceId: Int, z: Int, anchor: Int) {
//        drawMarker(name, lat, lng, "", null, z, anchor, true) {
//            val key = resourceId.toString()
//            val bmp = it.getStyleImage(key)
//            if (bmp == null) {
//                val image = BitmapFactory.decodeResource(view.resources, resourceId)
//                it.addImage(key, image)
//            }
//            key
//        }
//    }
//
//    override fun drawLetterMarker(name: String, lat: Double, lng: Double, letter: String, color: Int) {
//        drawMarker(name, lat, lng, letter, null, Z_MARKER, markerOption.anchor, false) {
//            buildMarkerIcon(it, "icon-$color", color)
//        }
//    }
//
//    override fun highlightLetterMarker(name: String, letter: String, color: Int, highlight: Boolean) {
//        val marker = objMap[name]
//        if (marker is PointAnnotation) {
//            val key = if (highlight) "icon-h-$color" else "icon-$color"
//            marker.iconImage = buildMarkerIcon(mapStyle, key, color)
//            marker.symbolSortKey = if (highlight) Z_HL_MARKER.toDouble() else Z_MARKER.toDouble()
//            symbolManager.update(marker)
//        }
//    }
//
//    private fun ptInBlock(pt: Point, block: List<Point>): Boolean {
//        val poly = mutableListOf<Point2D>()
//        for (i in 0 until block.size - 1) {
//            poly.add(Point2D(block[i].longitude(), block[i].latitude()))
//        }
//        return Polygon2D(poly).contains(Point2D(pt.longitude(), pt.latitude()))
//    }
//
//    override fun findBlock(pt: GeoHelper.LatLng): List<String> {
//        val p = toMapboxPoint(pt.latitude, pt.longitude)
//        val out = mutableListOf<String>()
//        for ((k, b) in objMap) {
//            if (k.startsWith("!")) continue
//            val inside = when (b) {
//                is PolygonAnnotation -> ptInBlock(p, b.points[0])
//                else -> false
//            }
//            if (inside) out.add(k)
//        }
//        return out
//    }
//
//    override fun highlightBlock(name: String, highlight: Boolean) {
//        val block = objMap[name]
//        if (block is PolygonAnnotation) {
//            if (highlight) {
//                block.fillOutlineColorInt = Params.BLOCK_HL_STROKE_COLOR
//                block.fillColorInt = Params.BLOCK_HL_FILL_COLOR
//                block.fillSortKey = Z_HL_BLOCK.toDouble()
//            } else {
//                block.fillColorInt = Params.BLOCK_FILL_COLOR
//                block.fillOutlineColorInt = Params.BLOCK_STROKE_COLOR
//                block.fillSortKey = Z_BLOCK.toDouble()
//            }
//            fillManager.update(block)
//        }
//    }
//
//    override fun drawLines(name: String, pts: MapTrack, color: Int, width: Float, z: Int, isRing: Boolean) {
//        if (pts.size < 2) {
//            remove(name)
//            return
//        }
//        val coords = convertRing(pts, isRing)
//        execute {
//            val line = objMap[name]
//            if (line != null) {
//                if (line is PolylineAnnotation) {
//                    line.points = coords
//                    line.lineSortKey = z.toDouble()
//                    line.lineColorInt = color
//                    line.lineWidth = width / 2.0
//                    line.lineSortKey = z.toDouble()
//                    lineManager.update(line)
//                }
//            } else {
//                val options = PolylineAnnotationOptions()
//                    .withLineWidth(width / 2.0)
//                    .withLineColor(color)
//                    .withPoints(coords)
//                    .withDraggable(false)
//                    .withLineSortKey(z.toDouble())
//                objMap[name] = lineManager.create(options)
//            }
//        }
//        boundMap[name] = buildBound(coords)
//    }
//
//    private fun buildCircleBound(c: Point, r: Double): BoundingBox {
//        val converter = GeoHelper.GeoCoordConverter(c.latitude(), c.longitude())
//        val lb = converter.convertPoint(-r, -r)
//        val rt = converter.convertPoint(r, r)
//        return BoundingBox.fromLngLats(lb.longitude, lb.latitude, rt.longitude, rt.latitude)
//    }
//
//    override fun drawCircle(name: String, c: GeoHelper.LatLng, r: Float, strokeColor: Int, width: Float, fillColor: Int, z: Int) {
////        val pt = LatLng(c.latitude, c.longitude)
////        val circle = objMap[name]
////        if (circle != null && circle is Circle) {
////            circle.center = pt
////            circle.radius = r.toDouble()
////        } else {
////            val options = CircleOptions()
////                .zIndex(z)
////                .center(pt)
////                .radius(r.toDouble())
////                .strokeColor(strokeColor)
////                .strokeWidth(width)
////                .fillColor(fillColor)
////            objMap[name] = it.addCircle(options)
////        }
////        boundMap[name] = buildCircleBound(pt, r.toDouble())
//    }
//
//    private fun fitBound(box: BoundingBox) {
//        val cb = CoordinateBounds(box.southwest(), box.northeast())
//        val camera = bmap.cameraForCoordinateBounds(cb, EdgeInsets(200.0, 200.0, 200.0, 200.0))
//        bmap.flyTo(camera)
//    }
//
//    override fun fit() {
//        execute {
//            val vertices = mutableListOf<Point>()
//            for ((k, v) in objMap) {
//                if (k.startsWith("!")) continue
//                when (v) {
//                    is PolygonAnnotation, is PolylineAnnotation -> {
//                        boundMap[k]?.apply {
//                            vertices.add(northeast())
//                            vertices.add(southwest())
//                        }
//                    }
//                    is PointAnnotation -> vertices.add(v.point)
//                }
//            }
//            if (vertices.size > 1) {
//                fitBound(buildBound(vertices))
//            }
//        }
//    }
//
//    override fun fit(names: List<String>) {
//        execute {
//            val vertices = mutableListOf<Point>()
//            for (name in names) {
//                when (val obj = objMap[name]) {
//                    is PolygonAnnotation, is PolylineAnnotation -> {
//                        boundMap[name]?.apply {
//                            vertices.add(northeast())
//                            vertices.add(southwest())
//                        }
//                    }
//                    is PointAnnotation -> vertices.add(obj.point)
//                }
//            }
//            if (vertices.size > 1) {
//                fitBound(buildBound(vertices))
//            }
//        }
//    }
//
//    private fun colorToString(color: Int): String {
//        val r = Color.red(color)
//        val g = Color.green(color)
//        val b = Color.blue(color)
//        return String.format("#%02x%02x%02x", r, g, b)
//    }
//
//    private fun pointsGeoJSON(pts: List<MapRing>, colors: List<Int>): String {
//        val sb = StringBuilder("{")
//        sb.append("\"type\":\"FeatureCollection\",")
//        sb.append("\"features\":[")
//        for ((idx, pt) in pts.withIndex()) {
//            val color = colorToString(colors[idx])
//            sb.append("{\"type\":\"Feature\",")
//            sb.append("\"properties\":{\"color\":\"$color\"},")
//            sb.append("\"geometry\":{\"type\":\"MultiPoint\",\"coordinates\":[")
//            for ((i, p) in pt.withIndex()) {
//                if (i > 0) sb.append(',')
//                sb.append("[${p.longitude},${p.latitude}]")
//            }
//            sb.append("]}}") // end of geometry
//            if (idx > 0) {
//                sb.append(',')
//            }
//        }
//        sb.append("]}") // end of JSON
//        return sb.toString()
//    }
//
//    override fun drawPoints(pts: List<MapRing>, color: List<Int>) {
//        bmap.getStyle {
//            it.getSourceAs<GeoJsonSource>("pts")?.apply {
//                data(pointsGeoJSON(pts, color))
//            }
//        }
//    }
//
//    override fun removePoints() {
//        bmap.getStyle {
//            it.getSourceAs<GeoJsonSource>("pts")?.apply {
//                data("{\"type\":\"Feature\",\"properties\":{\"color\":\"#000000\"},\"geometry\":{\"type\":\"MultiPoint\",\"coordinates\":[]}}")
//            }
//        }
//    }
//
//    override val angle: Float
//        get() = bmap.cameraState.bearing.toFloat()
//
//    override val centerPoint: GeoHelper.LatLng
//        get() = bmap.cameraState.center.let { fromMapboxPoint(it.latitude(), it.longitude()) }
//
//    override val boundingBox: LatLngBound
//        get() {
//            val nw = bmap.coordinateForPixel(ScreenCoordinate(0.0, 0.0))
//            val se = bmap.coordinateForPixel(ScreenCoordinate(view.width.toDouble(), view.height.toDouble()))
//            return LatLngBound(nw.latitude(), se.longitude(), se.latitude(), nw.longitude())
//        }
//
//    override fun toLatLng(x: Int, y: Int): GeoHelper.LatLng {
//        val pt = bmap.coordinateForPixel(ScreenCoordinate(x.toDouble(), y.toDouble()))
//        return fromMapboxPoint(pt.latitude(), pt.longitude())
//    }
//
//    override fun toPoint(lat: Double, lng: Double): Point2D {
//        var screenCoordinate = Point2D(-1000.0, -1000.0)
//        return try {
//            screenCoordinate = pixelNotWholeScreenCoordinate(lng, lat)
//            val mapboxMapInstance = bmap// 获取 nativeMap 字段的 Field 对象
//            val mapboxMapClass = Class.forName("com.mapbox.maps.MapboxMap")// 获取 MapboxMap 类的 Class 对象
//            val nativeMapField: Field = mapboxMapClass.getDeclaredField("nativeMap")
//            nativeMapField.isAccessible = true// 设置字段可访问
//
//            val nativeMapValue = nativeMapField.get(mapboxMapInstance) as MapInterface// 获取 nativeMap 字段的值
//            val pixelForCoordinateMethod: Method = nativeMapValue.javaClass.getDeclaredMethod("pixelForCoordinate", Point::class.java)// 获取 pixelForCoordinate 方法的 Method 对象
//            pixelForCoordinateMethod.isAccessible = true// 设置方法可访问
//            val coordinateInstance = Point.fromLngLat(lng, lat)// 创建 Point 实例（假设你有一个实例）
//            val wholeScreenCoordinate: ScreenCoordinate = pixelForCoordinateMethod.invoke(nativeMapValue, coordinateInstance) as ScreenCoordinate// 调用 pixelForCoordinate 方法并获取结果
////            Log.v("shero", "result (${wholeScreenCoordinate.x}, ${wholeScreenCoordinate.y})")
//            Point2D(wholeScreenCoordinate.x, wholeScreenCoordinate.y)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            screenCoordinate
//        }
//    }
//
//    private fun pixelNotWholeScreenCoordinate(lng: Double, lat: Double): Point2D {
//        val pf = bmap.pixelsForCoordinates(listOf(Point.fromLngLat(lng, lat)))[0]
//        return Point2D(pf.x, pf.y)
//    }
//
//    override fun screenshot(file: File, complete: () -> Unit, edit: ((Bitmap) -> Unit)?) {
//        view.snapshot {
//            it?.let {
//                edit?.invoke(it)
//                saveScreenshot(it, file)
//                complete()
//                Log.d("yuhang", "saved: $file")
//            }
//        }
//    }
//
//    override fun onStart() { view.onStart() }
//    override fun onStop() { view.onStop() }
//    override fun onDestroy() {
//        if (ready) {
//            fillManager.onDestroy()
//            lineManager.onDestroy()
//            symbolManager.onDestroy()
//        }
//        view.onDestroy()
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        val gson = Gson()
//        outState.putDouble("bearing", bmap.cameraState.bearing)
//        outState.putDouble("zoom", bmap.cameraState.zoom)
//        outState.putDouble("pitch", bmap.cameraState.pitch)
//        outState.putString("center", gson.toJson(bmap.cameraState.center))
//        outState.putDouble("padding.left", bmap.cameraState.padding.left)
//        outState.putDouble("padding.right", bmap.cameraState.padding.right)
//        outState.putDouble("padding.top", bmap.cameraState.padding.top)
//        outState.putDouble("padding.bottom", bmap.cameraState.padding.bottom)
//        super.onSaveInstanceState(outState)
//    }
//}
