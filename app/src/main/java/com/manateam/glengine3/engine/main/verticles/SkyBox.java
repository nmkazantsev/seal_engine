package com.manateam.glengine3.engine.main.verticles;

import static android.opengl.GLES10.GL_TRIANGLES;
import static android.opengl.GLES10.glDisable;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glUniform1i;
import static com.manateam.glengine3.utils.Utils.loadImage;

import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.manateam.glengine3.GamePageInterface;
import com.manateam.glengine3.engine.main.images.PImage;
import com.manateam.glengine3.engine.main.shaders.Shader;
import com.manateam.glengine3.engine.main.textures.CubeMap;
import com.manateam.glengine3.engine.main.textures.Texture;
import com.manateam.glengine3.maths.Point;
import com.manateam.glengine3.utils.Utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import kotlin.text.UStringsKt;

public class SkyBox implements VerticleSet {
    private boolean redrawNeeded = true, postToGlNeeded = true, isLoaded = false;
    private PImage[] images = new PImage[6];
    private final Function<Integer, PImage> redrawFunction = this::loadTexture;
    private final String textureFileName, res;
    private GamePageInterface creator;
    private CubeMap texture;
    private Obj object;
    private Face[] faces;

    public SkyBox(String fileName, String textureFileName, String res, GamePageInterface page) {
        this.res = res;
        creator = page;
        this.textureFileName = textureFileName;
        VectriesShapesManager.allShapes.add(new java.lang.ref.WeakReference<>(this));//добавить ссылку на Poligon
        texture = new CubeMap(page);
        new Thread(() -> {
            InputStreamReader inputStream;
            try {
                inputStream = new InputStreamReader(Utils.context.getAssets().open(fileName), StandardCharsets.UTF_8);
                object = ObjUtils.convertToRenderable(
                        ObjReader.read(inputStream));
            } catch (IOException e) {
                e.printStackTrace();
            }
            //конвертируем в Face
            this.faces = new Face[object.getNumFaces()];
            for (int i = 0; i < object.getNumFaces(); i++) {
                faces[i] = new Face(new Point[]{
                        new Point(object.getVertex(object.getFace(i).getVertexIndex(0)).getX(),
                                object.getVertex(object.getFace(i).getVertexIndex(0)).getY(),
                                object.getVertex(object.getFace(i).getVertexIndex(0)).getZ()),
                        new Point(object.getVertex(object.getFace(i).getVertexIndex(1)).getX(),
                                object.getVertex(object.getFace(i).getVertexIndex(1)).getY(),
                                object.getVertex(object.getFace(i).getVertexIndex(1)).getZ()),
                        new Point(object.getVertex(object.getFace(i).getVertexIndex(2)).getX(),
                                object.getVertex(object.getFace(i).getVertexIndex(2)).getY(),
                                object.getVertex(object.getFace(i).getVertexIndex(2)).getZ())},
                        new Point[]{
                                new Point(object.getTexCoord(object.getFace(i).getTexCoordIndex(0)).getX(),
                                        object.getTexCoord(object.getFace(i).getTexCoordIndex(0)).getY()),
                                new Point(object.getTexCoord(object.getFace(i).getTexCoordIndex(1)).getX(),
                                        object.getTexCoord(object.getFace(i).getTexCoordIndex(1)).getY()),
                                new Point(object.getTexCoord(object.getFace(i).getTexCoordIndex(2)).getX(),
                                        object.getTexCoord(object.getFace(i).getTexCoordIndex(2)).getY())},
                        new Point(
                                object.getNormal(object.getFace(i).getNormalIndex(0)).getX(),
                                object.getNormal(object.getFace(i).getNormalIndex(0)).getY(),
                                object.getNormal(object.getFace(i).getNormalIndex(0)).getZ()
                        ));
            }
            isLoaded = true;
        }).start();
        onRedrawSetup();
        redrawNow();
    }

    public void prepareAndDraw() {
        if (isLoaded) {
            bindData();
            glEnable(GL_CULL_FACE); //i dont know what is it, it should be optimization
            glDrawArrays(GL_TRIANGLES, 0, object.getNumFaces() * 3);
            glDisable(GL_CULL_FACE);
        }
    }

    public void bindData() {
        Shader.getActiveShader().getAdaptor().bindData(faces);
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
        postToGlNeeded = false;
    }

    private void postToGl() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getId());
        for (int i = 0; i < images.length; i++) {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA, images[i].bitmap, GLES20.GL_UNSIGNED_BYTE, 0);
        }
    }

    @Override
    public void onRedrawSetup() {
        setRedrawNeeded(true);
    }

    @Override
    public void setRedrawNeeded(boolean redrawNeeded) {
        this.redrawNeeded = redrawNeeded;
        postToGlNeeded = true;
        if (redrawNeeded) {
            VectriesShapesManager.allShapesToRedraw.add(new java.lang.ref.WeakReference<>(this));//добавить ссылку на Poligon
        }
    }

    @Override
    public boolean isRedrawNeeded() {
        return redrawNeeded;
    }


    @Override
    public void onRedraw() {
        for (int i = 0; i < images.length; i++) {
            if (images[i] != null) {
                images[i].delete();
            }
            images[i] = redrawFunction.apply(i);
            setRedrawNeeded(false);
        }
    }


    @Override
    public String getCreatorClassName() {
        return null;
    }

    @Override
    public void delete() {
        for (int i = 0; i < images.length; i++) {
            images[i].delete();
        }

    }

    public void redrawNow() {
        onRedraw();
    }

    private PImage loadTexture(int i) {
        return loadImage(textureFileName + String.valueOf(i) + "." + res);
    }
}

