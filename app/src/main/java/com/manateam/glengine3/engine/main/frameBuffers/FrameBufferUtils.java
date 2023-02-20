package com.manateam.glengine3.engine.main.frameBuffers;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static com.manateam.glengine3.engine.main.frameBuffers.FrameBuffer.allFrameBuffers;

import android.opengl.GLES20;

import com.manateam.glengine3.GamePageInterface;
import com.manateam.glengine3.OpenGLRenderer;

import java.lang.ref.WeakReference;
import java.util.Iterator;

public class FrameBufferUtils {


    // https://www.programcreek.com/java-api-examples/?class=android.opengl.GLES20&method=glBindFramebuffer
    public static FrameBuffer createFrameBuffer(int width, int height, GamePageInterface page) {
        int[] frameBuffers = new int[1];
        int[] frameBufferTextures = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffers, 0);

        GLES20.glGenTextures(1, frameBufferTextures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameBufferTextures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, frameBufferTextures[0], 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        int depthBuffer[] = new int[1];
        GLES20.glGenRenderbuffers(1, depthBuffer, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthBuffer[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthBuffer[0]);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        FrameBuffer f = new FrameBuffer(frameBuffers[0], depthBuffer[0], frameBufferTextures[0], page);
        f.setH(height);
        f.setW(width);
        return f;

    }

    public static void connectFrameBuffer(int frameBuffer) {
        // switch to the buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public static void connectDefaultFrameBuffer() {
        // switch to the buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }


    public static void onPageChanged() {
        Iterator<WeakReference<FrameBuffer>> iterator = allFrameBuffers.iterator();
        while (iterator.hasNext()) {
            WeakReference<FrameBuffer> e = iterator.next();
            if (!(e.get() == null)) {
                if (e.get().getCreatorClassName() != null) {
                    if (!e.get().getCreatorClassName().equals(OpenGLRenderer.getPageClassName())) {
                        e.get().delete();
                        iterator.remove();
                    }
                }
            }
        }
    }
}
