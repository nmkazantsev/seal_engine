package com.seal.gl_engine.engine.main.light;

import static android.opengl.GLES20.glGetUniformLocation;

import android.opengl.GLES30;

import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.engine.main.shaders.ShaderData;
import com.seal.gl_engine.maths.PVector;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SourceLight extends ShaderData {
    public PVector color;
    public PVector position;
    public PVector direction;
    public float diffuse;
    public float specular;
    public float constant, linear, quadratic, cutOff, outerCutOff;

    private int colorLoc, posLoc, diffuseLoc, specLoc, numberLoc, constLoc, linLoc, quadLoc, cutOffLoc, outerCutOffLoc, directionLoc;
    private int index;
    private static final List<WeakReference<SourceLight>> sourceLights = new ArrayList<>();
    private final WeakReference<SourceLight> thisRef;//link to this object for deleting later


    public SourceLight(GamePageClass gamePageClass) {
        super(gamePageClass);
        index = sourceLights.size();
        thisRef = new WeakReference<>(this);
        sourceLights.add(thisRef);
    }

    //always use before apply shader
    public void deleteLight() {
        sourceLights.remove(index);
        for (int i = 0; i < sourceLights.size(); i++) {
            sourceLights.get(i).get().index = i; //locations will be updated when apply shader
        }
    }

    @Override
    protected void getLocations(int programId) {
        colorLoc = glGetUniformLocation(programId, "sLights[" + index + "].color");
        posLoc = glGetUniformLocation(programId, "sLights[" + index + "].position");
        diffuseLoc = glGetUniformLocation(programId, "sLights[" + index + "].diffuse");
        specLoc = glGetUniformLocation(programId, "sLights[" + index + "].specular");
        constLoc = glGetUniformLocation(programId, "sLights[" + index + "].constant");
        linLoc = glGetUniformLocation(programId, "sLights[" + index + "].linear");
        quadLoc = glGetUniformLocation(programId, "sLights[" + index + "].quadratic");
        numberLoc = glGetUniformLocation(programId, "sLightNum");
        directionLoc = glGetUniformLocation(programId, "sLights[" + index + "].direction");
        cutOffLoc = glGetUniformLocation(programId, "sLights[" + index + "].cutOff");
        outerCutOffLoc = glGetUniformLocation(programId, "sLights[" + index + "].outerCutOff");

    }

    @Override
    protected void forwardData() {
        GLES30.glUniform3f(posLoc, position.x, position.y, position.z);
        GLES30.glUniform3f(colorLoc, color.x, color.y, color.z);
        GLES30.glUniform3f(directionLoc, direction.x, direction.y, direction.z);
        GLES30.glUniform1f(specLoc, specular);
        GLES30.glUniform1f(diffuseLoc, diffuse);
        GLES30.glUniform1f(constLoc, constant);
        GLES30.glUniform1f(linLoc, linear);
        GLES30.glUniform1f(quadLoc, quadratic);
        GLES30.glUniform1f(cutOffLoc, cutOff);
        GLES30.glUniform1f(outerCutOffLoc, outerCutOff);
        GLES30.glUniform1i(numberLoc, sourceLights.size());
    }

    @Override
    protected void delete() {
        sourceLights.remove(thisRef);
    }
}
