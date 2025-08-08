package com.seal.gl_engine.engine.main.light;

import static android.opengl.GLES20.glGetUniformLocation;

import android.opengl.GLES30;

import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.engine.main.shaders.ShaderData;
import com.seal.gl_engine.maths.PVector;

public class Material extends ShaderData {
    public PVector ambient;
    public PVector diffuse;
    public PVector specular;
    public float shininess;
    private int ambLoc, diffLoc, specLoc, shininessLoc;

    public Material(GamePageClass gamePageClass) {
        super(gamePageClass);
    }

    @Override
    protected void getLocations(int programId) {
        ambLoc = glGetUniformLocation(programId, "material.ambient");
        diffLoc = glGetUniformLocation(programId, "material.diffuse");
        specLoc = glGetUniformLocation(programId, "material.specular");
        shininessLoc = glGetUniformLocation(programId, "material.shininess");
    }

    @Override
    protected void forwardData() {
        //no automatic application on shader enable
    }

    @Override
    protected void delete() {
    }

    public void apply() {
        GLES30.glUniform3f(ambLoc, ambient.x, ambient.y, ambient.z);
        GLES30.glUniform3f(diffLoc, diffuse.x, diffuse.y, diffuse.z);
        GLES30.glUniform3f(specLoc, specular.x, specular.y, specular.z);
        GLES30.glUniform1f(shininessLoc, shininess);
    }
}
