package com.manateam.glengine3;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnTouchListener {
    static float[][] touchEvents = new float[100][22];//[x ,y,length]*10+ type(0 - started , 1 - moved , 2 - eneded)
    public static int pointsNumber;
    static int touchEventsNumb = 0;
    public static touch[] touches;
    static String touchEvent = "";
    float[][] tch = new float[10][2];//float touches

    private GLSurfaceView glSurfaceView;
    public static Context context;
    // Get a handler that can be used to post to the main thread

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();

        Log.e("version", String.valueOf(Double.parseDouble(configurationInfo.getGlEsVersion())));
        Log.e("version", String.valueOf(configurationInfo.reqGlEsVersion >= 0x30000));
        Log.e("version",String.format("%X", configurationInfo.reqGlEsVersion));
        context = getApplicationContext();
        touches = new MainActivity.touch[10];
        for (int i = 0; i < touches.length; i++) {
            touches[i] = new MainActivity.touch();
        }
        super.onCreate(savedInstanceState);
        if (!supportES2()) {
            Toast.makeText(this, "OpenGL ES 2.0 is not supported", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(3);
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        glSurfaceView.setRenderer(new OpenGLRenderer(this, displayMetrics.widthPixels, displayMetrics.heightPixels));
        setContentView(glSurfaceView);
        glSurfaceView.setOnTouchListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    private boolean supportES2() {
        ActivityManager activityManager =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        return (configurationInfo.reqGlEsVersion >= 0x20000);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
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

    public static class touch {
        public float x;
        public float y;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}