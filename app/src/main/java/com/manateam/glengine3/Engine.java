package com.manateam.glengine3;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.manateam.glengine3.utils.Utils;

public class Engine {
    public static class touch {
        public float x;
        public float y;
    }

    static float[][] touchEvents = new float[100][22];//[x ,y,length]*10+ type(0 - started , 1 - moved , 2 - eneded)
    public static int pointsNumber;
    static int touchEventsNumb = 0;
    public static touch[] touches;
    static String touchEvent = "";
    static float[][] tch = new float[10][2];//float touches

    private GLSurfaceView glSurfaceView;
    public Context context;

    public  GLSurfaceView onCreate(Context c) {
        ActivityManager activityManager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

        Log.e("version", String.valueOf(Double.parseDouble(configurationInfo.getGlEsVersion())));
        Log.e("version", String.valueOf(configurationInfo.reqGlEsVersion >= 0x30000));
        Log.e("version", String.format("%X", configurationInfo.reqGlEsVersion));
        context = c;
        touches = new touch[10];
        for (int i = 0; i < touches.length; i++) {
            touches[i] = new touch();
        }
        if (!supportES2()) {
            Toast.makeText(context, "OpenGL ES 2.0 is not supported", Toast.LENGTH_LONG).show();
            return null;
        }
        glSurfaceView = new GLSurfaceView(context);
        glSurfaceView.setEGLContextClientVersion(3);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        glSurfaceView.setRenderer(new OpenGLRenderer(context, displayMetrics.widthPixels, displayMetrics.heightPixels));
        return glSurfaceView;
    }

    private  boolean supportES2() {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return (configurationInfo.reqGlEsVersion >= 0x20000);
    }

    public static boolean onTouch(View v, MotionEvent event) {
        int actionMask = event.getActionMasked();
        pointsNumber = event.getPointerCount();
        // touches = new touch[pointsNumber];
        ////Log.i("",String.valueOf(pointsNumber));
        for (int i = 0; i < Math.min(pointsNumber, 49); i++) {
            tch[i][0] = event.getX(i);
            tch[i][1] = event.getY(i);
            //  Log.e("dgf",String.valueOf(y-tch[i][1]));
            if (touchEventsNumb >= touchEvents.length) {
                touchEventsNumb = touchEvents.length - 1;
            }
            touchEvents[touchEventsNumb][i * 2] = event.getX(i);
            touchEvents[touchEventsNumb][i * 2 + 1] = event.getY(i);
           /* if (callAlertHitbox != null) {
                if (callAlertHitbox.checkHitbox(event.getX(i), event.getY(i))) {
                    if (!enterTextAlertIsShown) {
                        showAlertDialog();
                        Log.e("alert", "showing");
                        break;//exit because we will not need to check touch on main screen any more
                    }
                }
            }
            */
        }
        touchEvents[touchEventsNumb][21] = pointsNumber;
        if (actionMask == MotionEvent.ACTION_DOWN || actionMask == MotionEvent.ACTION_POINTER_DOWN) {
            // draw_view.drawThread.touchStarted();
            touchEvent = "touchStarted";
            touchEvents[touchEventsNumb][20] = 0;
        }
        if (actionMask == MotionEvent.ACTION_UP || actionMask == MotionEvent.ACTION_POINTER_UP) {
            // draw_view.drawThread.touchEnded();
            touchEvent = "touchEnded";
            touchEvents[touchEventsNumb][20] = 2;
        }
        if (actionMask == MotionEvent.ACTION_MOVE) {
            // draw_view.drawThread.touchMoved();
            touchEvent = "touchMoved";
            touchEvents[touchEventsNumb][20] = 1;
        }
        touchEventsNumb++;
        return true;
        //  return false;
    }

    public  void onPause() {
        glSurfaceView.onPause();
        Utils.onPause();
    }

    public  void onResume() {
        glSurfaceView.onResume();
        Utils.onResume();
    }
}
