package com.manateam.glengine3.engine.main.animator;

import static com.manateam.glengine3.utils.Utils.contactArray;
import static com.manateam.glengine3.utils.Utils.millis;

import android.graphics.drawable.AnimatedImageDrawable;

import com.manateam.glengine3.engine.main.engine_object.EnObject;

import java.util.HashMap;
import java.util.function.Function;

public class Animator {
    private static HashMap<EnObject, Animation[]> animQueue;

    private static void listAnimation(Animation animation) {
        if (animQueue == null) {
            animQueue = new HashMap<EnObject, Animation[]>();
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
    private static void deleteAnimation(EnObject target) {
        animQueue.remove(target);
    }
    public static void animate(EnObject target) {
        float[] m = target.getDrawMatrix();
        if (!animQueue.containsKey(target)) return;
        for (Animation a: animQueue.get(target)) {
            m = a.getAnimMatrix(m);
        }
        target.setDrawMatrix(m);
    }

    public static class Animation {

        private boolean state; // active/inactive
        private EnObject target;
        private Function<float[], float[]> tf; // transmission function
        private Function<float[], Object> vf; // velocity function
        private float[] args; // additional arguments
        private float duration; // total duration
        // velocity function argument
        private float vfa; // velocity function argument
        private long startTiming; // global start timing in millis
        private float buffer;
        public Animation(EnObject target, Function tf, float[] args, Function vf, float duration, float vfa) {
            startTiming = millis();
            this.target = target;
            this.tf = tf;
            this.vf = vf;
            this.args = args;
            this.duration = duration;
            this.vfa = vfa;
            this.buffer = 0;
            listAnimation(this);
        }

        @Override
        public void finalize() {
            deleteAnimation(target);
        }

        // todo find out is it possible to make functions take more than one argument
        public float[] getAnimMatrix(float[] matrix) {
            float gt = (millis() - startTiming) / duration; // global timing (linear from 0 to 1)
            float t = (float) vf.apply(new float[]{gt, vfa}); // velocity function output for gt
            float dt = t - buffer; // difference in current and previous vf output (shift delta)
            buffer = t;
            if (gt >= 1) finalize(); // completion
            float[] params = new float[16 + args.length];
            float[] a = contactArray(args, new float[]{dt});
            return tf.apply(contactArray(matrix, a));
        }
    }
}
