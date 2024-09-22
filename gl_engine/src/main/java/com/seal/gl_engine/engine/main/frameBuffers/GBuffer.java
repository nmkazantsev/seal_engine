package com.seal.gl_engine.engine.main.frameBuffers;

import static android.opengl.GLES20.GL_COLOR_ATTACHMENT0;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES30.glDrawBuffers;

import android.opengl.GLES20;

import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.verticles.Face;
import com.seal.gl_engine.maths.Point;

import java.nio.IntBuffer;

public class GBuffer extends FrameBuffer {
    private final int[] textures;

    public GBuffer(float width, float height, int texturesCount, GamePageClass page) {
        super(width, height, page);
        textures = new int[texturesCount];
    }

    @Override
    public void onRedrawSetup() {
        int[] frameBuffers = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffers, 0);

        frameBuffer = frameBuffers[0];

        GLES20.glGenTextures(textures.length, textures, 0);
        int[] attachments = new int[textures.length];
        for (int i = 0; i < textures.length; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    w, h, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.getFrameBuffer());
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0 + i,
                    GLES20.GL_TEXTURE_2D, textures[i], 0);

            attachments[i] = GL_COLOR_ATTACHMENT0 + i;
        }

        int[] depthBuffer = new int[1];
        GLES20.glGenRenderbuffers(1, depthBuffer, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthBuffer[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, w, h);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthBuffer[0]);
        glDrawBuffers(textures.length, IntBuffer.wrap(attachments));

    }

    @Override
    public void apply() {
        FrameBufferUtils.connectFrameBuffer(getFrameBuffer());
    }

    @Override
    public void drawTexture(Point a, Point b, Point d) {
        this.drawTexture(0, a, b, d);
    }

    public void drawTexture(int textNum, Point a, Point b, Point d) {
         /*
        a-----b
        |     |
        |     |
        d-----c
         */
        Point c = new Point(d.x + b.x - a.x, b.y + d.y - a.y, b.z + d.z - a.z);
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
                new Point[]{
                        new Point(vertices[0][0], vertices[0][1], vertices[0][2]),
                        new Point(vertices[1][0], vertices[1][1], vertices[1][2]),
                        new Point(vertices[2][0], vertices[2][1], vertices[2][2]),
                },
                new Point[]{
                        new Point(textCoords[0][0], textCoords[0][1]),
                        new Point(textCoords[1][0], textCoords[1][1]),
                        new Point(textCoords[2][0], textCoords[2][1]),
                },
                new Point(0, 0, 1));
        Face face2 = new Face(
                new Point[]{
                        new Point(vertices[1][0], vertices[1][1], vertices[1][2]),
                        new Point(vertices[2][0], vertices[2][1], vertices[2][2]),
                        new Point(vertices[3][0], vertices[3][1], vertices[3][2]),
                },
                new Point[]{
                        new Point(textCoords[1][0], textCoords[1][1]),
                        new Point(textCoords[2][0], textCoords[2][1]),
                        new Point(textCoords[3][0], textCoords[3][1]),
                },
                new Point(0, 0, 1));
        Shader.getActiveShader().getAdaptor().bindData(new Face[]{face1, face2});
        //place texture to target 2D of unit 0
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textures[textNum]);
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }

    public void drawAllTextures(Point a, Point b, Point d) {
         /*
        a-----b
        |     |
        |     |
        d-----c
         */
        Point c = new Point(d.x + b.x - a.x, b.y + d.y - a.y, b.z + d.z - a.z);
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
                new Point[]{
                        new Point(vertices[0][0], vertices[0][1], vertices[0][2]),
                        new Point(vertices[1][0], vertices[1][1], vertices[1][2]),
                        new Point(vertices[2][0], vertices[2][1], vertices[2][2]),
                },
                new Point[]{
                        new Point(textCoords[0][0], textCoords[0][1]),
                        new Point(textCoords[1][0], textCoords[1][1]),
                        new Point(textCoords[2][0], textCoords[2][1]),
                },
                new Point(0, 0, 1));
        Face face2 = new Face(
                new Point[]{
                        new Point(vertices[1][0], vertices[1][1], vertices[1][2]),
                        new Point(vertices[2][0], vertices[2][1], vertices[2][2]),
                        new Point(vertices[3][0], vertices[3][1], vertices[3][2]),
                },
                new Point[]{
                        new Point(textCoords[1][0], textCoords[1][1]),
                        new Point(textCoords[2][0], textCoords[2][1]),
                        new Point(textCoords[3][0], textCoords[3][1]),
                },
                new Point(0, 0, 1));
        Shader.getActiveShader().getAdaptor().bindData(new Face[]{face1, face2});
        //place texture to target 2D of unit 0
        for (int i = 0; i < textures.length; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, textures[i]);
            glUniform1i(Shader.getActiveShader().getAdaptor().getTextureNumberNlocation(i), i);
        }
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glActiveTexture(GL_TEXTURE0);
    }
}