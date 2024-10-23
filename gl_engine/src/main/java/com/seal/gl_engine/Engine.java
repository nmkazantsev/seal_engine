package com.seal.gl_engine;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.seal.gl_engine.engine.config.MainConfigurationFunctions;
import com.seal.gl_engine.engine.main.debugger.Debugger;
import com.seal.gl_engine.utils.Utils;

import java.util.function.Function;

public class Engine {
    public static final String version = "3.1.1";
    private static boolean shadowsPass = false;

    public static boolean getShadowPass() {
        return shadowsPass;
    }

    protected static void setShadowsPass(boolean shadowPass) {
        shadowsPass = shadowPass;
    }

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
    protected static Function<Void, GamePageClass> getStartPage;

    public GLSurfaceView onCreate(Context c, Function<Void, GamePageClass> getStartPage, boolean landscape, boolean debug) {
        Engine.getStartPage = getStartPage;
        ActivityManager activityManager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        Log.e("engine version ", version);
        Log.e("version", String.valueOf(Double.parseDouble(configurationInfo.getGlEsVersion())));
        Log.e("version", String.valueOf(configurationInfo.reqGlEsVersion >= 0x30000));
        Log.e("version", String.format("%X", configurationInfo.reqGlEsVersion));
        Log.e("engine version", version);
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
        if (debug) {
            Debugger.debuggerInit();
        }
        return glSurfaceView;
    }

    public void startPage(GamePageClass pageInterface) {
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
    public static void StartNewPage(GamePageClass pageInterface) {
        OpenGLRenderer.startNewPage(pageInterface);
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
