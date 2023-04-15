package com.manateam.glengine3.engine.main.verticles;

import static android.opengl.GLES10.GL_TRIANGLES;
import static android.opengl.GLES10.glDisable;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glUniform1i;
import static com.manateam.glengine3.maths.Point.normal;
import static com.manateam.glengine3.utils.Utils.countSubstrs;
import static com.manateam.glengine3.utils.Utils.loadFile;
import static com.manateam.glengine3.utils.Utils.loadImage;
import static com.manateam.glengine3.utils.Utils.split1;

import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.manateam.glengine3.GamePageInterface;
import com.manateam.glengine3.engine.main.images.PImage;
import com.manateam.glengine3.engine.main.shaders.Shader;
import com.manateam.glengine3.engine.main.textures.Texture;
import com.manateam.glengine3.maths.Point;
import com.manateam.glengine3.maths.Vec3;
import com.manateam.glengine3.utils.Utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.function.Function;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

public class Shape implements VerticleSet, DrawableShape {
    private boolean isLoaded = false;

    private final Texture texture;
    private FloatBuffer vertexData;
    private final String textureFileName;
    private Obj object;
    private Face[] faces;

    private final static int POSITION_COUNT = 3;
    private static final int TEXTURE_COUNT = 2;
    private static final int NORMAL_COUNT = 3;
    private static final int STRIDE = (POSITION_COUNT + TEXTURE_COUNT + NORMAL_COUNT) * 4;//3 is noramls count

    private boolean postToGlNeeded = true;
    private boolean redrawNeeded = true;
    private PImage image;

    private final Function<Void, PImage> redrawFunction;
    private String creatorClassName;

    public Shape(String fileName, String textureFileName, GamePageInterface page) {
        this.redrawFunction = this::loadTexture;
        this.textureFileName = textureFileName;
        VectriesShapesManager.allShapes.add(new java.lang.ref.WeakReference<>(this));//добавить ссылку на Poligon
        if (page != null) {
            creatorClassName = page.getClass().getName();
        }
        texture = new Texture(page);
        new Thread(() -> {
            String file = loadFile(fileName);
            //а как по-другому?
            /*if (!String.valueOf(2.0f).equals("2.0")) {
                file = file.replace('.', ',');
            }*/
            InputStreamReader inputStream;
            try {
                inputStream = new InputStreamReader(Utils.context.getAssets().open("8.obj"), StandardCharsets.UTF_8);
                object = ObjUtils.convertToRenderable(
                        ObjReader.read(inputStream));
                object.toString();
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
                                new Point(object.getVertex(object.getFace(i).getTexCoordIndex(0)).getX(),
                                        object.getVertex(object.getFace(i).getTexCoordIndex(0)).getY()),
                                new Point(object.getVertex(object.getFace(i).getTexCoordIndex(1)).getX(),
                                        object.getVertex(object.getFace(i).getTexCoordIndex(1)).getY()),
                                new Point(object.getVertex(object.getFace(i).getTexCoordIndex(2)).getX(),
                                        object.getVertex(object.getFace(i).getTexCoordIndex(2)).getY())},
                        new Point(
                                object.getVertex(object.getFace(i).getNormalIndex(0)).getX(),
                                object.getVertex(object.getFace(i).getNormalIndex(0)).getY(),
                                object.getVertex(object.getFace(i).getNormalIndex(0)).getZ()
                        ));
            }
            isLoaded = true;
        }).start();
        onRedrawSetup();
        redrawNow();
    }

    private PImage loadTexture(Void v) {
        return loadImage(textureFileName);
    }


    public void bindData() {

        //vertexesNumber = Shader.getActiveShader().getAdaptor().bindData(this.faces);

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
        postToGlNeeded = false;
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getId());

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GL_RGBA, image.bitmap, GLES20.GL_UNSIGNED_BYTE, 0);
    }


    public void prepareAndDraw() {
        if (isLoaded) {
            bindData();
          //  glEnable(GL_CULL_FACE); //i dont know what is it, it should be optimization
            glDrawArrays(GL_TRIANGLES, 0,6);
            glDisable(GL_CULL_FACE);
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
        if (image != null) {
            image.delete();
        }
        this.image = redrawFunction.apply(null);
        setRedrawNeeded(false);
    }


    @Override
    public String getCreatorClassName() {
        return null;
    }

    @Override
    public void delete() {
        image.delete();
    }

    public void redrawNow() {
        onRedraw();
    }
}
