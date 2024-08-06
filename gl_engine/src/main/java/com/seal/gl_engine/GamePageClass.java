package com.seal.gl_engine;

public abstract class GamePageClass {
    public GamePageClass(){
        OpenGLRenderer.resetPageMillis();
    }
    public abstract void draw();
}
