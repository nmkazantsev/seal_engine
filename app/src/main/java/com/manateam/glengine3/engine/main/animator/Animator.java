package com.manateam.glengine3.engine.main.animator;

import static com.manateam.glengine3.utils.Utils.contactArray;
import static com.manateam.glengine3.utils.Utils.millis;

import com.manateam.glengine3.engine.main.engine_object.EnObject;

import java.util.function.Function;

public class Animator {
    private static Animator animatorsQueue[];
    private boolean active;
    private Animation animQueue[];
    private EnObject target;
    public Animator (EnObject obj) {
        if (animatorsQueue == null) animatorsQueue = new Animator[]{this};
        else {
            Animator b[] = new Animator[animatorsQueue.length + 1];
            for (int i = 0; i < animatorsQueue.length; i++) animatorsQueue[i] = b[i];
            b[animatorsQueue.length] = this;
            animatorsQueue = b;
        }
        active = true;
        animQueue = new Animation[]{};
        target = obj;
    }

    public boolean getActive() {
        return active;
    }

    public void activate() {
        active = true;
    }

    public void inactivate() {
        active = false;
    }

    // todo make it be applied on the EnObject
    public void goThroughAnimQueue() {
        for (Animation a: this.animQueue) a.getAnimMatrix(target.getMatrix());
    }

    public static class Animation {

        private Function<float[], float[]> tf;
        private Function<float[], Object> vf;
        private float[] args;
        private float duration;
        // velocity function argument
        private float vfa;
        private long startTiming;
        // tf - transformation function, vf - velocity function
        public Animation(Function tf, float[] args, Function vf, float duration, float vfa) {
            startTiming = millis();
            this.tf = tf;
            this.vf = vf;
            this.args = args;
            this.duration = duration;
            this.vfa = vfa;
        }

        @Override
        public void finalize() {
        }

        // todo find out is it possible to make functions take more than one argument
        public float[] getAnimMatrix(float[] matrix) {
            float t = (millis() - startTiming) / duration;
            float dt = t - (float) vf.apply(new float[]{t, vfa});
            if (t >= 1) finalize();
            float[] params = new float[16 + args.length];
            float a[] = contactArray(args, new float[]{dt});
            return tf.apply(contactArray(matrix, a));
        }
    }
}
