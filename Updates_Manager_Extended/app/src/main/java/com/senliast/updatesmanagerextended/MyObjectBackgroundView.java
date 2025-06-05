package com.senliast.updatesmanagerextended;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class MyObjectBackgroundView extends View {
    private Paint paint;
    private float cornerRadius;

    public MyObjectBackgroundView(Context context) {
        super(context);
        init(context);
    }

    public MyObjectBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyObjectBackgroundView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        cornerRadius = Utils.convertDpToFloat(context,30);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        canvas.drawRoundRect(0, 0, width, height, cornerRadius, cornerRadius, paint);
    }

    public void setRectangleColor(int color) {
        paint.setColor(color);
        invalidate();
    }
}
