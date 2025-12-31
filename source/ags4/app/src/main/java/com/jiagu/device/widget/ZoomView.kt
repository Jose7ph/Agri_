package com.jiagu.device.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class ZoomView: View {

    private var lineWidth = 45f
    private var viewSize = 230f
    private val paint = Paint()

    constructor(context: Context) : super(context) { init() }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { init() }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { init() }

    private fun init() {
        paint.color = Color.WHITE
        paint.isAntiAlias = true
        paint.strokeWidth = 3f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawLine(0f, lineWidth, 0f, 0f, paint)
        canvas.drawLine(0f, 0f, lineWidth, 0f, paint)

        canvas.drawLine(viewSize - lineWidth, 0f, viewSize, 0f, paint)
        canvas.drawLine(viewSize, 0f, viewSize, lineWidth, paint)

        canvas.drawLine(viewSize, viewSize - lineWidth, viewSize, viewSize, paint)
        canvas.drawLine(viewSize, viewSize, viewSize - lineWidth, viewSize, paint)

        canvas.drawLine(lineWidth, viewSize, 0f, viewSize, paint)
        canvas.drawLine(0f, viewSize, 0f, viewSize - lineWidth, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val h = MeasureSpec.getSize(viewSize.toInt())
        val w = MeasureSpec.getSize(viewSize.toInt())
        setMeasuredDimension(w,h)
    }
}