package com.jiagu.ags4.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import kotlin.math.abs

class MapClickLayout: FrameLayout {
    constructor(context: Context): super(context, null)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    private var preX = 0f
    private var preY = 0f
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (intercept) {
            when (ev?.action) {
                MotionEvent.ACTION_DOWN -> {
                    preX = ev.x
                    preY = ev.y
                }

                MotionEvent.ACTION_UP -> {
                    if (abs(preX - ev.x) < 16 && abs(preY - ev.y) < 16) {
                        performClick() //抬起，位置小于16，点击效果
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private var intercept = false
    fun enableInterceptTouchEvent(enable: Boolean) {
        intercept = enable
    }
}