package com.manateam.glengine3.engine.main.animator;

import com.manateam.glengine3.engine.main.verticles.DrawableShape;

import java.util.function.Function;

public class Animator {
    private static Animator animatorsQueue[];
    private boolean active;
    private Animation animQueue[];
    public Animator (DrawableShape obj) {
        if (animatorsQueue == null) animatorsQueue = new Animator[]{this};
        else {
            Animator b[] = new Animator[animatorsQueue.length + 1];
            for (int i = 0; i < animatorsQueue.length; i++) animatorsQueue[i] = b[i];
            b[animatorsQueue.length] = this;
            animatorsQueue = b;
        }
        active = true;
        animQueue = new Animation[]{};
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

    public static class Animation {

        // mm - motion matrix, mf - motion function
        public Animation(float[][] mm, Function mf) {}
    }
}
