package com.manateam.glengine3.engine.main.animator;

import static com.manateam.glengine3.utils.Utils.contactArray;
import static com.manateam.glengine3.utils.Utils.millis;
import static com.manateam.glengine3.utils.Utils.popFromArray;

import com.manateam.glengine3.engine.main.engine_object.EnObject;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

public class Animator {

    // indexes for constructing animation via template constructor
    public static final int
            SHIFT = 0,
            ROTATION = 1,
            PIVOT_ROTATION = 2,
            LINEAR = 0,
            SIGMOID = 1;
    private static HashMap<EnObject, Animation[]> animQueue;

    public static void initialize() {
        animQueue = new HashMap<>();
    }

    // template constructor itself, uses predefined indexes instead of manual function specifying
    public static void addAnimation(EnObject target, int tfType, float[] args, int vfType, float duration, float vfa, long st) {
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

        new Animation(target, tf, args, vf, duration, vfa, st);
    }

    // adds animation without templates, every function has to be specified by hand
    public static void addAnimation(EnObject target, Function<Animation, float[]> tf, float[] args, Function<float[], Float> vf, float duration, float vfa, long st) {
        new Animation(target, tf, args, vf, duration, vfa, st);
    }

    private static void listAnimation(Animation animation, EnObject target) {
        if (!animQueue.containsKey(target)) {
            animQueue.put(target, new Animation[]{animation});
            return;
        }
        Animation[] a = animQueue.get(target);
        if (a == null) animQueue.replace(target, new Animation[]{animation});
        else animQueue.replace(target, contactArray(a, new Animation[]{animation}));
    }

    public static void animate(EnObject target) {
        // getting targets space attributes
        float[] b = target.getSpaceAttrs();
        // getting array related to the object and going though it
        for (Animation animation : Objects.requireNonNull(animQueue.get(target))) {
            if (!animation.isDead) {
                // giving attributes to the animator and getting computation result
                animation.setAttrs(b);
                b = animation.getAnimMatrix();
            } else {
                // deleting "dead" animation
                animQueue.replace(target, popFromArray(animQueue.get(target), animation));
            }
        }
        // writing affected attributes back
        target.setSpaceAttrs(b);
    }

    public static class Animation {
        private boolean isActive; // false only in case animation has not achieved start timing yet
        private boolean isDead; // become true if animation worked out and ready to be deleted
        private final Function<Animation, float[]> tf; // transmission function
        private final Function<float[], Float> vf; // velocity function
        private final float[] args; // additional arguments
        private final float duration; // total duration
        // velocity function argument
        private final float vfa; // velocity function argument
        private final long startTiming; // global start timing in millis
        private float dtBuffer, dt; // buffer for proper dt computing and dt itself (can't be local)
        private float[] attrs; // attributes like position and rotation

        private Animation(EnObject target, Function<Animation, float[]> tf, float[] args, Function<float[], Float> vf, float duration, float vfa, long st) {
            long c = millis();
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
            if (!isActive) {
                if (startTiming <= millis()) {
                    isActive = true;
                    return getAnimMatrix();
                }
                return attrs;
            }
            float gt = (millis() - startTiming) / duration; // global timing (linear from 0 to 1)
            float t = vf.apply(new float[]{gt, vfa}); // velocity function output for gt
            dt = t - dtBuffer; // difference in current and previous vf output (shift delta)
            dtBuffer = t;
            if (gt >= 1) isDead = true; // completion
            return tf.apply(this);
        }
    }
}
