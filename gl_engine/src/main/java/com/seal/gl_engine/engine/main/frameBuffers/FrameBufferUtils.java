package com.seal.gl_engine.engine.main.frameBuffers;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.glClear;

import android.opengl.GLES20;

import com.seal.gl_engine.GamePageClass;

public class FrameBufferUtils {
    // https://www.programcreek.com/java-api-examples/?class=android.opengl.GLES20&method=glBindFramebuffer
    public static FrameBuffer createFrameBuffer(int width, int height, GamePageClass page) {
        FrameBuffer f = new FrameBuffer(width, height, page);
        f.onRedrawSetup();
        return f;

    }

    public static void connectFrameBuffer(int frameBuffer) {
        // switch to the buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void connectFramebuffer(FrameBuffer fb) {
        connectFrameBuffer(fb.getFrameBuffer());
    }

    public static void connectDefaultFrameBuffer() {
        // switch to the buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }
}
