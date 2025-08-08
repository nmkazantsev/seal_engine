package com.seal.gl_engine.engine.main.shaders;


import com.seal.gl_engine.OpenGLRenderer;
import com.seal.gl_engine.engine.main.vertex_bueffer.VertexBuffer;
import com.seal.gl_engine.engine.main.vertices.Face;
import com.seal.gl_engine.maths.PVector;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class Adaptor {
    private static final ArrayList<ShaderData> shaderData = new ArrayList<>();
    protected int programId;

    protected static void addLightAdaptor(ShaderData shaderData) {
        Adaptor.shaderData.add(shaderData);
    }

    public static void updateShaderDataLocations() {
        Iterator<ShaderData> iterator = shaderData.iterator();
        while (iterator.hasNext()) {
            ShaderData e = iterator.next();
            if (e == null) {
                iterator.remove();
            } else if (e.getCreatorClass() != null && !(e.getCreatorClass() == OpenGLRenderer.getPageClass())) {
                e.delete();
                iterator.remove();
            } else {
                e.getLocations(Shader.getActiveShader().getAdaptor().getProgramId());
            }
        }
    }

    public static void forwardData() {
        for (ShaderData e : shaderData) {
            e.forwardData();
        }
    }


    public void setProgramId(int id) {
        this.programId = id;
    }

    public int getProgramId() {
        return this.programId;
    }

    public abstract int bindData(Face[] faces);

    public abstract int bindData(Face[] faces, VertexBuffer vertexBuffer, boolean vboLoaded);

    public abstract void bindDataLine(PVector a, PVector b, PVector color);

    public abstract void updateLocations();

    public abstract int getTransformMatrixLocation();

    public abstract int getCameraLocation();

    public abstract int getProjectionLocation();

    public abstract int getTextureLocation();

    public abstract int getNormalTextureLocation();

    public abstract int getNormalMapEnableLocation();

    public abstract int getCameraPosLlocation();
}
