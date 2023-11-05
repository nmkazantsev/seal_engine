package com.manateam.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.manateam.glengine3.Engine;

public class MainActivity extends Activity implements View.OnTouchListener {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GLSurfaceView v = Engine.onCreate(getApplicationContext());
        setContentView(v);
        assert v != null;
        v.setOnTouchListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Engine.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Engine.onResume();
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return Engine.onTouch(v, event);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}