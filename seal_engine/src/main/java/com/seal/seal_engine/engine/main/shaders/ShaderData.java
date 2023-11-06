package com.seal.seal_engine.engine.main.shaders;

import com.seal.seal_engine.GamePageInterface;

public abstract class ShaderData {
    private final GamePageInterface gamePageInterface;

    protected ShaderData(GamePageInterface gamePageInterface) {
        this.gamePageInterface = gamePageInterface;
        Shader.getActiveShader().getAdaptor().addLightAdaptor(this);
    }

    public String getCreatorClassName() {
        if(gamePageInterface!=null) {
            return gamePageInterface.getClass().getName();
        }
        return null;
    }

    public abstract void getLocations(int programId);

    public abstract void forwardData();
}
