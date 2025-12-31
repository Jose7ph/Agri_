package com.jiagu.device.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.jiagu.api.ext.dp2px
import com.jiagu.api.video.IVideoListener
import com.jiagu.v9sdk.R
import com.jiagu.device.controller.ControllerFactory
import com.jiagu.device.controller.IVideo
import androidx.core.content.withStyledAttributes

open class VideoFrame : FrameLayout {

    private var videoListener: VideoTouchListener? = null
    private var videoZoomListener: VideoZoomListener? = null
    private var videoClickListener: VideoClickListener? = null
    interface VideoTouchListener {
        fun onVideoTouch(x: Float, y: Float, xStrength: Int, yStrength: Int)
        fun onVideoTouch(left: Int, right: Int, top: Int, bottom: Int, xStrength: Int, yStrength: Int)
        fun onVideoTouch(type: Int, strength: Int)
    }

    interface VideoZoomListener {
        fun onLongClick(x: Float, y: Float, width: Int, height: Int)
    }

    interface VideoClickListener {
        fun onClick(x: Float, y: Float, width: Int, height: Int)
    }

    fun setVideoTouchListener(l: VideoTouchListener) {
        videoListener = l
    }

    fun setVideoZoomListener(l: VideoZoomListener) {
        videoZoomListener = l
    }

    fun setVideoClickListener(l: VideoClickListener) {
        videoClickListener = l
    }

    constructor(context: Context) : super(context) { init(context, null) }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init(context, attrs) }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    private lateinit var video: IVideo
    private lateinit var label: TextView
    private lateinit var center: VideoCenterView
    private lateinit var zoomView: ZoomView
    private fun init(context: Context, attrs: AttributeSet?) {
        if (attrs != null){
            context.withStyledAttributes(attrs, R.styleable.VideoFrame, 0, 0) {
                val index = getInt(R.styleable.VideoFrame_video_index, 1)
                when (index) {
                    1 -> video = buildVideoView(context)
                    2 -> video = buildVideoView2(context)
                }
            }
        } else {
            video = buildVideoView(context)
        }

        addView(video.asView())
        if (video.queryCapability(IVideo.CAP_LED)) {
            addLed()
        }
        setBackgroundColor(Color.BLACK)

        label = TextView(context)
        val lp2 = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        lp2.gravity = Gravity.CENTER_HORIZONTAL
        lp2.setMargins(0, context.dp2px(3f), 0, 0)
        label.layoutParams = lp2
        addView(label)
        label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
        label.setTextColor(Color.WHITE)

        center = VideoCenterView(context)
        addView(center)

        val zoomLy = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        zoomView = ZoomView(context)
        zoomView.layoutParams = zoomLy
        addView(zoomView)
        zoomView.visibility = GONE
    }

    private fun addVideoView() {
        removeView(video.asView())
        video = buildVideoView(context)
        addView(video.asView(), 0)
    }

    private var longClickStartX = 0f
    private var longClickStartY = 0f
    fun showZoomView(show: Boolean) {
        zoomView.visibility = if (show) VISIBLE else GONE
        requestLayout()
        val zh = zoomView.measuredHeight / 2f
        val zw = zoomView.measuredWidth / 2f
        zoomView.x = longClickStartX - zw
        zoomView.y = longClickStartY - zh
//        Log.v("shero", "zh:$zh zw:$zw longX:$longClickStartX longY: $longClickStartY x:${zoomView.x} y:${zoomView.y}")
    }

    fun setLabel(text: String) {
        label.text = text
    }

    fun setVideoSwitch() {
        val btn = ImageView(context)
        btn.setImageResource(R.drawable.icon_switch_video)
        val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        lp.gravity = Gravity.START + Gravity.BOTTOM
        btn.layoutParams = lp
        addView(btn)
        btn.setOnClickListener { switchVideo() }
        label.text = context.getString(R.string.video_front)
        addVideoView()
    }

    private fun addLed() {
        val ledbtn = ImageView(context)
        val width = context.dp2px(26f)
        ledbtn.setImageResource(R.drawable.icon_video_led)
        val lp = LayoutParams(width, width)
        lp.gravity = Gravity.RIGHT + Gravity.BOTTOM
        val margin = context.dp2px(5f)
        lp.rightMargin = margin
        lp.bottomMargin = margin
        ledbtn.layoutParams = lp
        addView(ledbtn)
        ledbtn.setOnClickListener { specialAction("light") }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (!changed) return

        var w = right - left
        var h = bottom - top
        if (w / h > aspect) {
            w = (h * aspect).toInt()
        } else {
            h = (w / aspect).toInt()
        }
        val lp = LayoutParams(w, h)
        lp.gravity = Gravity.CENTER
        video.asView().layoutParams = lp

        val cw = h / 4
        val lp2 = LayoutParams(h / 2, h / 4)
        lp2.gravity = Gravity.CENTER
        center.layoutParams = lp2
    }

    private var aspect = 16f / 9
    fun setAspectRatio(ratio: Float) {
        aspect = ratio
    }

    open fun buildVideoView(context: Context): IVideo {
        return ControllerFactory.createVideo(context)
    }

    open fun buildVideoView2(context: Context): IVideo {
        return ControllerFactory.createVideo2(context)
    }

    private var camera = "fpv"
    fun specialAction(cmd: String) {
        val child = getChildAt(0) as IVideo
        when (cmd) {
            "light" -> if (child.queryCapability(IVideo.CAP_LED)) {
                child.toggleLed()
            }
            "fpv" -> if (camera != "fpv") {
                camera = "fpv"
                if (child.queryCapability(IVideo.CAP_SWITCH)) {
                    child.switchCamera(0)
                }
                label.text = context.getString(R.string.video_front)
            }
            "camera2" -> if (camera != "camera2") {
                camera = "camera2"
                if (child.queryCapability(IVideo.CAP_SWITCH)) {
                    child.switchCamera(1)
                }
                label.text = context.getString(R.string.video_back)
            }
        }
    }

    private fun switchVideo() {
        when (camera) {
            "fpv" -> specialAction("camera2")
            "camera2" -> specialAction("fpv")
            "led" -> specialAction("led")
        }
    }

    private var currentWidth = 0
    private var currentHeight = 0
    private var widthAverage = 0
    private var heightAverage = 0
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        currentWidth = w
        currentHeight = h
        widthAverage = currentWidth / 2 / 100
        heightAverage = currentHeight / 2 / 100
//        Log.v("shero", "w:$w h:$h oldW:$oldw oldh:$oldh")
    }

    private var preX = 0f
    private var preY = 0f
    private var preEventX = 0f
    private var preEventY = 0f
    private var lastMoveTime = 0L
    private var lastDownTime = 0L
    private var isScroll = false
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event?.action == MotionEvent.ACTION_UP) {
            longClickStartX = event.x
            longClickStartY = event.y
            videoZoomListener?.onLongClick(event.x, event.y, measuredWidth, measuredHeight)
            videoClickListener?.onClick(event.x, event.y, measuredWidth, measuredHeight)
        }
//        Log.v("shero", "point (${event?.x},${event?.y}) measuredWidth:${measuredWidth} measuredHeight:${measuredHeight}")
//        if (event?.action == MotionEvent.ACTION_DOWN) {
//            preX = event.x
//            preY = event.y
//            lastDownTime = System.currentTimeMillis()
//            isScroll = false
//        }
//        if (event?.action == MotionEvent.ACTION_UP) {
//            if (abs(preX - event.x) < 16) {
//                val current = System.currentTimeMillis()
//                val diffTime = current - lastDownTime
////                Log.v("shero", "click < 16 current:$current lastDownTime:${lastDownTime} diffTime:$diffTime")
//                if (diffTime > 500 && !isScroll) {
//                    longClickStartX = event.x
//                    longClickStartY = event.y
////                    Log.v("shero", "x: $longClickStartX y:$longClickStartY")
//                    videoZoomListener?.onLongClick(longClickStartX, longClickStartY,
//                        measuredWidth, measuredHeight)
//                }
////                else performClick()
//            }//抬起，位置小于16，点击效果
//            else {
//                videoListener?.onVideoTouch(0f, 0f, 0, 0)
//                videoListener?.onVideoTouch(0, 0)
//                preEventX = 0f
//                preEventY = 0f
//                lastMoveTime = 0
//            }
//        }
////        0..............960.............1920
////        0
////        .
////        .
////        540
////        .
////        .
////        .
////        1080
//        if (event?.action == MotionEvent.ACTION_MOVE) {//0-停止 1-向上 2-向下 3-向左 4-向右     速率 0~1000，1000最大
//            if (abs(preX - event.x) > 16) {
//                val current = System.currentTimeMillis()
//                if (lastMoveTime == 0L) lastMoveTime = current
//                val diff = current - lastMoveTime
//                if (abs(diff) >= 120) {
//                    val x = event.x
//                    val y = event.y
//                    if (preEventX == 0f) preEventX = x
//                    if (preEventY == 0f) preEventY = y
//                    val diffX = x - preEventX
//                    val diffY = y - preEventY
//
//
//                    val diffTime = if (diff == 0L) 1 else diff
//                    val absDiffX = abs(diffX)
//                    val absDiffY = abs(diffY)
//                    if (absDiffX > absDiffY) {
//                        if (absDiffX > 2) {
//                            val type = if (diffX > 0) 4 else 3
//                            val s = absDiffX / diffTime.toFloat()
//                            videoListener?.onVideoTouch(type, (s * 1000).toInt())
//                        }
//                    } else {
//                        if (absDiffY > 2) {
//                            val type = if (diffY > 0) 2 else 1
//                            val s = absDiffY / diffTime.toFloat()
//                            videoListener?.onVideoTouch(type, (s * 1000).toInt())
//                        }
//                    }
//                    isScroll = true
//
////                val xS = abs(currentX / widthAverage)
////                val yS = abs(currentY / heightAverage)
////                val xStrength = if (xS > 100) 100 else if (xS < 0) 0 else xS
////                val yStrength = if (yS > 100) 100 else if (yS < 0) 0 else yS
////                videoListener?.onVideoTouch(currentX, currentY, xStrength.toInt(), yStrength.toInt())
////                val left = if (currentX > 0) 0 else abs(currentX).toInt()
////                val right = if (currentX > 0) abs(currentX).toInt() else 0
////                val top = if (currentY > 0) abs(currentY).toInt() else 0
////                val bottom = if (currentY > 0) 0 else abs(currentY).toInt()
////                videoListener?.onVideoTouch(left, right, top, bottom, xStrength.toInt(), yStrength.toInt())
//                    lastMoveTime = current
//                    preEventX = x
//                    preEventY = y
////                    Log.v("shero", "absDiffX:$absDiffX absDiffY:${absDiffY} diffTime:${diffTime}")
////                Log.v("shero", "move ($x, $y) ($currentX,${currentY}) (${currentWidth},${currentHeight}) diff:$diff diffX:${diffX} diffY:${diffY}")
//                }
//            }
//        }
        return super.onTouchEvent(event)
    }

    fun setVideoListener(l: IVideoListener) {
        val child = getChildAt(0) as IVideo
        child.setVideoListener(l)
    }

    fun setRoll(roll: Int) {
        center.setRoll(roll)
    }
}