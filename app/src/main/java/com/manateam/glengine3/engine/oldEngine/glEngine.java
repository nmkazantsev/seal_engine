package com.manateam.glengine3.engine.oldEngine;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.aPositionLocation;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.aTextureLocation;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.uTextureUnitLocation;

public class glEngine {
    private final static int POSITION_COUNT = 3;
    private static final int TEXTURE_COUNT = 2;
    private static final int STRIDE = (POSITION_COUNT
            + TEXTURE_COUNT) * 4;

    public static void bindData(glShape s) {
        s.vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COUNT, GL_FLOAT,
                false, STRIDE, s.vertexData);
        glEnableVertexAttribArray(aPositionLocation);


        s.vertexData.position(POSITION_COUNT);
        glVertexAttribPointer(aTextureLocation, TEXTURE_COUNT, GL_FLOAT,
                false, STRIDE, s.vertexData);
        glEnableVertexAttribArray(aTextureLocation);

        glActiveTexture(GL_TEXTURE0);

        // glBindTexture(GL_TEXTURE_2D, s.texture);
        glUniform1i(uTextureUnitLocation, 0);
    }

    public static void prepareAndDraw(glShape shape, float[] vectries) {
        if (vectries != null) {
            shape.prepareData(vectries);
            bindData(shape);
            if (!shape.isPostToGlNeeded()) {
                glBindTexture(GL_TEXTURE_2D, shape.texture);
            }
            if (shape.isPostToGlNeeded()) {
                shape.postToGL();
            }
            glDrawArrays(GL_TRIANGLES, 0, vectries.length / 5);
        }
    }

    public static void prepareAndDraw(glShape shape, float n, float m, float rot, float px, float py, float sizx, float sizy, float z) {
        shape.prepareData(rot, n, m, px, py, sizx, sizy, z);
        bindData(shape);
        if (!shape.isPostToGlNeeded()) {
            glBindTexture(GL_TEXTURE_2D, shape.texture);
        }
        if (shape.isPostToGlNeeded()) {
            shape.postToGL();
        }
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }

    public static void prepareAndDraw(glShape shape, float px, float py, float sizx, float sizy, float z) {
        shape.prepareData(px, py, sizx, sizy, z);
        bindData(shape);
        if (!shape.isPostToGlNeeded()) {
            glBindTexture(GL_TEXTURE_2D, shape.texture);
        }
        if (shape.isPostToGlNeeded()) {
            shape.postToGL();
        }
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }

    public static void prepareAndDraw(glShape shape, float rot, float px, float py, float sizx, float sizy, float z) {
        shape.prepareData(rot, px, py, sizx, sizy, z);
        bindData(shape);
        if (!shape.isPostToGlNeeded()) {
            glBindTexture(GL_TEXTURE_2D, shape.texture);
        }
        if (shape.isPostToGlNeeded()) {
            shape.postToGL();
        }
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }
}
