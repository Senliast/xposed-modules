package com.senliast.updatesmanagerextended;

import android.app.UiModeManager;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;

import com.senliast.MyApplication;

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

    public static boolean isDarkModeActive() {
        UiModeManager uiModeManager = (UiModeManager) MyApplication.getAppContext().getSystemService(Context.UI_MODE_SERVICE);
        int mode = uiModeManager.getNightMode();
        if (mode == UiModeManager.MODE_NIGHT_YES) {
            return true;
        } else  {
            return false;
        }
    }
}
