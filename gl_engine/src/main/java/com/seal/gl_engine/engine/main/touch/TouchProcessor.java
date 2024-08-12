package com.seal.gl_engine.engine.main.touch;

import android.util.Log;
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
    private Integer touchId = -1;
    private final Class<?> creatorClassName;
    private static final HashMap<Integer, TouchProcessor> activeProcessors = new HashMap<>();
    private static final List<TouchProcessor> allProcessors = new ArrayList<>();
    private final Function<TouchPoint, Boolean> checkHitboxCallback;
    private final Function<TouchPoint, Void> touchStartedCallback;
    private final Function<TouchPoint, Void> touchMovedCallback;
    private final Function<TouchPoint, Void> touchEndedCallback; //here the last known touch point
    public TouchPoint lastTouchPoint = null;
    private static boolean pageChanged = false;
    private boolean touchAlive = false;

    //a class for queue of postponed (in nearest frame) callback (not all callbacks are allowed in touch thread, problems with openGL context)
    private static class Command {
        private final TouchPoint touchPoint;
        private final Function<TouchPoint, Void> function;

        private Command(TouchPoint t, Function<TouchPoint, Void> function) {
            this.touchPoint = t;
            this.function = function;
        }

        private void run() {
            function.apply(touchPoint);
        }
    }

    private static final List<Command> commandQueue = new ArrayList<>();

    /**
     * Create a new TouchProcessor
     *
     * @param checkHitboxCallback  called every time new touch starts. Must return true of this touch should init tracking for this object; else return false.
     * @param touchStartedCallback called when this object captures a new touch.
     * @param touchMovedCallback   called when captured touch moves.
     * @param touchEndedCallback   called when captured touch ends.
     * @param creatorPage          not null creator page object.
     */
    public TouchProcessor(Function<TouchPoint, Boolean> checkHitboxCallback,
                          Function<TouchPoint, Void> touchStartedCallback,
                          Function<TouchPoint, Void> touchMovedCallback,
                          Function<TouchPoint, Void> touchEndedCallback, GamePageClass creatorPage) {
        this.creatorClassName = creatorPage.getClass();
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
        touchAlive = false;
        activeProcessors.remove(touchId);
        Log.e("removed", String.valueOf(touchId));
        Log.e("size", String.valueOf(activeProcessors.size()));
        touchId = -1;
        if (touchEndedCallback != null) {
            //touchEndedCallback.apply(null);
            commandQueue.add(new Command(lastTouchPoint, touchEndedCallback));
        }
    }

    private boolean checkHitbox(TouchPoint event) {
        return checkHitboxCallback.apply(event);
    }

    //**********STATIC METHODS********************
    public static boolean onTouch(View v, MotionEvent event) {
        synchronized (commandQueue) {
            //  Log.e("event",event.toString());
            TouchProcessor t = activeProcessors.getOrDefault(event.getPointerId(event.getActionIndex()), null);
            //Log.e(event.toString(), String.valueOf(event.getPointerId(event.getActionIndex())));
            if (t != null) {
                if (t.creatorClassName == OpenGLRenderer.getPageClass()) {
                    if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                        touchMoved(event);
                    }
                    if (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP || event.getActionMasked() == MotionEvent.ACTION_UP) {
                        Log.e("touch ended", String.valueOf(event.getPointerId(event.getActionIndex())));
                        touchEnded(event);
                    }
                }
            } else if (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN || event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                Log.e("touch started", String.valueOf(event.getPointerId(event.getActionIndex())));
                touchStarted(event);
            }
        }
        return true; //a listener has reacted on event
    }

    public static void processMotions() {
        synchronized (commandQueue) {
            Iterator<Command> iterator = commandQueue.iterator();
            while (iterator.hasNext()) {
                Command command = iterator.next();
                //clean events if page changed
                if (pageChanged) {
                    iterator.remove(); //remove all without processing
                    continue;
                }
                command.run();
                iterator.remove();//no need in this event to be buffered any more
            }
        }
        pageChanged = false;
    }

    private static void touchStarted(MotionEvent event) {
        for (TouchProcessor t : allProcessors) {
            //Log.e("checking", "touch hitbox " + t.checkHitbox(event) + " alive " + t.touchAlive + " x " + event.getX(event.getActionIndex()));
            if (t.checkHitbox(new TouchPoint(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()))) && (t.creatorClassName == OpenGLRenderer.getPageClass()) && !t.touchAlive) { //not to start the same processor twice if 2 touches in 1 area
                activeProcessors.put(event.getPointerId(event.getActionIndex()), t);
                t.lastTouchPoint = new TouchPoint(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()));
                t.touchAlive = true;
                t.touchId = event.getPointerId(event.getActionIndex());
                Log.e("adding", "new touch");
                if (t.touchStartedCallback != null) {
                    commandQueue.add(new Command(t.lastTouchPoint, t.touchStartedCallback));
                    //t.touchStartedCallback.apply(t.lastTouchPoint);
                }
                return;
            }
        }
    }

    private static void touchMoved(MotionEvent event) {
        // Log.e("touch moved", event.toString() + " "+event.getActionIndex());
        /*
        No other ways here to get indexes of touch moved are not specified in docs.
        Process all moved touches here.
         */
        for (int i = 0; i < event.getPointerCount(); i++) {
            if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                TouchProcessor t = activeProcessors.getOrDefault(event.getPointerId(i), null);
                if (t != null) {
                    t.lastTouchPoint = new TouchPoint(event.getX(i), event.getY(i));
                    // Log.e("touch moved", String.valueOf(t.lastTouchPoint.touchY));
                    if (t.touchMovedCallback != null) {
                        //t.touchMovedCallback.apply(t.lastTouchPoint);
                        commandQueue.add(new Command(t.lastTouchPoint, t.touchMovedCallback));
                    }
                }
            }
        }
    }

    private static void touchEnded(MotionEvent event) {
        TouchProcessor t = activeProcessors.get(event.getPointerId(event.getActionIndex()));
        Log.e("fuond", String.valueOf(t.touchId));
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
                if (!(e.creatorClassName == OpenGLRenderer.getPageClass())) {
                    //do not call terminate here not to call touch ended
                    iterator2.remove();
                }
            }
        }
    }

    /**
     * Get if touch is pressed at the moment of calling this.
     *
     * @return true if finger is pressing, false if finger is released and this touch object is ready to capture new touch.
     */
    public boolean getTouchAlive() {
        return touchAlive;
    }
}

