package com.seal.gl_engine.default_adaptors;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glVertexAttribPointer;

import android.opengl.GLES30;

import com.seal.gl_engine.engine.main.shaders.Adaptor;
import com.seal.gl_engine.engine.main.vertex_bueffer.VertexBuffer;
import com.seal.gl_engine.engine.main.vertices.Face;
import com.seal.gl_engine.maths.PVector;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class LightShaderAdaptor extends Adaptor {

    private int normalMapEnableLocation;
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

    private void loadDataToBuffer(float[] vertices, int bufferIndex, VertexBuffer vertexBuffer) {
        FloatBuffer vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);//4 байта на флоат
        vertexBuffer.bindVbo(bufferIndex);//vertex coords
        vertexData.position(0);
        glBufferData(GL_ARRAY_BUFFER, vertices.length * 4, vertexData, GL_STATIC_DRAW);
    }

    @Override
    public int bindData(Face[] faces, VertexBuffer vertexBuffer, boolean vboLoaded) {
        if (!vboLoaded) {
            //set up positions
            float[] vertices = new float[faces.length * faces[0].vertices.length * 3];//3 because 3angle
            for (int i = 0; i < faces.length; i++) {
                vertices[i * 9] = faces[i].vertices[0].x;
                vertices[i * 9 + 1] = faces[i].vertices[0].y;
                vertices[i * 9 + 2] = faces[i].vertices[0].z;
                vertices[i * 9 + 3] = faces[i].vertices[1].x;
                vertices[i * 9 + 4] = faces[i].vertices[1].y;
                vertices[i * 9 + 5] = faces[i].vertices[1].z;
                vertices[i * 9 + 6] = faces[i].vertices[2].x;
                vertices[i * 9 + 7] = faces[i].vertices[2].y;
                vertices[i * 9 + 8] = faces[i].vertices[2].z;
            }
            loadDataToBuffer(vertices, 0, vertexBuffer);

            //set up uv
            vertices = new float[faces.length * faces[0].textureCoordinates.length * 3];//3 because 3angle
            for (int i = 0; i < faces.length; i++) {
                vertices[i * 6] = faces[i].textureCoordinates[0].x;
                vertices[i * 6 + 1] = 1 - faces[i].textureCoordinates[0].y;
                vertices[i * 6 + 2] = faces[i].textureCoordinates[1].x;
                vertices[i * 6 + 3] = 1 - faces[i].textureCoordinates[1].y;
                vertices[i * 6 + 4] = faces[i].textureCoordinates[2].x;
                vertices[i * 6 + 5] = 1 - faces[i].textureCoordinates[2].y;
            }
            loadDataToBuffer(vertices, 1, vertexBuffer);

            //set up normals
            vertices = new float[faces.length * 3 * 3];//3 because 3 coords in normal
            for (int i = 0; i < faces.length; i++) {
                for (int g = 0; g < 3; g++) {
                    vertices[i * 9 + g * 3] = faces[i].normal.x;
                    vertices[i * 9 + g * 3 + 1] = faces[i].normal.y;
                    vertices[i * 9 + g * 3 + 2] = faces[i].normal.z;
                }
            }
            loadDataToBuffer(vertices, 2, vertexBuffer);
            //set up tangent
            vertices = new float[faces.length * 3 * 3];//3 because 3 coords in normal
            for (int i = 0; i < faces.length; i++) {
                for (int g = 0; g < 3; g++) {
                    vertices[i * 9 + g * 3] = faces[i].tangent.x;
                    vertices[i * 9 + g * 3 + 1] = faces[i].tangent.y;
                    vertices[i * 9 + g * 3 + 2] = faces[i].tangent.z;
                }

            }
            loadDataToBuffer(vertices, 3, vertexBuffer);

            //set up bi tangent
            vertices = new float[faces.length * 3 * 3];//3 because 3 coords in normal
            for (int i = 0; i < faces.length; i++) {
                for (int g = 0; g < 3; g++) {
                    vertices[i * 9 + g * 3] = faces[i].bitangent.x;
                    vertices[i * 9 + g * 3 + 1] = faces[i].bitangent.y;
                    vertices[i * 9 + g * 3 + 2] = faces[i].bitangent.z;
                }
            }
            loadDataToBuffer(vertices, 4, vertexBuffer);
        }
        vertexBuffer.bindVao();
        glEnableVertexAttribArray(aPositionLocation);
        glEnableVertexAttribArray(aTextureLocation);
        glEnableVertexAttribArray(normalLocation);
        glEnableVertexAttribArray(tangetntLocation);
        glEnableVertexAttribArray(bitangentLocation);
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.getVboAdress(0));
        glVertexAttribPointer(aPositionLocation, 3, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.getVboAdress(1));
        glVertexAttribPointer(aTextureLocation, 2, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.getVboAdress(2));
        glVertexAttribPointer(normalLocation, 3, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.getVboAdress(3));
        glVertexAttribPointer(tangetntLocation, 3, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.getVboAdress(4));
        glVertexAttribPointer(bitangentLocation, 3, GL_FLOAT, false, 0, 0);

        vertexBuffer.bindDefaultVbo();//vertex coords
        vertexBuffer.bindDefaultVao();
        return 0;
    }

    @Override
    public void bindDataLine(PVector a, PVector b , PVector color) {

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
        normalMapEnableLocation = GLES30.glGetUniformLocation(programId, "normalMapEnable");
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
    public int getNormalMapEnableLocation() {
        return normalMapEnableLocation;
    }

    @Override
    public int getCameraPosLlocation() {
        return cameraPosLocation;
    }
}
