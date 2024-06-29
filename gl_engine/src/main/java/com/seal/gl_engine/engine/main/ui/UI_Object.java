package com.seal.gl_engine.engine.main.ui;

import com.seal.gl_engine.GamePageInterface;
import com.seal.gl_engine.OpenGLRenderer;
import com.seal.gl_engine.maths.Vec3;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public abstract class UI_Object {
    private final GamePageInterface creatorPage;
    protected static List<WeakReference<UI_Object>> allObjects = new ArrayList<>();

    //declare default touch callback
    private Function<Vec3, Void> callback = new Function<Vec3, Void>() {
        @Override
        public Void apply(Vec3 touch) {
            return null;
        }
    };

    protected UI_Object(GamePageInterface creatorPage) {
        this.creatorPage = creatorPage;
        allObjects.add(new WeakReference<>(this));
    }

    private String getCreatorClassName() {
        return (String) creatorPage.getClass().getName();
    }


    public static boolean checkTouch(float tx, float ty) {
        Iterator<WeakReference<UI_Object>> i = allObjects.iterator();
        while (i.hasNext()) {
            WeakReference<UI_Object> u = i.next();
            if (u.get() == null) {
                i.remove();
            } else {
                if (u.get().getCreatorClassName().equals(OpenGLRenderer.getPageClassName())) {
                    if (u.get().checkHitbox(tx, ty)) {
                        u.get().callback.apply(new Vec3(tx, ty, 0));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected abstract void draw();

    protected abstract boolean checkHitbox(float tx, float ty);

}
