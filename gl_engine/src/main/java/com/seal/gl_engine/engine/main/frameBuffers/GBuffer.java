package com.seal.gl_engine.engine.main.frameBuffers;

import android.opengl.GLES20;

import com.seal.gl_engine.GamePageClass;

public class GBuffer extends FrameBuffer {

    public GBuffer(float width, float height, GamePageClass page) {
        super(width, height, page);
    }

    @Override
    public void onRedrawSetup() {
        int[] frameBuffers = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffers, 0);

        frameBuffer = frameBuffers[0];

        int[] frameBufferTextures = new int[1];
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

        int[] depthBuffer = new int[1];
        GLES20.glGenRenderbuffers(1, depthBuffer, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthBuffer[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, w, h);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthBuffer[0]);
    }
}
