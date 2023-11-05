package com.manateam.main.adaptors;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glVertexAttribPointer;

import android.opengl.GLES30;

import com.manateam.glengine3.engine.main.shaders.Adaptor;
import com.manateam.glengine3.engine.main.vertex_bueffer.VertexBuffer;
import com.manateam.glengine3.engine.main.verticles.DrawableShape;
import com.manateam.glengine3.engine.main.verticles.Face;
import com.manateam.glengine3.maths.Vec3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Vector;

public class LightShaderAdaptor extends Adaptor {

    private int aPositionLocation;
    private int aTextureLocation;
    private int uTextureUnitLocation;
    private int projectionMatrixLoation;
    private int viewMatrixLocation;
    private int modelMtrixLocation;
    private int normalLocation, normalMapLocation;
    private int tangetntLocation, bitangentLocation, cameraPosLocation;

    private final static int POSITION_COUNT = 3;
    private static final int TEXTURE_COUNT = 2;
    private static final int NORMAL_COUNT = 3;
    private static final int TANGENT_VEC = 3;
    private static final int BITANGENT_VEC = 3;
    private static final int STRIDE = (POSITION_COUNT
            + TEXTURE_COUNT + NORMAL_COUNT + TANGENT_VEC + BITANGENT_VEC) * 4;

    @Override
    public int bindData(Face[] faces) {
        float[] vertices = new float[faces.length * (faces[0].verticesNumberTangentSpace())];
        int vertexesNumber = 0;
        for (int i = 0; i < faces.length; i++) {
            System.arraycopy(faces[i].getArrayRepresentationTangentSpace(), 0, vertices, i * (faces[i].verticesNumberTangentSpace()), faces[i].verticesNumberTangentSpace());
            vertexesNumber++;
        }
        FloatBuffer vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);//4 байта на флоат
        // координаты вершин
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(aPositionLocation);

        // координаты текстур
        vertexData.position(POSITION_COUNT);
        glVertexAttribPointer(aTextureLocation, TEXTURE_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(aTextureLocation);

        vertexData.position(POSITION_COUNT + TEXTURE_COUNT);
        glVertexAttribPointer(normalLocation, NORMAL_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(normalLocation);

        vertexData.position(POSITION_COUNT + TEXTURE_COUNT + NORMAL_COUNT);
        glVertexAttribPointer(tangetntLocation, TANGENT_VEC, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(tangetntLocation);

        vertexData.position(POSITION_COUNT + TEXTURE_COUNT + NORMAL_COUNT + TANGENT_VEC);
        glVertexAttribPointer(bitangentLocation, BITANGENT_VEC, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(bitangentLocation);

        return vertexesNumber;
    }

    @Override
    public int bindData(Face[] faces, VertexBuffer vertexBuffer) {
        //todo here
        return 0;
    }

    @Override
    public void updateLocations() {
        aPositionLocation = glGetAttribLocation(programId, "aPos");
        aTextureLocation = glGetAttribLocation(programId, "aTexCoord");
        normalLocation = glGetAttribLocation(programId, "normalVec");
        tangetntLocation = glGetAttribLocation(programId, "aT");
        bitangentLocation = glGetAttribLocation(programId, "aB");
        uTextureUnitLocation = glGetUniformLocation(programId, "u_TextureUnit");
        normalMapLocation = glGetUniformLocation(programId, "normalMap");
        projectionMatrixLoation = GLES30.glGetUniformLocation(programId, "projection");
        viewMatrixLocation = GLES30.glGetUniformLocation(programId, "view");
        modelMtrixLocation = GLES30.glGetUniformLocation(programId, "model");
        cameraPosLocation = GLES30.glGetUniformLocation(programId, "viewPos");
    }

    @Override
    public int getTransformMatrixLocation() {
        return modelMtrixLocation;
    }

    @Override
    public int getCameraLocation() {
        return viewMatrixLocation;
    }

    @Override
    public int getProjectionLocation() {
        return projectionMatrixLoation;
    }

    @Override
    public int getTextureLocation() {
        return uTextureUnitLocation;
    }

    @Override
    public int getNormalTextureLocation() {
        return normalMapLocation;
    }

    @Override
    public int getCameraPosLlocation() {
        return cameraPosLocation;
    }
}
