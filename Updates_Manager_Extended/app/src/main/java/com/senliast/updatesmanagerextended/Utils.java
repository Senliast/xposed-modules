package com.senliast.updatesmanagerextended;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;

public class Utils {
    public static float convertDpToFloat(Context context, int dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;

        float floatValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());

        return floatValue;
    }

    public static int changeColorAlpha(int color, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));

        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        return Color.argb(alpha, red, green, blue);
    }
}
