package com.manateam.glengine3.engine.main.verticles;

import static android.opengl.GLES10.GL_TRIANGLES;
import static android.opengl.GLES10.glDisable;
import static android.opengl.GLES20.GL_CULL_FACE;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glVertexAttribPointer;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.aPositionLocation;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.aTextureLocation;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.normalsLocation;
import static com.manateam.glengine3.engine.config.MainConfigurationFunctions.uTextureUnitLocation;
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

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.function.Function;

public class Shape implements VerticleSet, DrawableShape {
    private boolean isLoaded = false;
    float[][] vectries;
    float[][] vtextuers;
    float[] coords;
    private float[] vertexCoords, normalCoords, textureCoords;
    private int vertexesNumber = 0;
    private final String textureFileName;


    private Texture texture;
    private FloatBuffer vertexData;

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
        VectriesShapesManager.allShapes.add(new java.lang.ref.WeakReference<>(this));//добавить ссылку на Poligon
        if (page != null) {
            creatorClassName = page.getClass().getName();
        }
        this.textureFileName = textureFileName;
        texture = new Texture(page);
        new Thread(() -> {
            String file = loadFile(fileName);
            //а как по-другому?
            if (!String.valueOf(2.0f).equals("2.0")) {
                file = file.replace('.', ',');
            }
            file = formatFile(file);
            //работает - не трогай 3 строки ниже:
            vectries = loadVectries(file, 0);
            vtextuers = loadTetxtures(file, finishedAt + 1);
            loadCoords(file, finishedAt + 1);
            //clean memory
            vectries = null;
            vtextuers = null;
            isLoaded = true;
        }).start();
        onRedrawSetup();
        redrawNow();
    }

    private String formatFile(String file) {
        String[] f = split1(file, '\n');
        String r = "";
        for (int i = 0; i < f.length; i++) {
            String[] s = split1(f[i], ' ');
            if (s[0].equals("v") || s[0].equals("vt") || s[0].equals("f")) {
                r += f[i] + "#";
            }
        }
        return r;
    }


    private PImage loadTexture(Void v) {
        return loadImage(textureFileName);
    }

    private int finishedAt = 0;

    private float[][] loadVectries(String file, int startPos) {
        float[][] v = new float[countSubstrs(file, "v ")][3];
        String[] lines = split1(file, '#');
        String[] s;
        for (int i = startPos; i < lines.length; i++) {
            s = split1(lines[i], ' ');
            if (!s[0].equals("v")) {
                break;
            }
            v[i - startPos][0] = Float.parseFloat(s[1]);
            v[i - startPos][1] = Float.parseFloat(s[2]);
            v[i - startPos][2] = Float.parseFloat(s[3]);
            finishedAt = i;
        }
        return v;
    }

    private float[][] loadTetxtures(String file, int startPos) {
        float[][] v = new float[countSubstrs(file, "vt ")][2];
        String[] lines = split1(file, '#');
        String[] s;
        for (int i = startPos; i < lines.length; i++) {
            s = split1(lines[i], ' ');
            if (!s[0].equals("vt")) {
                break;
            }
            v[i - startPos][0] = Float.parseFloat(s[1]);
            v[i - startPos][1] = Float.parseFloat(s[2]);
            finishedAt = i;
        }
        return v;
    }

    private void loadCoords(String file, int startPos) {
        ArrayList<Float> vc = new ArrayList<Float>(), tc = new ArrayList<Float>(), nc = new ArrayList<Float>(); //vertex coord, texture coord, normal coord

        String[] lines = split1(file, '#');
        String[] s;
        for (int i = startPos; i < lines.length; i++) {
            s = split1(lines[i], ' ');
            if (!s[0].equals("f")) {
                break;
            }
            Point[] vertexes = new Point[3];//for coords of vert and calc normals
            //это пробежка по вершинам. В одной строке инфа про 3 вершины
            for (int g = 1; g < s.length; g++) {
                String[] t = split1(s[g], '/');//разбиваем на конкретгые индексы
                float[] vertex = vectries[Utils.parseInt(t[0]) - 1];//3д координаты вершины
                float[] textureCoord = vtextuers[Utils.parseInt(t[1]) - 1];//2д координаты текстуры
                if (g == 4) {
                    Log.e("ERROR LOADING SHAPE", "WRONG TRIANGULATION, FOUND FACE WITH >3 VERTEXES");
                }
                vc.add(vertex[0]);
                vc.add(vertex[1]);
                vc.add(vertex[2]);
                tc.add(textureCoord[0]);
                tc.add(textureCoord[1]);
                vertexes[g - 1] = new Point(vertex[0], vertex[1], vertex[2]);
            }
            //считаем нормаль
            Vec3 n = normal(vertexes[0], vertexes[1], vertexes[2]);
            nc.add(n.x);
            nc.add(n.y);
            nc.add(n.z);
            finishedAt = i;

            vertexCoords = toFloatArray(vc);
            textureCoords = toFloatArray(tc);
            normalCoords = toFloatArray(nc);

        }
    }

    private float[] toFloatArray(ArrayList<Float> vc) {
        int iter = 0;
        float arr[] = new float[vc.size()];
        for (Float f : vc) {
            arr[iter++] = (f != null ? f : Float.NaN); // Or whatever default you want.
        }
        return arr;
    }


    public void prepareData() {
        //in order not to recalculate it every time
        if (coords != null) {
            vertexData = null;
            vertexData = ByteBuffer.allocateDirect(coords.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            vertexData.put(coords);
            vertexesNumber = coords.length / 8;//because every 8 point of it make 1 vectrie
            coords = null;
        }
    }


    public void bindData() {
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
        glUniform1i(uTextureUnitLocation, 0);
    }

    private void postToGl() {
        postToGlNeeded = false;
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getId());

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GL_RGBA, image.bitmap, GLES20.GL_UNSIGNED_BYTE, 0);
    }

    public int createTexture() {
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

    public void prepareAndDraw() {
        if (isLoaded) {
            prepareData();
            bindData();
            glEnable(GL_CULL_FACE); //i dont know what is it, it should be optimization
            glDrawArrays(GL_TRIANGLES, 0, vertexesNumber);
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

    public void redrawNow() {
        onRedraw();
    }

    @Override
    public float[] getVertexData() {
        return vertexCoords;
    }

    @Override
    public float[] getTextureData() {
        return textureCoords;
    }
}
