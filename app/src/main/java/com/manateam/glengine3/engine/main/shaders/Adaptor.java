package com.manateam.glengine3.engine.main.shaders;


import com.manateam.glengine3.engine.main.verticles.Face;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class Adaptor {
    private static ArrayList<LightAdaptor> lightAdaptors = new ArrayList<>();
    protected int programId;

    protected void addLightAdaptor(LightAdaptor lightAdaptor) {
        lightAdaptors.add(lightAdaptor);
    }

    public static void updateLightLocations() {
        Iterator<LightAdaptor> iterator = lightAdaptors.iterator();
        while (iterator.hasNext()) {
            LightAdaptor e = iterator.next();
            if (e == null) {
                iterator.remove();
            } else {
                e.getLocations(Shader.getActiveShader().getAdaptor().getProgramId());
            }
        }
    }


    public void setProgramId(int id) {
        this.programId = id;
    }

    public int getProgramId() {
        return this.programId;
    }

    public abstract int bindData(Face faces[]);

    public abstract void updateLocations();

    public abstract int getTransformMatrixLocation();

    public abstract int getCameraLocation();

    public abstract int getProjectionLocation();

    public abstract int getTextureLocation();

    public abstract int getNormalTextureLocation();

    public abstract int getCameraPosLlocation();
}
