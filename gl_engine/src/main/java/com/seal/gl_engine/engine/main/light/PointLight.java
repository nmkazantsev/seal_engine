package com.seal.gl_engine.engine.main.light;

import static android.opengl.GLES20.glGetUniformLocation;

import android.opengl.GLES30;

import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.engine.main.shaders.ShaderData;
import com.seal.gl_engine.maths.PVector;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PointLight extends ShaderData {
    public PVector color;
    public PVector position;
    public float diffuse;
    public float specular;
    public float constant, linear, quadratic;

    private int colorLoc, posLoc, diffuseLoc, specLoc, numberLoc, constLoc, linLoc, quadLoc;
    private int index;
    private static final List<WeakReference<PointLight>> pointLights = new ArrayList<>();
    private final WeakReference<PointLight> thisRef;//link to this object for deleting later


    public PointLight(GamePageClass gamePageClass) {
        super(gamePageClass);
        index = pointLights.size();
        thisRef = new WeakReference<>(this);
        pointLights.add(thisRef);
    }

    //always use before apply shader
    public void deleteLight() {
        pointLights.remove(index);
        for (int i = 0; i < pointLights.size(); i++) {
            pointLights.get(i).get().index = i; //locations will be updated when apply shader
        }
    }

    @Override
    protected void getLocations(int programId) {
        colorLoc = glGetUniformLocation(programId, "pLights[" + index + "].color");
        posLoc = glGetUniformLocation(programId, "pLights[" + index + "].position");
        diffuseLoc = glGetUniformLocation(programId, "pLights[" + index + "].diffuse");
        specLoc = glGetUniformLocation(programId, "pLights[" + index + "].specular");
        constLoc = glGetUniformLocation(programId, "pLights[" + index + "].constant");
        linLoc = glGetUniformLocation(programId, "pLights[" + index + "].linear");
        quadLoc = glGetUniformLocation(programId, "pLights[" + index + "].quadratic");
        numberLoc = glGetUniformLocation(programId, "pLightNum");
    }

    @Override
    protected void forwardData() {
        GLES30.glUniform3f(posLoc, position.x, position.y, position.z);
        GLES30.glUniform3f(colorLoc, color.x, color.y, color.z);
        GLES30.glUniform1f(specLoc, specular);
        GLES30.glUniform1f(diffuseLoc, diffuse);
        GLES30.glUniform1f(constLoc, constant);
        GLES30.glUniform1f(linLoc, linear);
        GLES30.glUniform1f(quadLoc, quadratic);
        GLES30.glUniform1i(numberLoc, pointLights.size());
    }

    @Override
    protected void delete() {
        pointLights.remove(thisRef);
    }
}


