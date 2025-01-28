package com.seal.gl_engine.engine.main.vertices;

import static android.opengl.GLES10.GL_TRIANGLES;
import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_CUBE_MAP;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDepthMask;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glTexParameteri;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES30.GL_TEXTURE_WRAP_R;

import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.seal.gl_engine.engine.main.textures.CubeMap;
import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.engine.main.images.PImage;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.maths.PVector;
import com.seal.gl_engine.utils.Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

public class SkyBox implements VerticesSet {
    private final CubeMap texture;
    private final String textureFileName, res;
    private Face[] faces;
    private final PImage[] images = new PImage[6];

    private boolean postToGlNeeded = true;
    private boolean redrawNeeded = true;

    private final Function<Void, PImage> redrawFunction;

    private final String[] names = new String[]{"right", "left", "bottom", "top", "front", "back"};

    public SkyBox(String textureFileName, String res, GamePageClass page) {
        this.res = res;
        this.redrawFunction = this::loadTexture;
        this.textureFileName = textureFileName;
        VerticesShapesManager.allShapes.add(new java.lang.ref.WeakReference<>(this));//добавить ссылку на Poligon
        texture = new CubeMap(page);
        try {
            createFaces();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        onRedrawSetup();
        redrawNow();
    }

    //loads a cube.obj file to faces
    private void createFaces() throws IOException {

        ByteArrayInputStream inputStream;
        Obj object;

        //library projects con not have assets
        String cube_file = "# Blender v3.2.2 OBJ File: ''\n" +
                "# www.blender.org\n" +
                "mtllib cube.mtl\n" +
                "o Cube\n" +
                "v 1.000000 1.000000 -1.000000\n" +
                "v 1.000000 -1.000000 -1.000000\n" +
                "v 1.000000 1.000000 1.000000\n" +
                "v 1.000000 -1.000000 1.000000\n" +
                "v -1.000000 1.000000 -1.000000\n" +
                "v -1.000000 -1.000000 -1.000000\n" +
                "v -1.000000 1.000000 1.000000\n" +
                "v -1.000000 -1.000000 1.000000\n" +
                "vt 0.875000 0.500000\n" +
                "vt 0.625000 0.750000\n" +
                "vt 0.625000 0.500000\n" +
                "vt 0.375000 1.000000\n" +
                "vt 0.375000 0.750000\n" +
                "vt 0.625000 0.000000\n" +
                "vt 0.375000 0.250000\n" +
                "vt 0.375000 0.000000\n" +
                "vt 0.375000 0.500000\n" +
                "vt 0.125000 0.750000\n" +
                "vt 0.125000 0.500000\n" +
                "vt 0.625000 0.250000\n" +
                "vt 0.875000 0.750000\n" +
                "vt 0.625000 1.000000\n" +
                "vn 0.0000 1.0000 0.0000\n" +
                "vn 0.0000 0.0000 1.0000\n" +
                "vn -1.0000 0.0000 0.0000\n" +
                "vn 0.0000 -1.0000 0.0000\n" +
                "vn 1.0000 0.0000 0.0000\n" +
                "vn 0.0000 0.0000 -1.0000\n" +
                "usemtl Material\n" +
                "s off\n" +
                "f 5/1/1 3/2/1 1/3/1\n" +
                "f 3/2/2 8/4/2 4/5/2\n" +
                "f 7/6/3 6/7/3 8/8/3\n" +
                "f 2/9/4 8/10/4 6/11/4\n" +
                "f 1/3/5 4/5/5 2/9/5\n" +
                "f 5/12/6 2/9/6 6/7/6\n" +
                "f 5/1/1 7/13/1 3/2/1\n" +
                "f 3/2/2 7/14/2 8/4/2\n" +
                "f 7/6/3 5/12/3 6/7/3\n" +
                "f 2/9/4 4/5/4 8/10/4\n" +
                "f 1/3/5 3/2/5 4/5/5\n" +
                "f 5/12/6 1/3/6 2/9/6\n";
        inputStream = new ByteArrayInputStream(cube_file.getBytes(StandardCharsets.UTF_8));

        object = ObjUtils.convertToRenderable(
                ObjReader.read(inputStream));


        //конвертируем в Face
        this.faces = new Face[object.getNumFaces()];
        for (int i = 0; i < object.getNumFaces(); i++) {
            faces[i] = new Face(new PVector[]{
                    new PVector(object.getVertex(object.getFace(i).getVertexIndex(0)).getX(),
                            object.getVertex(object.getFace(i).getVertexIndex(0)).getY(),
                            object.getVertex(object.getFace(i).getVertexIndex(0)).getZ()),
                    new PVector(object.getVertex(object.getFace(i).getVertexIndex(1)).getX(),
                            object.getVertex(object.getFace(i).getVertexIndex(1)).getY(),
                            object.getVertex(object.getFace(i).getVertexIndex(1)).getZ()),
                    new PVector(object.getVertex(object.getFace(i).getVertexIndex(2)).getX(),
                            object.getVertex(object.getFace(i).getVertexIndex(2)).getY(),
                            object.getVertex(object.getFace(i).getVertexIndex(2)).getZ())},
                    new PVector[]{
                            new PVector(object.getTexCoord(object.getFace(i).getTexCoordIndex(0)).getX(),
                                    object.getTexCoord(object.getFace(i).getTexCoordIndex(0)).getY()),
                            new PVector(object.getTexCoord(object.getFace(i).getTexCoordIndex(1)).getX(),
                                    object.getTexCoord(object.getFace(i).getTexCoordIndex(1)).getY()),
                            new PVector(object.getTexCoord(object.getFace(i).getTexCoordIndex(2)).getX(),
                                    object.getTexCoord(object.getFace(i).getTexCoordIndex(2)).getY())},
                    new PVector(
                            object.getNormal(object.getFace(i).getNormalIndex(0)).getX(),
                            object.getNormal(object.getFace(i).getNormalIndex(0)).getY(),
                            object.getNormal(object.getFace(i).getNormalIndex(0)).getZ()
                    ));
        }
    }

    private PImage loadTexture(Void v) {
        for (int i = 0; i < images.length; i++) {
            images[i] = Utils.loadImage(textureFileName + names[i] + "." + res);
        }
        return null;
    }


    public void bindData() {

        Shader.getActiveShader().getAdaptor().bindData(faces);

        // помещаем текстуру в target 2D юнита 0
        glActiveTexture(GL_TEXTURE0);
        if (!postToGlNeeded) {
            glBindTexture(GL_TEXTURE_CUBE_MAP, texture.getId());
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
        glBindTexture(GL_TEXTURE_CUBE_MAP, texture.getId());
        glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);


        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, images[0].bitmap, 0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, images[1].bitmap, 0);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, images[2].bitmap, 0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, images[3].bitmap, 0);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, images[4].bitmap, 0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, images[5].bitmap, 0);
        for (PImage i : images) {
            i.delete();
        }
    }

    public void prepareAndDraw() {
        bindData();
        glDepthMask(false);
        glDrawArrays(GL_TRIANGLES, 0, 12 * 3);
        glDepthMask(true);
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
            VerticesShapesManager.allShapesToRedraw.add(new java.lang.ref.WeakReference<>(this));//добавить ссылку на Poligon
        }
    }

    @Override
    public boolean isRedrawNeeded() {
        return redrawNeeded;
    }


    @Override
    public void onRedraw() {
        redrawFunction.apply(null);
        setRedrawNeeded(false);
    }


    @Override
    public String getCreatorClassName() {
        return null;
    }

    @Override
    public void onFrameBegin() {

    }

    @Override
    public void delete() {
        //everything is already deleted
    }

    public void redrawNow() {
        onRedraw();
    }

}

