package com.seal.seal_engine.engine.main.frameBuffers;

import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteFramebuffers;
import static android.opengl.GLES20.glDeleteRenderbuffers;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glDrawArrays;

import android.opengl.GLES20;

import com.seal.seal_engine.GamePageInterface;
import com.seal.seal_engine.engine.main.shaders.Shader;
import com.seal.seal_engine.engine.main.verticles.DrawableShape;
import com.seal.seal_engine.engine.main.verticles.Face;
import com.seal.seal_engine.maths.Point;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FrameBuffer implements DrawableShape {
    protected static List<WeakReference<FrameBuffer>> allFrameBuffers = new ArrayList<>();
    private int texture, depth, frameBuffer;
    private int w;
    private int h;
    private String creatorClassName;
    private float[][] vertexes,textCoords;

    public FrameBuffer(int frameBuffer, int depth, int texture, GamePageInterface page) {
        this.frameBuffer = frameBuffer;
        this.texture = texture;
        this.depth = depth;
        allFrameBuffers.add(new WeakReference<>(this));
        if (page != null) {
            this.creatorClassName = (String) page.getClass().getName();
        }
    }

    public String getCreatorClassName() {
        return creatorClassName;
    }

    public static void onRedraw() {
        for (int i = 0; i < allFrameBuffers.size(); i++) {
            if (allFrameBuffers.get(i).get() != null) {
                allFrameBuffers.get(i).get().onRedrawSetup();
            }
        }
        Iterator<WeakReference<FrameBuffer>> iterator = allFrameBuffers.iterator();
        while (iterator.hasNext()) {
            WeakReference<FrameBuffer> f = iterator.next();
            if (f.get() == null) {
                iterator.remove();
            }
        }
    }

    public void onRedrawSetup() {
        //this.delete();
        int[] frameBuffers = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffers, 0);

        this.frameBuffer = frameBuffers[0];

        int frameBufferTextures[] = new int[1];
        if (this.texture != 0) {
            GLES20.glGenTextures(1, frameBufferTextures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameBufferTextures[0]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    w, h, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, this.getFrameBuffer());
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, frameBufferTextures[0], 0);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        }

        this.texture = frameBufferTextures[0];

        int depthBuffer[] = new int[1];
        GLES20.glGenRenderbuffers(1, depthBuffer, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthBuffer[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, w, h);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthBuffer[0]);
    }

    public void drawTexture(Point a, Point b, Point d) {
        Point c = new Point(d.x + b.x - a.x, b.y + d.y - a.y, b.z + d.z - a.z);
        vertexes = new float[][]{
                {a.x, a.y, a.z},
                {d.x, d.y, d.z},
                {b.x, b.y, b.z},
                {c.x, c.y, c.z}
        };

        textCoords = new float[][]{
                {0, 0},
                {0, 1},
                {1, 0},
                {1, 1}
        };
        Face face1 = new Face(
                new Point[]{
                        new Point(vertexes[0][0], vertexes[0][1], vertexes[0][2]),
                        new Point(vertexes[1][0], vertexes[1][1], vertexes[1][2]),
                        new Point(vertexes[2][0], vertexes[2][1], vertexes[2][2]),
                },
                new Point[]{
                        new Point(textCoords[0][0], textCoords[0][1]),
                        new Point(textCoords[1][0], textCoords[1][1]),
                        new Point(textCoords[2][0], textCoords[2][1]),
                },
                new Point(0, 0, 1));
        Face face2 = new Face(
                new Point[]{
                        new Point(vertexes[1][0], vertexes[1][1], vertexes[1][2]),
                        new Point(vertexes[2][0], vertexes[2][1], vertexes[2][2]),
                        new Point(vertexes[3][0], vertexes[3][1], vertexes[3][2]),
                },
                new Point[]{
                        new Point(textCoords[1][0], textCoords[1][1]),
                        new Point(textCoords[2][0], textCoords[2][1]),
                        new Point(textCoords[3][0], textCoords[3][1]),
                },
                new Point(0, 0, 1));
        Shader.getActiveShader().getAdaptor().bindData(new Face[]{face1, face2});
        // помещаем текстуру в target 2D юнита 0
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
}