package com.seal.gl_engine.engine.main.frameBuffers;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glDeleteFramebuffers;
import static android.opengl.GLES20.glDeleteRenderbuffers;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES30.GL_RGBA16F;

import android.opengl.GLES20;

import com.seal.gl_engine.engine.main.VRAMobject;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.vertices.Face;
import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.maths.PVector;

public class FrameBuffer extends VRAMobject {
    protected int texture;
    protected int depth;
    protected int frameBuffer;
    protected int w;
    protected int h;

    // https://www.programcreek.com/java-api-examples/?class=android.opengl.GLES20&method=glBindFramebuffer
    public FrameBuffer(int width, int height, GamePageClass page) {
        super(page);
        this.w = width;
        this.h = height;
        onRedrawSetup();
    }

    public void onRedrawSetup() {
        int[] frameBuffers = new int[1];
        int[] frameBufferTextures = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffers, 0);

        GLES20.glGenTextures(1, frameBufferTextures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameBufferTextures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GL_RGBA16F,
                w, h, 0,
                GLES20.GL_RGBA, GLES20.GL_FLOAT, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, frameBufferTextures[0], 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        int[] depthBuffer = new int[1];
        GLES20.glGenRenderbuffers(1, depthBuffer, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthBuffer[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, w, h);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthBuffer[0]);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        frameBuffer = frameBuffers[0];
        depth = depthBuffer[0];
        texture = frameBufferTextures[0];
    }

    public void drawTexture(PVector a, PVector b, PVector d) {
        PVector c = new PVector(d.x + b.x - a.x, b.y + d.y - a.y, b.z + d.z - a.z);
        float[][] vertices = new float[][]{
                {a.x, a.y, a.z},
                {d.x, d.y, d.z},
                {b.x, b.y, b.z},
                {c.x, c.y, c.z}
        };

        float[][] textCoords = new float[][]{
                {1, 1},
                {1, 0},
                {0, 1},
                {0, 0},
        };
        Face face1 = new Face(
                new PVector[]{
                        new PVector(vertices[0][0], vertices[0][1], vertices[0][2]),
                        new PVector(vertices[1][0], vertices[1][1], vertices[1][2]),
                        new PVector(vertices[2][0], vertices[2][1], vertices[2][2]),
                },
                new PVector[]{
                        new PVector(textCoords[0][0], textCoords[0][1]),
                        new PVector(textCoords[1][0], textCoords[1][1]),
                        new PVector(textCoords[2][0], textCoords[2][1]),
                },
                new PVector(0, 0, 1));
        Face face2 = new Face(
                new PVector[]{
                        new PVector(vertices[1][0], vertices[1][1], vertices[1][2]),
                        new PVector(vertices[2][0], vertices[2][1], vertices[2][2]),
                        new PVector(vertices[3][0], vertices[3][1], vertices[3][2]),
                },
                new PVector[]{
                        new PVector(textCoords[1][0], textCoords[1][1]),
                        new PVector(textCoords[2][0], textCoords[2][1]),
                        new PVector(textCoords[3][0], textCoords[3][1]),
                },
                new PVector(0, 0, 1));
        Shader.getActiveShader().getAdaptor().bindData(new Face[]{face1, face2});
        //place texture to target 2D of unit 0
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }

    public int getFrameBuffer() {
        return frameBuffer;
    }

    public int getDepth() {
        return depth;
    }

    public int getTexture() {
        return texture;
    }

    public void setH(int h) {
        this.h = h;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }

    public void delete() {
        glDeleteFramebuffers(1, new int[]{getFrameBuffer()}, 0);
        glDeleteRenderbuffers(1, new int[]{getDepth()}, 0);
        glDeleteTextures(1, new int[]{getTexture()}, 0);
    }

    @Override
    public void reload() {
        onRedrawSetup();
    }

    public void apply() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }
    public static void connectDefaultFrameBuffer() {
        // switch to the buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }
}
