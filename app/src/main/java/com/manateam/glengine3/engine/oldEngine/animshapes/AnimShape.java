package com.manateam.glengine3.engine.oldEngine.animshapes;

import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static com.manateam.glengine3.engine.oldEngine.glEngine.bindData;
import static com.manateam.glengine3.utils.Utils.millis;
import static com.manateam.glengine3.utils.Utils.parseInt;

import android.util.Log;

import com.manateam.glengine3.GamePageInterface;
import com.manateam.glengine3.engine.main.images.PImage;
import com.manateam.glengine3.engine.oldEngine.glShape;

import java.lang.ref.WeakReference;
import java.util.List;

class AnimShape {
    //на вход принимается атлас из текстур, каждая из которых является кадром.
    //атлас должен быть прямоугольник, а кадры одинаковыми

    //!!!!!!!разрешение атласа не более 2048*2048 пкс

    //этот шейп хранит весь набор изображений, атлас
    private glShape images;
    //текущий кард. Изменяется от нуля до площади атласа в кадрах
    int cadr = 0;
    //последняя смена кадра
    long prevframe = -1;
    //частота кадров
    float frameRate;
    //сколько всего кадров в этом атласе
    int numCadrs = 0;
    //размеры кадров. ВАЖНО! указать как долю от размера аталаса, не выходить за пределы [0,1]
    //например, 0.5 на 0.5. (Тогда на атласе всего 4 кадра)
    float cadrSizeX, cadrSizeY;
    //стороны прямоугольника
    int cadrsX, cadrsY;
    private GamePageInterface page;

    //если true, то после тогда автоповтор отключается и после заврешения isPlaying станет false
    public boolean playOnce = false;
    public boolean isPlaying = true;

    public AnimShape(PImage img, float cadrSizeX, float cadrSizeY, float frameRate,GamePageInterface page) {
        this.page=page;
        this.cadrSizeX = cadrSizeX;
        this.cadrSizeY = cadrSizeY;
        this.frameRate = frameRate;
        if (img.width > 2048 || img.height > 2048) {
            //не критично, просто приложение словит outOfMemory))
            Log.e("слишком большой алтас", "ограничение 2048*2048 пкс");
        }
        uploadImg(img);
        //сохраняем атлас из изображения в текстуру, с ней проще
        cadrsX = parseInt(img.width) / parseInt(img.width * cadrSizeX);
        cadrsY = parseInt(img.height) / parseInt(img.height * cadrSizeY);
        numCadrs = cadrsX * cadrsY;
    }

    //НЕ ЗАБЫТЬ ВЫЗВАТЬ В REDRAWSETUP()!!
    public void uploadImg(PImage img) {
        images = null;
        images = new glShape(img.width, img.height, true,page);
        if (images.isRedrawNeeded()) {
            images.bitmap = img.bitmap;
            images.setRedrawNeeded(false);
        }
    }

    private boolean restartNeeded = false;//чтобы рестартнуть точно при отрисовке

    public void restart() {
        restartNeeded = true;
    }

    public void cancelRestart() {
        restartNeeded = false;
    }

    public void draw(float px, float py, float a, float b, float z) {

        if (restartNeeded) {
            isPlaying = true;
            cadr = 0;
            prevframe = millis();
            restartNeeded = false;
        }

        if (isPlaying) {
            if (prevframe == -1) {
                //чтобы начинать с первого кадра
                prevframe = millis();
            }
            if (millis() - prevframe > 1000 / frameRate) {
                //время переключать кадры
                prevframe = millis();
                cadr++;
                if (playOnce) {
                    //если дошли до последнего кадра
                    if (cadr >= numCadrs) {
                        isPlaying = false;
                        cadr -= 1;
                    }
                }
                cadr %= numCadrs;
            }
        }
        //это координаты (в кадрах) выбранного для показа изобрадения
        int cx = cadr % cadrsX;
        int cy = cadr / cadrsX;
        //а это обычный препареанддров, только тут на полигон накладывают не всю текстуру, а тлолько ее часть,
        //которая и является нужным кадром
        prepareAndDraw(images, px, py, a, b, z, cx * cadrSizeX, cy * cadrSizeY, cadrSizeX, cadrSizeY);
    }

    private void prepareAndDraw(glShape shape, float px, float py, float sizx, float sizy, float z,
                                float texx, float texy, float texa, float texb) {
        shape.prepareData(px, py, sizx, sizy, z, texx, texy, texa, texb);
        bindData(shape);
        if (!shape.isPostToGlNeeded()) {
            glBindTexture(GL_TEXTURE_2D, shape.texture);
        }
        if (shape.isPostToGlNeeded()) {
            shape.postToGL();
        }
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }

    public void delete() {
        images.deleteTexture();
    }
}
