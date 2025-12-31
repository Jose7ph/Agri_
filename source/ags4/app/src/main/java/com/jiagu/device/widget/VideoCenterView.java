package com.jiagu.device.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class VideoCenterView extends View {

    public VideoCenterView(Context context) {
        super(context);
        init();
    }

    public VideoCenterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoCenterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private Paint paint;
    private Paint paintDash;
    private final Path path = new Path();
    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10f);
        paint.setColor(Color.argb(100, 255, 255, 0));

        paintDash = new Paint();
        paintDash.setAntiAlias(true);
        paintDash.setStyle(Paint.Style.STROKE);
        paintDash.setStrokeWidth(10f);
        paintDash.setColor(Color.argb(100, 255, 255, 0));

        // 设置虚线效果，第一个参数是虚线数组，第二个参数是偏移量
        paintDash.setPathEffect(new DashPathEffect(new float[]{8, 10}, 0));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        float x = w / 2f;
        float y = h / 2f;
        path.reset();
        path.moveTo(x, h / 3f);
        path.lineTo(x, 0);
        canvas.drawPath(path, paintDash);
        path.reset();
        path.moveTo(x, 2 * h / 3f);
        path.lineTo(x, h);
        canvas.drawPath(path, paintDash);
//        canvas.drawLine(x, h / 3f, x, 0, paintDash);
//        canvas.drawLine(x, 2 * h / 3f, x, h, paintDash);
        double radianAngle = -angle / 180f * Math.PI;
        float yRadius = w / 6f;
        double cosx = Math.cos(radianAngle);
        double sinx = Math.sin(radianAngle);
        float x1 = (float) cosx * x;
        float y1 = (float) sinx * x;
        float x11 = (float) cosx * (yRadius);
        float y11 = (float) sinx * (yRadius);
        path.reset();
        path.moveTo(x + x11, y - y11);
        path.lineTo(x + x1, y - y1);
        canvas.drawPath(path, paintDash);
        path.reset();
        path.moveTo(x - x11, y + y11);
        path.lineTo(x - x1, y + y1);
        canvas.drawPath(path, paintDash);
//        canvas.drawLine(x + x11, y - y11, x + x1, y - y1, paintDash);
//        canvas.drawLine(x - x11, y + y11, x - x1, y + y1, paintDash);

        // 添加绘制圆的代码
        float circleRadius = h / 9f;  // h/6
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        canvas.drawCircle(x, y, circleRadius, paint);  // 在中心点绘制半径为30的圆
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(x, y, circleRadius / 8f, paint);  // 在中心点绘制半径为30的圆
    }

    private int angle = 0;
    public void setRoll(int a) {
        angle = a;
        invalidate();
    }
}
