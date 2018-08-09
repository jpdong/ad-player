package com.eeseetech.nagrand.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.eeseetech.nagrand.Global;

import static android.content.Context.DISPLAY_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

public class MultiWindowManager {

    private static WindowManager.LayoutParams mLayoutParams;
    private static WindowManager mWindowManager;
    private static Context mDisplayContext;

    public static boolean createMultiWindow(Context context, View view) {
        WindowManager windowManager = getWindowManager(context);
        if (view != null && windowManager != null) {
            if (mLayoutParams == null) {
                Point point = new Point();
                windowManager.getDefaultDisplay().getSize(point);
                int screenWidth = point.x;
                int screenHeight = point.y;
                Log.d(Global.TAG, "MultiWindowManager/createMultiWindow:width : " + screenWidth + ",height:" + screenHeight);
                mLayoutParams = new WindowManager.LayoutParams();
                //mLayoutParams.x = 0;
                //mLayoutParams.y = 0;
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                mLayoutParams.format = PixelFormat.RGBA_8888;
                mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                //mLayoutParams.gravity = Gravity.START | Gravity.TOP;
                /*mLayoutParams.width = WindowManager.LayoutParams.FLAG_FULLSCREEN;
                mLayoutParams.height = WindowManager.LayoutParams.FLAG_FULLSCREEN;*/
                mLayoutParams.width = screenWidth;
                mLayoutParams.height = screenHeight;
            }
            windowManager.addView(view, mLayoutParams);
            return true;
        } else {
            return false;
        }
    }

    public static boolean removeMultiWindow(Context context, View view) {
        WindowManager windowManager = getWindowManager(context);
        if (view != null && windowManager != null) {
            windowManager.removeView(view);
            return true;
        } else {
            return false;
        }
    }

    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            Context displayContext = getDisplayContext(context);
            if (displayContext != null) {
                mWindowManager = (WindowManager) displayContext.getSystemService(WINDOW_SERVICE);
            }
        }
        return mWindowManager;
    }

    public static Context getDisplayContext(Context context) {
        if (mDisplayContext == null) {
            DisplayManager dm = (DisplayManager) context.getSystemService(DISPLAY_SERVICE);
            if (dm != null) {
                Display dispArray[] = dm.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
                Log.d(Global.TAG, "onCreate: display num:" + dispArray.length);
                if (dispArray.length > 0) {
                    Display display = dispArray[0];
                    Log.e(Global.TAG, "Service using display:" + display.getName());
                    mDisplayContext = context.createDisplayContext(display);
                }
            }
        }
        return mDisplayContext;
    }

    public static void clearCache() {
        mDisplayContext = null;
        mWindowManager = null;
        mLayoutParams = null;
    }
}
