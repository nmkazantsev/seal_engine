package com.manateam.main.adaptors;

import static android.opengl.GLES20.glGetUniformLocation;

import android.opengl.GLES30;

import com.manateam.glengine3.GamePageInterface;
import com.manateam.glengine3.engine.main.shaders.ShaderData;
import com.manateam.glengine3.maths.Vec3;

public class PointLight extends ShaderData {
    private int posLoc, diffuseLoc, ambLoc, specLoc, countLoc;
    private final int index;
    public Vec3 position;
    public float diffuse, ambient, specular;
    public static int count;

    public PointLight(int index, GamePageInterface g) {
        super(g);
        this.index = index;
    }

    @Override
    public void getLocations(int programId) {
        posLoc = glGetUniformLocation(programId, "pLights[" + index + "].lightPos");
        diffuseLoc = glGetUniformLocation(programId, "pLights[" + index + "].diffuse");
        ambLoc = glGetUniformLocation(programId, "pLights[" + index + "].ambinient");
        specLoc = glGetUniformLocation(programId, "pLights[" + index + "].specular");
        countLoc = glGetUniformLocation(programId, "pLightNum");
    }

    @Override
    public void forwardData() {
        GLES30.glUniform3f(posLoc, position.x, position.y, position.z);
        GLES30.glUniform1f(diffuseLoc, diffuse);
        GLES30.glUniform1f(ambLoc, ambient);
        GLES30.glUniform1f(specLoc, specular);
        GLES30.glUniform1i(countLoc, count);
    }
}
