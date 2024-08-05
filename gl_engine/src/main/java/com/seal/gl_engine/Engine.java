package com.seal.gl_engine;

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

import com.seal.gl_engine.engine.config.MainConfigurationFunctions;
import com.seal.gl_engine.utils.Utils;

import java.util.function.Function;

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
    protected static Function<Void, GamePageInterface> getStartPage;

    public GLSurfaceView onCreate(Context c, Function<Void, GamePageInterface> getStartPage, boolean landscape) {
        Engine.getStartPage = getStartPage;
        ActivityManager activityManager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

        Log.e("version", String.valueOf(Double.parseDouble(configurationInfo.getGlEsVersion())));
        Log.e("version", String.valueOf(configurationInfo.reqGlEsVersion >= 0x30000));
        Log.e("version", String.format("%X", configurationInfo.reqGlEsVersion));
        context = c;
        MainConfigurationFunctions.context = context;
        Utils.context = context;

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
        float widthPixels = displayMetrics.widthPixels;
        float heightPixels = displayMetrics.heightPixels;
        if (landscape && widthPixels < heightPixels) {
            glSurfaceView.setRenderer(new OpenGLRenderer(context, heightPixels, widthPixels));
        } else if (!landscape && widthPixels > heightPixels) {
            glSurfaceView.setRenderer(new OpenGLRenderer(context, heightPixels, widthPixels));
        } else {
            glSurfaceView.setRenderer(new OpenGLRenderer(context, widthPixels, heightPixels));
        }

        return glSurfaceView;
    }

    public void startPage(GamePageInterface pageInterface) {
        OpenGLRenderer.startNewPage(pageInterface);
    }

    private boolean supportES2() {
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return (configurationInfo.reqGlEsVersion >= 0x20000);
    }

    /**
     * Redefinition of OpenglRenderer.startNewPage(..). Calls the function above.
     *
     * @param pageInterface - object of new page
     */
    public static void StartNewPage(GamePageInterface pageInterface) {
        OpenGLRenderer.startNewPage(pageInterface);
    }


    public static boolean onTouch(View v, MotionEvent event) {
        int actionMask = event.getActionMasked();
        pointsNumber = event.getPointerCount();
        for (int i = 0; i < Math.min(pointsNumber, 49); i++) {
            tch[i][0] = event.getX(i);
            tch[i][1] = event.getY(i);
            //  Log.e("dgf",String.valueOf(y-tch[i][1]));
            if (touchEventsNumb >= touchEvents.length) {
                touchEventsNumb = touchEvents.length - 1;
            }
            touchEvents[touchEventsNumb][i * 2] = event.getX(i);
            touchEvents[touchEventsNumb][i * 2 + 1] = event.getY(i);
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

    public void onPause() {
        glSurfaceView.onPause();
        Utils.onPause();
    }

    public void onResume() {
        glSurfaceView.onResume();
        Utils.onResume();
    }
}
