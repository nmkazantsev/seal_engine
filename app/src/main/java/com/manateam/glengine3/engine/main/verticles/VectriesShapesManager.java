package com.manateam.glengine3.engine.main.verticles;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class VectriesShapesManager {
    protected static List<WeakReference<VerticleSet>> allShapesToRedraw = new ArrayList<>();
    protected static List<WeakReference<VerticleSet>> allShapes = new ArrayList<>();

    //called only after setup and redraws all shapes
    public static void redrawAllSetup() {
        allShapesToRedraw = new ArrayList<>();
        for (int i = 0; i < allShapes.size(); i++) {
            if (allShapes.get(i).get() != null) {
                if (allShapes.get(i).get().isRedrawNeeded()) {
                    allShapes.get(i).get().onRedraw();
                }
            }
        }
        Iterator<WeakReference<VerticleSet>> iterator = allShapes.iterator();
        while (iterator.hasNext()) {
            WeakReference<VerticleSet> e = iterator.next();
            if (e.get() == null) {
                iterator.remove();
            }
        }
    }

    //called after every draw loop is completed and redraws only that was asked to redraw
    public static void redrawAll() {
        for (int i = 0; i < allShapesToRedraw.size(); i++) {
            if (allShapesToRedraw.get(i).get() != null) {
                if (allShapesToRedraw.get(i).get().isRedrawNeeded()) {
                    allShapesToRedraw.get(i).get().onRedraw();
                }
            }
        }
        allShapesToRedraw.clear();
    }

    //called when redraw setup (not first start)
    public static void onRedrawSetup() {
        allShapesToRedraw = new ArrayList<>();
        for (int i = 0; i < allShapes.size(); i++) {
            if (allShapes.get(i).get() != null) {
                allShapes.get(i).get().onRedrawSetup();
            }
        }
        Iterator<WeakReference<VerticleSet>> iterator = allShapes.iterator();
        while (iterator.hasNext()) {
            WeakReference<VerticleSet> e = iterator.next();
            if (e.get() == null) {
                iterator.remove();
            }
        }
    }


}
