package com.manateam.glengine3.engine.main.shaders;

public abstract class ShaderData {
    protected ShaderData() {
        Shader.getActiveShader().getAdaptor().addLightAdaptor(this);
    }

    protected int programId;

    public abstract void getLocations(int programId);

    public abstract void forwardData();
}
