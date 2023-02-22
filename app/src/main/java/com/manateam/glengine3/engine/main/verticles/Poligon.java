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
import com.manateam.glengine3.engine.main.shaders.Shader;
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
public class Poligon implements VerticleSet, DrawableShape {
    private boolean saveMemory;
    private String creatorClassName;
    float[] vertexes, textCoords;
    private Texture texture;
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
        this.saveMemory = saveMemory;
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
        vertexes = new float[]{
                a.x, a.y, a.z,
                d.x, d.y, d.z,
                b.x, b.y, b.z,
                c.x, c.y, c.z
        };

        textCoords = new float[]{
                0, 0,
                0, 1,
                1, 0,
                1, 1
        };
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
        vertexes = new float[]{
                x, y, z,
                x + a, y, z,
                x, y + b, z,

                x, y + b, z,
                x + a, y + b, z,
                x + a, y, z
        };
        textCoords = new float[]{
                texx, texy,
                texx + texa, texy,
                texx, texy + texb,

                texx, texy + texb,
                texx + texa, texy + texb,
                texx + texa, texy
        };
    }

    private void bindData() {

        Shader.getActiveShader().getAdaptor().bindData(this);
        // помещаем текстуру в target 2D юнита 0
        glActiveTexture(GL_TEXTURE0);
        if (!postToGlNeeded) {
            glBindTexture(GL_TEXTURE_2D, texture.getId());
        }
        if (postToGlNeeded) {
            postToGl();
        }
        // юнит текстуры
        glUniform1i(Shader.getActiveShader().getAdaptor().getTextureLocation(), 0);

    }

    private void postToGl() {
        if (redrawNeeded||image.isLoaded()) {
            redrawNow();
        }
        postToGlNeeded = false;
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getId());
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GL_RGBA, image.bitmap, 0);
        if (saveMemory) {
            image.delete();
        }
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
        image.setLoaded(true);
        setRedrawNeeded(false);
    }

    @Override
    public String getCreatorClassName() {
        return creatorClassName;
    }

    public void redrawNow() {
        onRedraw();
    }

    @Override
    public float[] getVertexData() {
        return vertexes;
    }

    @Override
    public float[] getTextureData() {
        return textCoords;
    }
}
