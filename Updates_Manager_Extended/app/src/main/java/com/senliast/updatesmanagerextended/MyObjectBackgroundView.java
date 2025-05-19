package com.senliast.updatesmanagerextended;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.material.color.MaterialColors;
import com.senliast.MyApplication;

public class MyObjectBackgroundView extends View {
    private Paint paint;
    private RectF rect;
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
        paint.setColor(MaterialColors.getColor(MyApplication.getAppContext(), android.R.attr.colorAccent, MyApplication.getAppContext().getColor(R.color.primary)));
        paint.setStyle(Paint.Style.FILL);
        paint.setAlpha(30);

        rect = new RectF(0, 0, getWidth(), getHeight());
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
