package com.seal.seal_engine.engine.main.verticles;

/*
этот интерфейс отвечает за поведение всего, что яляется набором вершин.
Это может быть полигон, фигура или многоугольник - без разницы
Требование - это наличие вершин и привязанных к ним координат текстур (по формату glShape)
 */
public interface VerticleSet {
     void onRedrawSetup();

     void setRedrawNeeded(boolean redrawNeeded);

     boolean isRedrawNeeded();

     void onRedraw();

     String getCreatorClassName();

     void delete();
}
