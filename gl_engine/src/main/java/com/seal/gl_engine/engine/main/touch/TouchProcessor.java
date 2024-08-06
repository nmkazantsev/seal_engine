package com.seal.gl_engine.engine.main.touch;

import android.view.MotionEvent;
import android.view.View;

import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.OpenGLRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * A class, created for tracking touch.
 * On every new ouch it's object is being bind (in accordance with defined hitbox) to some touch and object's callbacks will be called for certain touch.
 * All touch events are buffered and then processed in main thread, for it to be possible to call opengl functions
 */
public class TouchProcessor {
    private static final List<MotionEvent> eventsQueue = new ArrayList<>();
    private Integer touchId = -1;
    private final String creatorClassName;
    private static final HashMap<Integer, TouchProcessor> activeProcessors = new HashMap<>();
    private static final List<TouchProcessor> allProcessors = new ArrayList<>();
    private final Function<MotionEvent, Boolean> checkHitboxCallback;
    private final Function<TouchPoint, Void> touchStartedCallback;
    private final Function<TouchPoint, Void> touchMovedCallback;
    private final Function<Void, Void> touchEndedCallback;
    public TouchPoint lastTouchPoint = null;
    private static boolean pageChanged = false;

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
                          Function<Void, Void> touchEndedCallback, GamePageClass creatorPage) {
        this.creatorClassName = creatorPage.getClass().getName();
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
        if (touchEndedCallback != null) {
            touchEndedCallback.apply(null);
        }
    }

    private boolean checkHitbox(MotionEvent event) {
        return checkHitboxCallback.apply(event);
    }

    //**********STATIC METHODS********************
    public static boolean onTouch(View v, MotionEvent event) {
        synchronized (eventsQueue) {
            eventsQueue.add(event);
        }
        return true; //a listener has reacted on event
    }

    public static void processMotions() {
        synchronized (eventsQueue) {
            Iterator<MotionEvent> iterator = eventsQueue.iterator();
            while (iterator.hasNext()) {
                MotionEvent event = iterator.next();
                //clean events if page changed
                if (pageChanged) {
                    iterator.remove();
                    continue;
                }
                //if it is registered touch
                TouchProcessor t = activeProcessors.getOrDefault(event.getPointerId(event.getActionIndex()), null);
                if (t != null) {
                    if (t.creatorClassName.equals(OpenGLRenderer.getPageClassName())) {
                        if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                            touchMoved(event);
                        }
                        if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP || event.getActionMasked() == MotionEvent.ACTION_UP) {
                            touchEnded(event);
                        }
                    }
                } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN || event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    touchStarted(event);
                }
                iterator.remove();//no need in this event to be buffered any more
            }
        }
        pageChanged = false;
    }

    private static void touchStarted(MotionEvent event) {
        for (TouchProcessor t : allProcessors) {
            if (t.checkHitbox(event) && t.creatorClassName.equals(OpenGLRenderer.getPageClassName())) {
                activeProcessors.put(event.getPointerId(event.getActionIndex()), t);
                t.lastTouchPoint = new TouchPoint(event.getX(), event.getY());
                if (t.touchStartedCallback != null) {
                    t.touchStartedCallback.apply(t.lastTouchPoint);
                }
                return;
            }
        }
    }

    private static void touchMoved(MotionEvent event) {
        TouchProcessor t = activeProcessors.get(event.getPointerId(event.getActionIndex()));
        t.lastTouchPoint = new TouchPoint(event.getX(), event.getY());
        if (t.touchMovedCallback != null) {
            t.touchMovedCallback.apply(t.lastTouchPoint);
        }
    }

    private static void touchEnded(MotionEvent event) {
        TouchProcessor t = activeProcessors.get(event.getPointerId(event.getActionIndex()));
        t.terminate(event);
    }

    public static void onPageChange() {
        //clearing only through iterator, else concurrent modification error
        activeProcessors.clear();
        pageChanged = true;
        Iterator<TouchProcessor> iterator2 = allProcessors.iterator();
        while (iterator2.hasNext()) {
            TouchProcessor e = iterator2.next();
            if (e.creatorClassName != null) {
                if (!e.creatorClassName.equals(OpenGLRenderer.getPageClassName())) {
                    //do not call terminate here not to call touch ended
                    iterator2.remove();
                }
            }
        }
    }
}

