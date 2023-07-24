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

    private static void listAnimation(Animation animation) {
        if (animQueue == null) {
            animQueue = new HashMap<>();
            animQueue.put(animation.target, new Animation[]{animation});
            return;
        }
        if (!animQueue.containsKey(animation.target)) {
            animQueue.put(animation.target, new Animation[]{animation});
            return;
        }
        Animation[] a = animQueue.get(animation.target);
        if (a == null) animQueue.replace(animation.target, new Animation[]{animation});
        else animQueue.replace(animation.target, (Animation[]) contactArray(a, new Animation[]{animation}));
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
            }
        }
        target.setPosMatrix(p);
        target.setRotMatrix(r);
    }

    public static class Animation {
        private final int tfType;
        private final EnObject target;
        private Function<float[], float[]> tf; // transmission function
        private Function<float[], Float> vf; // velocity function
        private final float[] args; // additional arguments
        private final float duration; // total duration
        // velocity function argument
        private final float vfa; // velocity function argument
        private final long startTiming; // global start timing in millis
        private float buffer;

        public Animation(EnObject target, int tfType, float[] args, int vfType, float duration, float vfa) {
            this.tfType = tfType;
            /*
        tf - defines witch attribute of EnObject is affected by animation (posMatrix = 0; rotMatrix = 1)
        vf - defines witch velocity function will be used
        */
            startTiming = millis();
            this.target = target;
            switch (tfType) {
                case 0:
                    this.tf = params -> FC.shift((float[]) params);
                    break;
                case 1:
                    this.tf = params -> FC.rotate((float[]) params);
                    break;
            }
            switch (vfType) {
                case 0:
                    this.vf = params -> FC.linear((float[]) params);
                    break;
                case 1:
                    this.vf = params -> FC.sigmoid((float[]) params);
                    break;
            }
            this.args = args;
            this.duration = duration;
            this.vfa = vfa;
            this.buffer = 0;
            listAnimation(this);
        }

        @Override
        protected void finalize() {
            deleteAnimation(this);
        }

        public float[] getAnimMatrix(float[] matrix) {
            float gt = (millis() - startTiming) / duration; // global timing (linear from 0 to 1)
            float t = (float) vf.apply(new float[]{gt, vfa}); // velocity function output for gt
            float dt = t - buffer; // difference in current and previous vf output (shift delta)
            buffer = t;
            if (gt >= 1) finalize(); // completion
            float[] a = contactArray(args, new float[]{dt});
            return tf.apply(contactArray(matrix, a));
        }
    }
}
