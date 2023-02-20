package com.manateam.glengine3.engine.main.verticles;

import static android.opengl.GLES20.glDeleteTextures;

import android.opengl.GLES20;
import android.util.Log;

import com.manateam.glengine3.OpenGLRenderer;

import java.lang.ref.WeakReference;

class TexturePoligonConnector {
    private WeakReference<VerticleSet> link;
    private int texture;
    private String creatorClassName;
    public TexturePoligonConnector(WeakReference<VerticleSet> link){
        this.link=link;
    }
    public void setTexture(int texture){
        this.texture=texture;
    }
    private boolean poligonDeleted=false;
    protected void deleteTextIfReferetNull(){
        if(creatorClassName==null){
            return;//this object is static
        }
        if( !creatorClassName.equals(OpenGLRenderer.getPageClassName())){//link.get()==null ||
            glDeleteTextures(1, new int[]{texture}, 0);//удалить текстуру с id texture, отступ ноль длина массива 1
            poligonDeleted=true;

        }
    }
    public void setCreatorClassName(String name){
        this.creatorClassName=name;
    }
    public boolean poligonDeleted(){
        return poligonDeleted;
    }
}
