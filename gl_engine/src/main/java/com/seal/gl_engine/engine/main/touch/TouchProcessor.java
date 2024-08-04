package com.seal.gl_engine.engine.main.touch;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.seal.gl_engine.GamePageInterface;
import com.seal.gl_engine.OpenGLRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * A class, created for tracking touch.
 * On every new ouch it's object is being bind (in accordance with defined hitbox) to some touch and object's callbacks will be called for certain touch.
 */
public class TouchProcessor {
    private Integer touchId = 0;
    private String creatorClassName = null;
    private static final HashMap<Integer, TouchProcessor> activeProcessors = new HashMap<>();
    private static final List<TouchProcessor> allProcessors = new ArrayList<>();
    private Function<MotionEvent, Boolean> checkHitboxCallback;
    private Function<TouchPoint, Void> touchStartedCallback;
    private Function<TouchPoint, Void> touchMovedCallback;
    private Function<Void, Void> touchEndedCallback;

    /**
     * Create a new TouchProcessor
     *
     * @param checkHitboxCallback  called every time new touch starts. Must return true of this touch should init tracking for this object; else return false.
     * @param touchStartedCallback called when this object captures a new touch.
     * @param touchMovedCallback   called when captured touch moves.
     * @param touchEndedCallback   called when captured touch ends.
     * @param creatorPage          not null creator page object.
     */
    public TouchProcessor(Function<MotionEvent, Boolean> checkHitboxCallback,
                          Function<TouchPoint, Void> touchStartedCallback,
                          Function<TouchPoint, Void> touchMovedCallback,
                          Function<Void, Void> touchEndedCallback, GamePageInterface creatorPage) {
        creatorClassName = (String) creatorPage.getClass().getName();
        this.checkHitboxCallback = checkHitboxCallback;
        this.touchStartedCallback = touchStartedCallback;
        this.touchMovedCallback = touchMovedCallback;
        this.touchEndedCallback = touchEndedCallback;
        allProcessors.add(this);
    }

    /**
     * A function that stops tracking current touch. May be called any time you wish.
     * Also called when touch ends.
     */
    public void terminate() {
        terminate(null);
    }

    private void terminate(MotionEvent event) {
        activeProcessors.remove(touchId);
        touchId = 0;
        touchEndedCallback.apply(null);
    }

    private boolean checkHitbox(MotionEvent event) {
        return checkHitboxCallback.apply(event);
    }

    private void delete() {
        allProcessors.remove(this);
        activeProcessors.remove(touchId);
    }

    //**********STATIC METHODS********************
    public static boolean onTouch(View v, MotionEvent event) {
        //if it is registered touch
        if (activeProcessors.getOrDefault(event.getPointerId(event.getActionIndex()), null) != null) {
            if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                touchMoved(event);
            }
            if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
                touchEnded(event);
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
            touchStarted(event);
        } else { //if not registered and not started
            Log.e("touch", "error in touch processor. Not registered event called with not touchStarted flag");
        }

        return true; //a listener has reacted on event
    }

    private static void touchStarted(MotionEvent event) {
        for (TouchProcessor t : allProcessors) {
            if (t.checkHitbox(event)) {
                activeProcessors.put(event.getPointerId(event.getActionIndex()), t);
                t.touchStartedCallback.apply(new TouchPoint(event.getX(), event.getY()));
                return;
            }
        }
    }

    private static void touchMoved(MotionEvent event) {
        activeProcessors.get(event.getPointerId(event.getActionIndex())).touchMovedCallback.apply(new TouchPoint(event.getX(), event.getY()));
    }

    private static void touchEnded(MotionEvent event) {
        activeProcessors.get(event.getPointerId(event.getActionIndex())).terminate(event);
    }

    protected static void onPageChange() {
        Iterator<TouchProcessor> iterator = allProcessors.iterator();
        while (iterator.hasNext()) {
            TouchProcessor e = iterator.next();
            if (e.creatorClassName != null) {
                if (!e.creatorClassName.equals(OpenGLRenderer.getPageClassName())) {
                    e.terminate();
                    e.delete();
                    iterator.remove();
                }
            }
        }
    }
}

