package com.seal.gl_engine.engine.main.vertices;

/*
этот интерфейс отвечает за поведение всего, что яляется набором вершин.
Это может быть полигон, фигура или многоугольник - без разницы
Требование - это наличие вершин и привязанных к ним координат текстур (по формату glShape)
 */
public interface VerticesSet {
     void onRedrawSetup();

     void setRedrawNeeded(boolean redrawNeeded);

     boolean isRedrawNeeded();

     void onRedraw();

     String getCreatorClassName();

     void onFrameBegin();

     void delete();
}
