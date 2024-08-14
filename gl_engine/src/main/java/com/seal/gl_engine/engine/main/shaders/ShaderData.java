package com.seal.gl_engine.engine.main.shaders;

import com.seal.gl_engine.GamePageClass;

public abstract class ShaderData {
    private final GamePageClass gamePageClass;

    protected ShaderData(GamePageClass gamePageClass) {
        this.gamePageClass = gamePageClass;
        Shader.getActiveShader().getAdaptor().addLightAdaptor(this);
    }


    protected Class<?> getCreatorClass() {
        if (gamePageClass != null) {
            return gamePageClass.getClass();

        }
        return null;
    }

    protected abstract void getLocations(int programId);

    protected abstract void forwardData();

    protected abstract void delete();
}
