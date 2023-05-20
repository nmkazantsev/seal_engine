package com.manateam.main.adaptors;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glVertexAttribPointer;

import android.opengl.GLES30;

import com.manateam.glengine3.engine.main.shaders.Adaptor;
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
    private int normalLocation;
    private int tangetntLocation, bitangentLocation;

    private final static int POSITION_COUNT = 3;
    private static final int TEXTURE_COUNT = 2;
    private static final int NORMAL_COUNT = 3;
    private static final int TANGENT_VEC = 3;
    private static final int BITANGENT_VEC = 3;
    private static final int STRIDE = (POSITION_COUNT
            + TEXTURE_COUNT + NORMAL_COUNT ) * 4; //+ TANGENT_VEC + BITANGENT_VEC

    @Override
    public int bindData(Face[] faces) {
        float[] vertices = new float[faces.length * (faces[0].verticesNumber())];// + TANGENT_VEC + BITANGENT_VEC
        int vertexesNumber = 0;
        for (int i = 0; i < faces.length; i++) {
            System.arraycopy(faces[i].getArrayRepresentation(), 0, vertices, i * (faces[i].verticesNumber()), faces[i].verticesNumber());// + TANGENT_VEC + BITANGENT_VEC

            //calculate tangent space (https://learnopengl.com/Advanced-Lighting/Normal-Mapping)
            Vec3 edge1 = (faces[i].vertices[1].toVec3().minus(faces[i].vertices[0].toVec3()));
            Vec3 edge2 = (faces[i].vertices[2].toVec3().minus(faces[i].vertices[0].toVec3()));
            Vec3 deltaUV1 = (faces[i].textureCoordinates[0].toVec3().minus(faces[i].textureCoordinates[1].toVec3()));
            Vec3 deltaUV2 = (faces[i].textureCoordinates[2].toVec3().minus(faces[i].textureCoordinates[1].toVec3()));
            float f = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV2.x * deltaUV1.y);

            Vec3 tangent = new Vec3(), bitangent = new Vec3();
            tangent.x = f * (deltaUV2.y * edge1.x - deltaUV1.y * edge2.x);
            tangent.y = f * (deltaUV2.y * edge1.y - deltaUV1.y * edge2.y);
            tangent.z = f * (deltaUV2.y * edge1.z - deltaUV1.y * edge2.z);

            bitangent.x = f * (-deltaUV2.x * edge1.x + deltaUV1.x * edge2.x);
            bitangent.y = f * (-deltaUV2.x * edge1.y + deltaUV1.x * edge2.y);
            bitangent.z = f * (-deltaUV2.x * edge1.z + deltaUV1.x * edge2.z);
            //push this to list
            /*System.arraycopy(new float[]{tangent.x, tangent.y, tangent.z, bitangent.x, bitangent.y,
                            bitangent.z}, 0, vertices, i * (faces[i].verticesNumber() + TANGENT_VEC + BITANGENT_VEC) + faces[i].verticesNumber(),
                    TANGENT_VEC + BITANGENT_VEC); //we skip i segments and place for main data in i+1 and then paste

*/
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

        /*vertexData.position(POSITION_COUNT + TEXTURE_COUNT + NORMAL_COUNT);
        glVertexAttribPointer(tangetntLocation, TANGENT_VEC, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(tangetntLocation);

        vertexData.position(POSITION_COUNT + TEXTURE_COUNT + NORMAL_COUNT + TANGENT_VEC);
        glVertexAttribPointer(bitangentLocation, BITANGENT_VEC, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(bitangentLocation);*/

        return vertexesNumber;
    }

    @Override
    public void updateLocations() {
        aPositionLocation = glGetAttribLocation(programId, "aPos");
        aTextureLocation = glGetAttribLocation(programId, "aTexCoord");
        normalLocation = glGetAttribLocation(programId, "normalVec");
        tangetntLocation = glGetAttribLocation(programId, "aT");
        bitangentLocation = glGetAttribLocation(programId, "aB");
        uTextureUnitLocation = glGetUniformLocation(programId, "u_TextureUnit");
        projectionMatrixLoation = GLES30.glGetUniformLocation(programId, "projection");
        viewMatrixLocation = GLES30.glGetUniformLocation(programId, "view");
        modelMtrixLocation = GLES30.glGetUniformLocation(programId, "model");

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
}
