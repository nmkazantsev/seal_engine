package com.manateam.glengine3.engine.main.verticles;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_STRIP;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.aPositionLocation;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.aTextureLocation;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.uTextureUnitLocation;

import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.manateam.glengine3.GamePageInterface;
import com.manateam.glengine3.engine.main.images.PImage;
import com.manateam.glengine3.engine.main.textures.Texture;
import com.manateam.glengine3.maths.Point;
import com.manateam.glengine3.utils.Utils;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

//это 3д glShape так теперь называется)
public class Poligon implements VerticleSet {
    private Texture texture;
    private String creatorClassName;
    private static FloatBuffer vertexData;
    private final static int POSITION_COUNT = 3;
    private static final int TEXTURE_COUNT = 2;
    private static final int STRIDE = (POSITION_COUNT
            + TEXTURE_COUNT) * 4;

    protected boolean postToGlNeeded = true;
    protected boolean redrawNeeded = true;
    public PImage image;
    public List<String> redrawParams = new ArrayList<>();//change it in the way you like

    private Function<List<String>, PImage> redrawFunction;


    public Poligon(Function<List<String>, PImage> redrawFunction, boolean saveMemory, int paramSize, GamePageInterface page) {
        this.redrawFunction = redrawFunction;
        VectriesShapesManager.allShapes.add(new WeakReference<>(this));//добавить ссылку на Poligon
        texture = new Texture(page);
        for (int i = 0; i < paramSize; i++) {
            redrawParams.add("");
        }
        redrawNow();
        creatorClassName = (String) page.getClass().getName();
        if (page == null) {
            creatorClassName = null;
        }
    }


    public void newParamsSize(int paramSize) {
        redrawParams = new ArrayList<String>();
        for (int i = 0; i < paramSize; i++) {
            redrawParams.add("");
        }
    }

    public void prepareData(Point a, Point b, Point d) {
        /*
        a-----b
        |     |
        |     |
        d-----c
         */

        Point c = new Point(d.x + b.x - a.x, b.y + d.y - a.y, b.z + d.z - a.z);
        float[] vertices = {
                a.x, a.y, a.z, 0, 0,
                d.x, d.y, d.z, 0, 1,
                b.x, b.y, b.z, 1, 0,
                c.x, c.y, c.z, 1, 1
        };

        vertexData = null;
        vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);
    }

    protected void prepareData(Point A, Point B, float texx, float texy, float texa, float texb) {
        /*
        a-----b
        |     |
        |     |
        d-----c
         */
        float x = A.x;
        float y = A.y;
        float a = A.x - B.x;
        float b = A.y - B.y;
        float z = A.z;
        float[] vertices = {
                x, y, z, texx, texy,
                x + a, y, z, texx + texa, texy,
                x, y + b, z, texx, texy + texb,

                x, y + b, z, texx, texy + texb,
                x + a, y + b, z, texx + texa, texy + texb,
                x + a, y, z, texx + texa, texy
        };

        vertexData = null;
        vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);
    }

    private void bindData() {
        try {
            // координаты вершин
            vertexData.position(0);
            glVertexAttribPointer(aPositionLocation, POSITION_COUNT, GL_FLOAT,
                    false, STRIDE, vertexData);
            glEnableVertexAttribArray(aPositionLocation);

            // координаты текстур
            vertexData.position(POSITION_COUNT);
            glVertexAttribPointer(aTextureLocation, TEXTURE_COUNT, GL_FLOAT,
                    false, STRIDE, vertexData);
            glEnableVertexAttribArray(aTextureLocation);

            // помещаем текстуру в target 2D юнита 0
            glActiveTexture(GL_TEXTURE0);
            if (!postToGlNeeded) {
                glBindTexture(GL_TEXTURE_2D, texture.getId());
            }
            if (postToGlNeeded) {
                postToGl();
            }

            // юнит текстуры
            glUniform1i(uTextureUnitLocation, 0);
        } catch (NullPointerException e) {
            //if image was deleted before it was reloaded
            redrawNow();
        }
    }

    private void postToGl() {
        postToGlNeeded=false;
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getId());
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GL_RGBA, image.bitmap, 0);
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

    public void prepareAndDraw(Point a, Point b, Point c) {
        prepareData(a, b, c);
        bindData();
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    }

    public void prepareAndDraw(Point a, Point b, float texx, float texy, float teexa, float texb) {
        prepareData(a, b, texx, texy, teexa, texb);
        bindData();
        glDrawArrays(GL_TRIANGLES, 0, 6);
    }

    @Override
    public void onRedrawSetup() {
        setRedrawNeeded(true);
    }

    @Override
    public void setRedrawNeeded(boolean redrawNeeded) {
        this.redrawNeeded = redrawNeeded;
        postToGlNeeded = true;
    }

    @Override
    public boolean isRedrawNeeded() {
        return redrawNeeded;
    }

    @Override
    public void onRedraw() {
        if (image != null) {
            image.delete();
        }
        this.image = redrawFunction.apply(redrawParams);
        setRedrawNeeded(false);
    }

    @Override
    public String getCreatorClassName() {
        return creatorClassName;
    }

    public void redrawNow() {
        onRedraw();
    }
}
