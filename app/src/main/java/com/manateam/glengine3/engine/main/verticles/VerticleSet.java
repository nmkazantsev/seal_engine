package com.manateam.glengine3.engine.main.verticles;

/*
этот интерфейс отвечает за поведение всего, что яляется набором вершин.
Это может быть полигон, фигура или многоугольник - без разницы
Требование - это наличие вершин и привязанных к ним координат текстур (по формату glShape)
 */
interface VerticleSet {
     void onRedrawSetup();

     void setRedrawNeeded(boolean redrawNeeded);

     boolean isRedrawNeeded();

     void onRedraw();

     String getCreatorClassName();
}
