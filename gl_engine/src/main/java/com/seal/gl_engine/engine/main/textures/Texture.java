package com.seal.gl_engine.engine.main.textures;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;


import android.opengl.GLES20;

import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.engine.main.VRAMobject;


public class Texture extends VRAMobject {
    private int id;

    public Texture(GamePageClass creator) {
        super(creator);
        id = createTexture();
    }

    @Override
    public void delete() {
        glDeleteTextures(1, new int[]{id}, 0);//delete texture with id texture, offset zero, array length 1
    }

    protected int createTexture() {
        final int[] textureIds = new int[1];
        /*create an empty array of one element
        OpenGL ES will write a free texture number to this array,
        get a free texture name, which will be written to names[0]*/
        glGenTextures(1, textureIds, 0);
        if (textureIds[0] == 0) {
            return 0;
        }
        // настройка объекта текстуры
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureIds[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // сброс target
        glBindTexture(GL_TEXTURE_2D, 0);

        return textureIds[0];
    }

    public void reload() {
        id = createTexture();//create it once again. Do not delete it, you may delete a previously created in this loop texture
    }

    public int getId() {
        return id;
    }
}
