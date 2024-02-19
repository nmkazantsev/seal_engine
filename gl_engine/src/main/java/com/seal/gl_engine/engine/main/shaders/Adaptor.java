package com.seal.gl_engine.engine.main.shaders;


import com.seal.gl_engine.engine.main.verticles.Face;
import com.seal.gl_engine.OpenGLRenderer;
import com.seal.gl_engine.engine.main.vertex_bueffer.VertexBuffer;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class Adaptor {
    private static ArrayList<ShaderData> shaderData = new ArrayList<>();
    protected int programId;

    protected void addLightAdaptor(ShaderData shaderData) {
        Adaptor.shaderData.add(shaderData);
    }

    public static void updateLightLocations() {
        Iterator<ShaderData> iterator = shaderData.iterator();
        while (iterator.hasNext()) {
            ShaderData e = iterator.next();
            if (e == null || (e.getCreatorClassName() != null && !e.getCreatorClassName().equals(OpenGLRenderer.getPageClassName()))) {
                iterator.remove();
            } else {
                e.getLocations(Shader.getActiveShader().getAdaptor().getProgramId());
            }
        }
    }


    public void setProgramId(int id) {
        this.programId = id;
    }

    public int getProgramId() {
        return this.programId;
    }

    public abstract int bindData(Face faces[]);

    public abstract int bindData(Face faces[], VertexBuffer vertexBuffer);

    public abstract void updateLocations();

    public abstract int getTransformMatrixLocation();

    public abstract int getCameraLocation();

    public abstract int getProjectionLocation();

    public abstract int getTextureLocation();

    public abstract int getNormalTextureLocation();

    public abstract int getCameraPosLlocation();
}
