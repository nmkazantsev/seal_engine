package com.manateam.glengine3.engine.main.vertex_bueffer;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES30.glBindVertexArray;
import static android.opengl.GLES30.glGenBuffers;
import static android.opengl.GLES30.glGenVertexArrays;

public class VertexBuffer {
    private int vao;
    private int[] vbo;

    public VertexBuffer(int vboNum) {
        vbo = new int[vboNum];
        glGenBuffers(vboNum, vbo, 0);
        int x[] = new int[1];
        glGenVertexArrays(1, x, 0);
        vao = x[0];
    }

    public void bindVbo(int vboNum) {
        glBindBuffer(GL_ARRAY_BUFFER, vbo[vboNum]);
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

}
