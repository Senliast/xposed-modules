package com.senliast.updatesmanagerextended;

import android.app.UiModeManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import com.senliast.MyApplication;

public class Utils {
    public static float convertDpToFloat(Context context, int dpValue) {
        float floatValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
        return floatValue;
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

    public static int converDpToPx(Context context, int dp) {
        int px = (int) (dp * context.getResources().getSystem().getDisplayMetrics().density);
        return px;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        // Fallback size if intrinsic size is not available
        if (width <= 0) width = 1;
        if (height <= 0) height = 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
