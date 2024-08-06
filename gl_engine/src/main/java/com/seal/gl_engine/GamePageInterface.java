package com.seal.gl_engine;

public abstract class GamePageInterface {
    public GamePageInterface(){
        OpenGLRenderer.resetPageMillis();
    }
   public abstract void initialize();
    public abstract void draw();
}
