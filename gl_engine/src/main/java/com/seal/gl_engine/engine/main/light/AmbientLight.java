package com.seal.gl_engine.engine.main.light;

import static android.opengl.GLES20.glGetUniformLocation;

import android.opengl.GLES30;

import com.seal.gl_engine.GamePageInterface;
import com.seal.gl_engine.engine.main.shaders.ShaderData;
import com.seal.gl_engine.maths.Vec3;

public class AmbientLight extends ShaderData {
    private int aLightLocation; //link to color data
    public Vec3 color = new Vec3(0, 0, 0);

    public AmbientLight(GamePageInterface gamePageInterface) {
        super(gamePageInterface);
    }

    @Override
    public void getLocations(int programId) {
        aLightLocation = glGetUniformLocation(programId, "aLight.color");
    }

    @Override
    public void forwardData() {
        GLES30.glUniform3f(aLightLocation, color.x, color.y, color.z);
    }

}

