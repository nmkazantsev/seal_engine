package com.seal.gl_engine.engine.main.verticles;

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

import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.engine.main.images.PImage;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.textures.Texture;
import com.seal.gl_engine.engine.main.vertex_bueffer.VertexBuffer;
import com.seal.gl_engine.maths.Point;
import com.seal.gl_engine.utils.Utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

public class Shape implements VerticleSet, DrawableShape {
    private boolean isVertexLoaded = false, globalLoaded = false;

    private final Texture texture;
    private Texture normalTexture;
    private final String textureFileName;
    private String normalMapFileName;
    private Obj object;
    private Face[] faces;
    private final GamePageClass creator;

    private boolean postToGlNeeded = true;
    private boolean redrawNeeded = true;
    private PImage image, normalImage;

    private final Function<Void, PImage> redrawFunction;

    private VertexBuffer vertexBuffer;

    private boolean vboLoaded = false;

    public Shape(String fileName, String textureFileName, GamePageClass page) {
        creator = page;
        this.redrawFunction = this::loadTexture;
        this.textureFileName = textureFileName;
        VectriesShapesManager.allShapes.add(new java.lang.ref.WeakReference<>(this));//добавить ссылку на Poligon
        texture = new Texture(page);
        new Thread(() -> {
            InputStreamReader inputStream;
            try {
                inputStream = new InputStreamReader(Utils.context.getAssets().open(fileName), StandardCharsets.UTF_8);
                object = ObjUtils.convertToRenderable(
                        ObjReader.read(inputStream));
            } catch (IOException e) {
                Log.e("ERROR LOADING", fileName);
            }
            //convert to Face
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
            isVertexLoaded = true;
        }).start();
        onRedrawSetup();
        redrawNow();
    }

    public void onFrameBegin() {
        if (isVertexLoaded) {
            globalLoaded = true;
        }
    }

    public void addNormalMap(String normalMapFileName) {
        this.normalMapFileName = normalMapFileName;
        normalImage = Utils.loadImage(normalMapFileName);
        normalTexture = new Texture(creator);
    }

    private PImage loadTexture(Void v) {
        if (normalMapFileName != null) {
            normalImage = Utils.loadImage(normalMapFileName);
        }
        return Utils.loadImage(textureFileName);
    }


    public void bindData() {
        if (!vboLoaded) {
            vertexBuffer = new VertexBuffer(5, creator); //5 because 5 types of coordinates so we need 5 buffers
            Shader.getActiveShader().getAdaptor().bindData(faces, vertexBuffer);
            vboLoaded = true;
        }

        // place texture in target 2D unit 0
        glActiveTexture(GL_TEXTURE0);
        if (!postToGlNeeded) {
            glBindTexture(GL_TEXTURE_2D, texture.getId());
        }
        if (postToGlNeeded) {
            postToGl();
        }
        // texture unit
        glUniform1i(Shader.getActiveShader().getAdaptor().getTextureLocation(), 0);

        //  place texture in target 2D unit 0
        glActiveTexture(GL_TEXTURE1);
        if (!postToGlNeeded && normalTexture != null) {
            glBindTexture(GL_TEXTURE_2D, normalTexture.getId());
        }
        if (postToGlNeeded) {
            postToGlNormals();
        }
        //texture unit
        glUniform1i(Shader.getActiveShader().getAdaptor().getNormalTextureLocation(), 1);

        //enable or disable normal map in shader
        if (normalTexture != null) {
            GLES30.glUniform1i(Shader.getActiveShader().getAdaptor().getNormalMapEnableLocation(), 1);
        } else {
            GLES30.glUniform1i(Shader.getActiveShader().getAdaptor().getNormalMapEnableLocation(), 0);
        }
        postToGlNeeded = false;
    }

    private void postToGl() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getId());
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GL_RGBA, image.bitmap, GLES20.GL_UNSIGNED_BYTE, 0);
    }

    private void postToGlNormals() {
        if (normalImage != null && normalImage.isLoaded()) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, normalTexture.getId());
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GL_RGBA, normalImage.bitmap, 0);
            normalImage.delete();
            glActiveTexture(GL_TEXTURE0);
        }
    }


    public void prepareAndDraw() {
        if (globalLoaded) {
            bindData();
            vertexBuffer.bindVao();
            glEnable(GL_CULL_FACE); //i dont know what is it, it should be optimization
            glDrawArrays(GL_TRIANGLES, 0, object.getNumFaces() * 3);
            glDisable(GL_CULL_FACE);
            vertexBuffer.bindDefaultVao();
        }
    }

    @Override
    public void onRedrawSetup() {
        setRedrawNeeded(true);
        vboLoaded = false;
    }

    @Override
    public void setRedrawNeeded(boolean redrawNeeded) {
        this.redrawNeeded = redrawNeeded;
        postToGlNeeded = true;
        if (redrawNeeded) {
            VectriesShapesManager.allShapesToRedraw.add(new java.lang.ref.WeakReference<>(this));//добавить ссылку на Poligon
        }
        vboLoaded = false;
    }

    @Override
    public boolean isRedrawNeeded() {
        return redrawNeeded;
    }


    @Override
    public void onRedraw() {
        vboLoaded = false;
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
        normalImage.delete();
        vertexBuffer.delete();
    }

    public void redrawNow() {
        onRedraw();
    }
}
