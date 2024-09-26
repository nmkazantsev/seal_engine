package com.seal.gl_engine.engine.main.frameBuffers;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.glClear;

import android.opengl.GLES20;

import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.utils.Utils;

public class FrameBufferUtils {
    // https://www.programcreek.com/java-api-examples/?class=android.opengl.GLES20&method=glBindFramebuffer
    public static FrameBuffer createFrameBuffer(float width, float height, GamePageClass page) {
        FrameBuffer f = new FrameBuffer(width, height, page);
        f.onRedrawSetup();
        return f;
    }

    public static GBuffer createGBuffer(int textureCount, float width, float height, GamePageClass page) {
        GBuffer f = new GBuffer(width, height, textureCount, page);
        f.onRedrawSetup();
        return f;
    }

    protected static void connectFrameBuffer(int frameBuffer) {
        // switch to the buffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    protected void connectFramebuffer(FrameBuffer fb) {
        GLES20.glViewport(0, 0, fb.getWidth(), fb.getHeight());
        connectFrameBuffer(fb.getFrameBuffer());
    }

    public static void connectDefaultFrameBuffer() {
        // switch to the buffer
        GLES20.glViewport(0, 0,(int) Utils.x,(int) Utils.y);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }
}
