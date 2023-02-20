package com.manateam.glengine3.engine.main.frameBuffers;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteFramebuffers;
import static android.opengl.GLES20.glDeleteRenderbuffers;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.aPositionLocation;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.aTextureLocation;

import android.opengl.GLES20;

import com.manateam.glengine3.GamePageInterface;
import com.manateam.glengine3.maths.Point;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FrameBuffer {
    protected static List<WeakReference<FrameBuffer>> allFrameBuffers = new ArrayList<>();
    private int texture, depth, frameBuffer;
    private int w;
    private int h;
    private String creatorClassName;

    public FrameBuffer(int frameBuffer, int depth, int texture, GamePageInterface page) {
        this.frameBuffer = frameBuffer;
        this.texture = texture;
        this.depth = depth;
        allFrameBuffers.add(new java.lang.ref.WeakReference<>(this));
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
        this.delete();
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
        int POSITION_COUNT = 3;
        int TEXTURE_COUNT = 2;
        int STRIDE = (POSITION_COUNT
                + TEXTURE_COUNT) * 4;
        FloatBuffer vertexData;
           /*
        a-----b
        |     |
        |     |
        d-----c
         */

        Point c = new Point(d.x + b.x - a.x, b.y + d.y - a.y, b.z + d.z - a.z);
        float[] vertices = {
                a.x, a.y, a.z, 0, 0,
                d.x, d.y, d.z, 0, 1,
                b.x, b.y, b.z, 1, 0,
                c.x, c.y, c.z, 1, 1
        };
        vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(aPositionLocation);

        // координаты текстур
        vertexData.position(POSITION_COUNT);
        glVertexAttribPointer(aTextureLocation, TEXTURE_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(aTextureLocation);

        // помещаем текстуру в target 2D юнита 0
        glBindTexture(GL_TEXTURE_2D, texture);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
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
