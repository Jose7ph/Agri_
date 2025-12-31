package com.jiagu.ags4

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jiagu.ags4.repo.sp.AppConfig
import com.jiagu.ags4.ui.widget.MapClickLayout
import com.jiagu.ags4.vm.LocationModel
import com.jiagu.api.ext.dp2px
import com.jiagu.tools.map.DroneView
import com.jiagu.tools.map.IMapCanvas
import com.jiagu.tools.map.IMapCanvas.Companion.Z_MARKER
//import com.jiagu.tools.map.MapboxCanvas
import com.jiagu.tools.map.createGoogleMap
//import com.jiagu.tools.map.createMapbox
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class MapBaseActivity(private val autoLocate: Boolean = false) : BaseActivity(),
    IMapCanvas.MapReadyListener {

    lateinit var canvas: IMapCanvas
    lateinit var droneCanvas: DroneView

    val mapbox by lazy { findViewById<MapClickLayout>(R.id.mapbox) }
    protected lateinit var controller: WindowInsetsControllerCompat
    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContentView()

        droneCanvas = DroneView(this)
//        canvas = createMapCanvas(mapbox, savedInstanceState, autoLocate)
        droneCanvas.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        mapbox.addView(droneCanvas)
    }

//    private fun createMapCanvas(
//        box: FrameLayout,
//        savedInstanceState: Bundle?,
//        autoLocate: Boolean,
//    ): IMapCanvas {
//        val config = AppConfig(this)
//        return when (config.mapProvider) {
//            "mapbox" -> IMapCanvas.createMapbox(
//                box,
//                savedInstanceState,
//                autoLocate,
////                MapboxCanvas.STYLE_MAPBOX,
////                this
//            )
//
//            "google" -> IMapCanvas.createGoogleMap(box, savedInstanceState, autoLocate, this)
//            else -> {
//                var url = config.mapUrl
//                if (url == "") url = MapboxCanvas.STYLE_GOOGLE
//                Log.d("yuhang", "createMapCanvas: $url")
//                IMapCanvas.createMapbox(box, savedInstanceState, autoLocate, url, this)
//            }
//        }.apply { applyNewTheme() }
//    }

    private fun IMapCanvas.applyNewTheme() {
        applyMarkerOption(object : IMapCanvas.MarkerOption(
            IMapCanvas.Params.ANCHOR_CENTER,
            Color.WHITE,
            14f
        ) {
            override fun draw(title: String, color: Int): Bitmap {
                val paint = Paint()
                val sz = context.dp2px(18f).toFloat()
                val bitmap = Bitmap.createBitmap(sz.toInt(), sz.toInt(), Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                paint.style = Paint.Style.FILL
                paint.color = color
                canvas.drawCircle(sz / 2, sz / 2, sz / 2 - 1, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.WHITE
                paint.strokeWidth = 2f
                canvas.drawCircle(sz / 2, sz / 2, sz / 2 - 1, paint)
                if (title.isNotBlank()) {
                    paint.textSize = sz * 0.5f
                    paint.color = textColor
                    val bounds = Rect()
                    paint.getTextBounds(title, 0, title.length, bounds)
                    canvas.drawText(
                        title,
                        (sz - bounds.width()) / 2f - bounds.left,
                        (sz + bounds.height()) / 2f,
                        paint
                    )
                }
                return bitmap
            }
        })

        IMapCanvas.applyTheme(
            ContextCompat.getColor(context, R.color.main_btn_color), Color.RED,
            Color.argb(128, 0xFF, 0x8D, 0x1A), 4f,
            ContextCompat.getColor(context, R.color.main_btn_color),
        )
    }

    override fun onMapReady(canvas: IMapCanvas) = droneCanvas.setMapCanvas(canvas)

    abstract fun setContentView()

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun attachBaseContext(newBase: Context?) {
        newBase?.let { baseContext ->
            val overrideConfiguration = Configuration(baseContext.resources.configuration)
            overrideConfiguration.fontScale = 1.0f // 保持字体不缩放
            val context = baseContext.createConfigurationContext(overrideConfiguration)
            super.attachBaseContext(context)
        } ?: super.attachBaseContext(newBase)
    }

    private val jobs = mutableListOf<Job>()
    private val jobMap = mutableMapOf<String, Job>()
    fun cancelJobs() {
        for (job in jobs) {
            job.cancel()
        }
        jobs.clear()
    }

    fun <T : Any?> collectFlow(flow: Flow<T>, f: suspend (T) -> Unit) {
        jobs.add(lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                flow.collectLatest(f)
            }
        })
    }

    fun <T : Any?> collectEveryFlow(flow: Flow<T>, f: suspend (T) -> Unit) {
        jobs.add(lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                flow.collect(f)
            }
        })
    }

    fun <T : Any?> collectFlowPlus(flow: Flow<T>, name: String, f: suspend (T) -> Unit) {
        cancelJob(name)
        jobMap[name] = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                flow.collectLatest(f)
            }
        }
    }

    fun <T : Any?> collectEveryFlowPlus(flow: Flow<T>, name: String, f: suspend (T) -> Unit) {
        cancelJob(name)
        jobMap[name] = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                flow.collect(f)
            }
        }
    }

    fun clearNumbers() {
        canvas.clearNumberMarker()
        droneCanvas.clearText()
    }

    fun cancelJob(name: String) {
        jobMap[name]?.cancel()
    }

    fun cancelAllJob(){
        cancelJobs()
        jobMap.keys.forEach {
            cancelJob(it)
        }
        jobMap.clear()
    }

    private val locatorName = "currentLocator"
    private val locatorJobName = "locatorJob"
    fun startLocatorJob(locationVM: LocationModel) {
        collectFlowPlus(locationVM.location, locatorJobName) {
            if (it == null) {
                canvas.remove(locatorName)
            } else {
                if (it.type > 0) {
                    val resId = when (it.type) {
                        3 -> R.drawable.locater_g
                        2 -> R.drawable.locater_b
                        else -> R.drawable.locater_r
                    }
                    canvas.drawMarker(
                        name = locatorName,
                        lat = it.lat,
                        lng = it.lng,
                        z = Z_MARKER,
                        resourceId = resId
                    )
                } else {
                    canvas.remove(locatorName)
                }
            }
        }
    }

    fun stopLocatorJob() {
        cancelJob(locatorJobName)
        canvas.remove(locatorName)
    }
}