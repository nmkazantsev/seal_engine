package com.seal.seal_engine.engine.main.vertex_bueffer;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glDeleteBuffers;
import static android.opengl.GLES30.glBindVertexArray;
import static android.opengl.GLES30.glDeleteVertexArrays;
import static android.opengl.GLES30.glGenBuffers;
import static android.opengl.GLES30.glGenVertexArrays;

public class VertexBuffer {
    private final int vao;
    private final int[] vbo;

    private final int vboNum;

    public VertexBuffer(int vboNum) {
        vbo = new int[vboNum];
        this.vboNum = vboNum;
        glGenBuffers(vboNum, vbo, 0);
        int x[] = new int[1];
        glGenVertexArrays(1, x, 0);
        vao = x[0];
    }

    public void bindVbo(int vboInd) {
        glBindBuffer(GL_ARRAY_BUFFER, vbo[vboInd]);
    }

    public void bindDefaultVbo() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void bindVao() {
        glBindVertexArray(vao);
    }

    public void bindDefaultVao() {
        glBindVertexArray(0);
    }

    public int getVboAdress(int vboIndex) {
        return vbo[vboIndex];
    }

    public void delete() {
        glDeleteBuffers(vboNum, vbo, 0);
        int x[] = new int[1];
        x[0] = vao;
        glDeleteVertexArrays(1, x, 0);
    }

}
