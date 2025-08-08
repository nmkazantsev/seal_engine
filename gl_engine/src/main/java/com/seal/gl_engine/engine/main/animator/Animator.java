package com.seal.gl_engine.engine.main.animator;

import static com.seal.gl_engine.utils.Utils.contactArray;

import com.seal.gl_engine.OpenGLRenderer;
import com.seal.gl_engine.engine.main.engine_object.SealObject;
import com.seal.gl_engine.utils.Utils;

import java.util.HashMap;
import java.util.function.Function;

public class Animator {

    // indexes for constructing animation via template constructor
    public static final int
            SHIFT = 0,
            ROTATION = 1,
            PIVOT_ROTATION = 2,
            LINEAR = 0,
            SIGMOID = 1;
    private static HashMap<SealObject, Animation[]> animQueue;

    public static void initialize() {
        animQueue = new HashMap<>();
    }

    public static void freezeAnimations(SealObject target) {
        if (animQueue.get(target) == null) return;
        for (Animation a: animQueue.get(target)) {
            if (a.waiting) continue;
            a.waiting = true;
            a.frozen = false;
        }
    }
    public static void unfreezeAnimations(SealObject target) {
        if (animQueue.get(target) == null) return;
        for (Animation a: animQueue.get(target)) {
            if (!a.waiting) continue;
            a.waiting = false;
            a.elapsedTime = OpenGLRenderer.pageMillis() - a.freezeTiming;
        }
    }

    // template constructor itself, uses predefined indexes instead of manual function specifying
    public static void addAnimation(SealObject target, int tfType, float[] args, int vfType, float duration, float vfa, long st, boolean recurring) {
        Function<Animation, float[]> tf = null;
        Function<float[], Float> vf = null;

        // tfType - defines witch attribute of EnObject is affected by animation (posMatrix = 0; rotMatrix = 1, combined = 2)
        switch (tfType) {
            case 0:
                tf = FC::shift;
                break;
            case 1:
                tf = FC::rotate;
                break;
            case 2:
                tf = FC::pivotRotation;
                break;
        }

        switch (vfType) {
            case 0:
                vf = FC::linear;
                break;
            case 1:
                vf = FC::sigmoid;
                break;
        }

        new Animation(target, tf, args, vf, duration, vfa, st, recurring);
    }

    // adds animation without templates, every function has to be specified by hand
    /*
    Example of using specific functions.
    First argument is an EnObject instance that is being target of the animation.
    Second argument is a function that takes an instance of Animation and returns an array of 6 floats,
    first 3 are position, second 3 defines rotation
    (mention this are is not deltas, this are changed attribute).
    Third argument is a function that defines rate of affect on attributes, it takes array
    containing value from 0 to 1 (0 is the beginning of the animation, 1 is the very last moment)
    and some argument, the function must return value from 0 to 1 as well, as was mentioned before
    0 is fist position of the animation, 1 is the very last.
    Then goes duration, single velocity function attribute and start timing.
    Example of full function call from EnObject class:
    addAnimation(
        this,
        (Animator.Animation animation) -> {
            float[] attrs = animation.getAttrs();
            float[] args = animation.getArgs();
            return attrs;
        },
        new float[3],
        (float[] f) -> {
            float k = f[0];
            float a = f[1];
            return f[0];
        },
        1000,
        1.0f,
        5000
    );
     */
    public static void addAnimation(SealObject target, Function<Animation, float[]> tf, float[] args, Function<float[], Float> vf, float duration, float vfa, long st, boolean recurring) {
        new Animation(target, tf, args, vf, duration, vfa, st, recurring);
    }

    private static void listAnimation(Animation animation, SealObject target) {
        if (!animQueue.containsKey(target)) {
            animQueue.put(target, new Animation[]{animation});
            return;
        }
        Animation[] a = animQueue.get(target);
        if (a == null) animQueue.replace(target, new Animation[]{animation});
        else animQueue.replace(target, contactArray(a, new Animation[]{animation}));
    }

    public static void animate(SealObject target) {
        if (animQueue.get(target) == null) return;
        // getting targets space attributes
        float[] b = target.getSpaceAttrs();
        // getting array related to the object and going though it
        for (Animation animation : animQueue.get(target)) {
            if (!animation.isDead) {
                // giving attributes to the animator and getting computation result
                animation.setAttrs(b);
                b = animation.getAnimMatrix();
            } else {
                // deleting "dead" animation
                animQueue.replace(target, Utils.popFromArray(animQueue.get(target), animation));
            }
        }
        // writing affected attributes back
        target.setSpaceAttrs(b);
    }

    public static class Animation {
        private boolean isActive; // false only in case animation has not achieved start timing yet
        private boolean isDead; // becomes true if animation has worked out and ready to be deleted
        private final Function<Animation, float[]> tf; // transmission function
        private final Function<float[], Float> vf; // velocity function
        private final float[] args; // additional arguments
        private final float duration; // total duration
        // velocity function argument
        private final float vfa; // velocity function argument
        private long startTiming; // global start timing in millis
        private float dtBuffer, dt; // buffer for proper dt computing and dt itself (can't be local)
        private float[] attrs; // attributes like position and rotation
        private boolean waiting; // allows to freeze animation
        private long freezeTiming, elapsedTime; // needed to process freezes properly
        private boolean frozen; // technical nuance
        private final boolean recurring;
        private int loopCounter;

        private Animation(SealObject target, Function<Animation, float[]> tf, float[] args, Function<float[], Float> vf, float duration, float vfa, long st, boolean recurring) {
            long c = OpenGLRenderer.pageMillis();
            if (st <= c) {
                startTiming = c;
                isActive = true;
            } else {
                startTiming = st;
                isActive = false;
            }
            this.tf = tf;
            this.vf = vf;
            this.args = args;
            this.duration = duration;
            this.vfa = vfa;
            this.dtBuffer = 0;
            listAnimation(this, target);
            isDead = false;
            waiting = false;
            this.recurring = recurring;
        }

        public float[] getAttrs() {
            return attrs.clone();
        }

        public void setAttrs(float[] attrs) {
            this.attrs = attrs;
        }

        public float[] getArgs() {
            return args.clone();
        }

        public float getDeltaT() {
            return dt;
        }

        // function that returns changes in attributes according to current time and arguments
        public float[] getAnimMatrix() {
            if (waiting) {
                if (!frozen) {
                    frozen = true;
                    freezeTiming = OpenGLRenderer.pageMillis();
                }
                return attrs;
            }
            if (!isActive) {
                if (startTiming + elapsedTime <= OpenGLRenderer.pageMillis()) {
                    isActive = true;
                    return getAnimMatrix();
                }
                return attrs;
            }
            float gt = (OpenGLRenderer.pageMillis() - (startTiming + elapsedTime)) / duration; // global timing (linear from 0 to 1)
            float t = vf.apply(new float[]{gt, vfa}); // velocity function output for gt
            dt = t - dtBuffer; // difference in current and previous vf output (shift delta)
            dtBuffer = t;
            if (gt >= 1) {
                if (recurring) {
                    startTiming = OpenGLRenderer.pageMillis();
                } else isDead = true; // completion
            }
            return tf.apply(this);
        }
    }
}
