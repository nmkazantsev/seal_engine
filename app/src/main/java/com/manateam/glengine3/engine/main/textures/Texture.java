package com.manateam.glengine3.engine.main.textures;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;


import android.opengl.GLES20;

import com.manateam.glengine3.GamePageInterface;
import com.manateam.glengine3.OpenGLRenderer;

import java.util.Iterator;
import java.util.List;

public class Texture {
    private static List<Texture> textures;
    private int id;
    private String owner = null;

    Texture(GamePageInterface creator) {
        textures.add(this);
        owner = (String) creator.getClass().getName();
        if(creator==null){
            owner=null;
        }
        id = createTexture();
    }

    private int createTexture() {
        final int[] textureIds = new int[1];
        //создаем пустой массив из одного элемента
        //в этот массив OpenGL ES запишет свободный номер текстуры,
        // получаем свободное имя текстуры, которое будет записано в names[0]
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

    private String getCreatorClassName() {
        return (String) owner;
    }

    private void reload(){
        createTexture();//create it once again. Do not delete it, you may delete a previously created in this loop texture
    }

    public static void onPageChanged() {
        Iterator<Texture> iterator = textures.iterator();
        while (iterator.hasNext()) {
            Texture e = iterator.next();
            if (e.getCreatorClassName() != null) {
                if (!e.getCreatorClassName().equals(OpenGLRenderer.getPageClassName())) {
                    e.deleteTexture();
                    iterator.remove();
                }
            }
        }
    }
    public static void reloadAll(){
        Iterator<Texture> iterator = textures.iterator();
        while (iterator.hasNext()) {
            Texture e = iterator.next();
            e.reload();
        }
    }

    public void deleteTexture() {
        glDeleteTextures(1, new int[]{id}, 0);//удалить текстуру с id texture, отступ ноль длина массива 1
    }
}
