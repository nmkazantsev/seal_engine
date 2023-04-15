package com.manateam.glengine3.engine.main.shaders;

import com.manateam.glengine3.engine.main.verticles.DrawableShape;
import com.manateam.glengine3.engine.main.verticles.Face;

public abstract class Adaptor {
    protected int programId;
    public void setProgramId(int id){
        this.programId=id;
    }
    public abstract int bindData(Face faces[]);
    public abstract void updateLocations();
    public abstract int getTransformMatrixLocation();
    public abstract int getCameraLocation();
    public abstract int getProjectionLocation();
    public abstract int getTextureLocation();
}
