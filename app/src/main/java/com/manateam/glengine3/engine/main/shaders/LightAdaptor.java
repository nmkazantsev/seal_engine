package com.manateam.glengine3.engine.main.shaders;

public abstract class LightAdaptor {
    protected LightAdaptor() {
        Shader.getActiveShader().getAdaptor().addLightAdaptor(this);
    }

    protected int programId;

    public abstract void getLocations(int programId);

    public abstract void forwardData();
}
