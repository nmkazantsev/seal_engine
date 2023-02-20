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
import com.manateam.glengine3.maths.Point;
import com.manateam.glengine3.maths.Vec3;
import com.manateam.glengine3.utils.Utils;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.function.Function;

public class Shape implements VerticleSet {
    private boolean isLoaded = false;
    float[][] vectries;
    float[][] vtextuers;
    float[] coords;
    private int vertexesNumber = 0;
    private final String textureFileName;
    private final WeakReference<TexturePoligonConnector> texturePoligonConnector;

    private int texture;
    private  FloatBuffer vertexData;

    private final static int POSITION_COUNT = 3;
    private static final int TEXTURE_COUNT = 2;
    private static final int NORMAL_COUNT=3;
    private static final int STRIDE = (POSITION_COUNT
            + TEXTURE_COUNT + NORMAL_COUNT) * 4;//3 is noramls count

    private boolean postToGlNeeded = true;
    private boolean isTextureDeleted = true;
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
        texturePoligonConnector = VectriesShapesManager.addTexPoliLinnk(new java.lang.ref.WeakReference<>(this));
        texturePoligonConnector.get().setCreatorClassName(creatorClassName);
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
            coords = loadCoords(file, finishedAt + 1);
            //clean memory
            vectries = null;
            vtextuers = null;
            isLoaded = true;
        }).start();
        onRedrawSetup();
        redrawNow();
    }
    private String formatFile(String file){
        String [] f=split1(file,'\n');
        String r="";
        for (int i=0;i<f.length;i++){
            String[] s=split1(f[i],' ');
            if(s[0].equals("v") || s[0].equals("vt")||s[0].equals("f")){
               r+=f[i]+"#";
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

    private float[] loadCoords(String file, int startPos) {
        // float v[] = new float[countSubstrs(file, "f ") * 5 * 3];
        ArrayList<Float> faces = new ArrayList<>();
        String[] lines = split1(file, '#');
        String[] s;
        for (int i = startPos; i < lines.length; i++) {
            s = split1(lines[i], ' ');
            if (!s[0].equals("f")) {
                break;
            }
            /*
            сейчас мы будем прохожить по строке и читать оттуда вершины и координаты текстур. Все это мы будем пихать в массив temp так, как оно потом будет в инальной сруктуре данных,
            но только с таким рассчётом, чтобы туда еще пропихнуть нормали (по оному вектору из 3 элемента для каждой верниы). Поэтому он 3 (количесво вершин)*5(количество значений).
            Также сохраняем отдельно точки в vertexes чтобы по ним посчитать нормали.
             */
            float[][] temp = new float[3][5];
            Point[] vertexes = new Point[3];//for coords of vert and calc normals
            //это пробежка по вершинам. В одной строке инфа про 3 вершины
            for (int g = 1; g < s.length; g++) {
                String[] t = split1(s[g], '/');//разбиваем на конкретгые индексы
                float[] vertex = vectries[Utils.parseInt(t[0]) - 1];//3д координаты вершины
                float[] textureCoord = vtextuers[Utils.parseInt(t[1]) - 1];//2д координаты текстуры
                if(g==4){
                    Log.e("ERROR LOADING SHAPE","WRONG TRIANGULATION, FOUND FACE WITH >3 VERTEXES");
                }
                temp[g - 1] = new float[]{vertex[0], vertex[1], vertex[2], textureCoord[0], textureCoord[1]};
                vertexes[g-1]=new Point(vertex[0], vertex[1],vertex[2]);
            }
            //считаем нормаль
            Vec3 n = normal(vertexes[0], vertexes[1], vertexes[2]);
            for (int g = 0; g < 3; g++) {
                //загружаем первые пять координат
                for (int f = 0; f < temp[0].length; f++) {
                    faces.add(temp[g][f]);
                }
                //и добавляем нормаль
                faces.add(n.x);//normals[(i - startPos) / 3][0])
                faces.add(n.y);
                faces.add(n.z);

            }
            finishedAt = i;
        }
        float[] v = new float[faces.size()];
        for (int i = 0; i < faces.size(); i++) {
            v[i] = faces.get(i);
        }
        return v;
    }


    public void prepareData() {
        //in order not to recalculate it every time
        if (coords != null) {
            vertexData = null;
            vertexData = ByteBuffer
                    .allocateDirect(coords.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            vertexData.put(coords);
            vertexesNumber = coords.length / 8;//because every 8 point of it make 1 vectrie
            coords = null;
        }
    }


    public void bindData() {
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

        //normals
        //prepareNormals();
        vertexData.position(POSITION_COUNT + TEXTURE_COUNT);
        glVertexAttribPointer(normalsLocation, NORMAL_COUNT, GL_FLOAT,
                false, STRIDE, vertexData);
        glEnableVertexAttribArray(normalsLocation);



        // помещаем текстуру в target 2D юнита 0
        glActiveTexture(GL_TEXTURE0);
        if (!postToGlNeeded) {
            glBindTexture(GL_TEXTURE_2D, texture);
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
        glBindTexture(GL_TEXTURE_2D, this.texture);

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
        setTextureDeleted(true);
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
        if (texturePoligonConnector.get() != null) {
            this.image = redrawFunction.apply(null);
            setRedrawNeeded(false);
        }
    }



    @Override
    public String getCreatorClassName() {
        return null;
    }

    public void redrawNow() {
        onRedraw();
    }

    private void setTextureDeleted(boolean textureDeleted) {
        isTextureDeleted = textureDeleted;
    }
}
