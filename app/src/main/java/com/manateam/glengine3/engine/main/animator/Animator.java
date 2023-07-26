package com.manateam.glengine3.engine.main.animator;

import static com.manateam.glengine3.utils.Utils.contactArray;
import static com.manateam.glengine3.utils.Utils.millis;
import static com.manateam.glengine3.utils.Utils.popFromArray;

import com.manateam.glengine3.engine.main.engine_object.EnObject;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

public class Animator {

    public static final int
            SHIFT = 0,
            ROTATION = 1,
            LINEAR = 0,
            SIGMOID = 1;
    private static HashMap<EnObject, Animation[]> animQueue;

    public static void initialize() {
        animQueue = new HashMap<>();
    }

    public static void addAnimation(EnObject target, int tfType, float[] args, int vfType, float duration, float vfa, long st, Function<float[], float[]> customTF, Function<float[], Float> customVF) {
        Function<float[], float[]> tf = customTF;
        Function<float[], Float> vf = customVF;

        // tfType - defines witch attribute of EnObject is affected by animation (posMatrix = 0; rotMatrix = 1, combined = 2)
        if (customTF == null)
            switch (tfType) {
                case 0:
                    tf = FC::shift;
                    break;
                case 1:
                    tf = FC::rotate;
                    break;
            }

        if (customVF == null)
            switch (vfType) {
                case 0:
                    vf = FC::linear;
                    break;
                case 1:
                    vf = FC::sigmoid;
                    break;
            }

        new Animation(target, tfType, tf, args, vf, duration, vfa, st);
    }

    private static void listAnimation(Animation animation) {
        if (!animQueue.containsKey(animation.target)) {
            animQueue.put(animation.target, new Animation[]{animation});
            return;
        }
        Animation[] a = animQueue.get(animation.target);
        if (a == null) animQueue.replace(animation.target, new Animation[]{animation});
        else animQueue.replace(animation.target, contactArray(a, new Animation[]{animation}));
    }
    private static void deleteAnimation(Animation anim) {
        Animation[] a = animQueue.get(anim.target);
        assert a != null;
        animQueue.replace(anim.target, popFromArray(a, anim));
    }
    public static void animate(EnObject target) {
        float[] p = target.getPosMatrix();
        float[] r = target.getRotMatrix();
        for (Animation a: Objects.requireNonNull(animQueue.get(target))) {
            switch (a.tfType) {
                case 0:
                    p = a.getAnimMatrix(p);
                    break;
                case 1:
                    r = a.getAnimMatrix(r);
                    break;
                case 2:
                    float[] b = a.getAnimMatrix(contactArray(p, r));
                    System.arraycopy(b, 0, p, 0, 3);
                    System.arraycopy(b, 3, r, 0, 3);
            }
        }
        target.setPosMatrix(p);
        target.setRotMatrix(r);
    }

    public static class Animation {
        private boolean isActive;
        private final int tfType;
        private final EnObject target;
        private final Function<float[], float[]> tf; // transmission function
        private final Function<float[], Float> vf; // velocity function
        private final float[] args; // additional arguments
        private final float duration; // total duration
        // velocity function argument
        private final float vfa; // velocity function argument
        private final long startTiming; // global start timing in millis
        private float buffer;

        private Animation(EnObject target, int tfType, Function<float[], float[]> tf, float[] args, Function<float[], Float> vf, float duration, float vfa, long st) {
            this.tfType = tfType;
            long c = millis();
            if (st <= c) {
                startTiming = c;
                isActive = true;
            } else {
                startTiming = st;
                isActive = false;
            }
            this.target = target;
            this.tf = tf;
            this.vf = vf;
            this.args = args;
            this.duration = duration;
            this.vfa = vfa;
            this.buffer = 0;
            listAnimation(this);
        }

        public float[] getAnimMatrix(float[] matrix) {
            if (!isActive) {
                if (startTiming <= millis()) {
                    isActive = true;
                    return this.getAnimMatrix(matrix);
                }
                return matrix;
            }
            float gt = (millis() - startTiming) / duration; // global timing (linear from 0 to 1)
            float t = vf.apply(new float[]{gt, vfa}); // velocity function output for gt
            float dt = t - buffer; // difference in current and previous vf output (shift delta)
            buffer = t;
            if (gt >= 1) deleteAnimation(this); // completion
            float[] a = contactArray(args, new float[]{dt});
            return tf.apply(contactArray(matrix, a));
        }
    }
}
