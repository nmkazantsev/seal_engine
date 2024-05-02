package com.seal.gl_engine.engine.main.light;

import static android.opengl.GLES20.glGetUniformLocation;

import android.opengl.GLES30;

import com.seal.gl_engine.GamePageInterface;
import com.seal.gl_engine.engine.main.shaders.ShaderData;
import com.seal.gl_engine.maths.Vec3;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class DirectedLight extends ShaderData {

    public Vec3 color;
    public Vec3 direction;
    public float diffuse;
    public float specular;

    private int colorLoc, directionLoc, diffuseLoc, specLoc, numberLoc;
    private int index;
    private static final List<WeakReference<DirectedLight>> directLights = new ArrayList<>();

    public DirectedLight(GamePageInterface gamePageInterface) {
        super(gamePageInterface);
        index = directLights.size();
        directLights.add(new WeakReference<>(this));
    }

    //always use before apply shader
    public void deleteLight() {
        directLights.remove(index);
        for (int i = 0; i < directLights.size(); i++) {
            directLights.get(i).get().index = i; //locations will be updated when apply shader
        }
    }

    @Override
    public void getLocations(int programId) {
        colorLoc = glGetUniformLocation(programId, "dLights[" + index + "].color");
        directionLoc = glGetUniformLocation(programId, "dLights[" + index + "].direction");
        diffuseLoc = glGetUniformLocation(programId, "dLights[" + index + "].diffuse");
        specLoc = glGetUniformLocation(programId, "dLights[" + index + "].specular");
        numberLoc = glGetUniformLocation(programId, "dLightNum");
    }

    @Override
    public void forwardData() {
        GLES30.glUniform3f(directionLoc, direction.x, direction.y, direction.z);
        GLES30.glUniform3f(colorLoc, color.x, color.y, color.z);
        GLES30.glUniform1f(specLoc, specular);
        GLES30.glUniform1f(diffuseLoc, diffuse);
        GLES30.glUniform1i(numberLoc, directLights.size());
    }

    @Override
    public void delete() {
        directLights.remove(index);
    }
}
