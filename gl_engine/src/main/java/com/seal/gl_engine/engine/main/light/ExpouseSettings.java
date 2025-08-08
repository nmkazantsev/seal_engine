package com.seal.gl_engine.engine.main.light;

import android.opengl.GLES30;

import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.engine.main.shaders.ShaderData;

public class ExpouseSettings extends ShaderData {
    private int expouseLoc, gammaLoc;
    public float expouse, gamma;

    public ExpouseSettings(GamePageClass gamePageClass) {
        super(gamePageClass);
        expouse = 1;
        gamma = 1;
    }

    @Override
    protected void getLocations(int programId) {
        expouseLoc = GLES30.glGetUniformLocation(programId, "exposure");
        gammaLoc = GLES30.glGetUniformLocation(programId, "gamma");
    }

    @Override
    protected void forwardData() {
        GLES30.glUniform1f(expouseLoc, expouse);
        GLES30.glUniform1f(gammaLoc, gamma);
    }

    @Override
    protected void delete() {

    }
}
