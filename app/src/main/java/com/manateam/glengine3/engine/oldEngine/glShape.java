package com.manateam.glengine3.engine.oldEngine;


import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Build;

import com.manateam.glengine3.GamePageInterface;
import com.manateam.glengine3.OpenGLRenderer;
import com.manateam.glengine3.engine.main.images.PImage;
import com.manateam.glengine3.utils.Utils;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class glShape {
    public boolean saveMemory = false;
    public boolean upperText = false;
    public boolean bigRounds = false;//atrem, contact me, i will tell about bug in draw ellipse that caused drawing to big ellipses in tankki 5.1 and now we have to minimize them
    float translationX, translationY;//works only with line because it is needed only in drawWalls()
    float textSize;
    public Paint paintImg = new Paint();
    public Bitmap bitmap;
    public Canvas canvas;
    public Paint paint, stroke;
    public int texture;
    float sizex, sizey;
    private boolean redrawNeeded = true;
    private boolean postToGlNeeded = true;
    public FloatBuffer vertexData;
    private boolean isTextureDeleted = false;
    private String creatorClassName;

    private static List<WeakReference<glShape>> allShapes = new ArrayList<>();

    public void setFont(String font) {
        Typeface tf = Typeface.createFromAsset(Utils.context.getAssets(), font);
        paint.setTypeface(tf);
    }

    public glShape(float sizex, float sizey, GamePageInterface page) {
        if(page!=null) {
            this.creatorClassName = (String) page.getClass().getName();
        }
        this.sizex = sizex;
        this.sizey = sizey;
        bitmap = Bitmap.createBitmap((int) sizex, (int) sizey, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        stroke = new Paint();
        paint.setAntiAlias(true);
        stroke.setAntiAlias(true);
        texture = createTexture();
        allShapes.add(new WeakReference<glShape>(this));//добавить ссылку на glShape
    }

    public glShape(float sizex, float sizey, boolean saveMemory, GamePageInterface page) {
        this.saveMemory = saveMemory;
        if(page!=null) {
            this.creatorClassName = (String) page.getClass().getName();
        }
        if (!saveMemory) {
            bitmap = Bitmap.createBitmap((int) sizex, (int) sizey, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
        }
        paint = new Paint();
        stroke = new Paint();
        paint.setAntiAlias(true);
        stroke.setAntiAlias(true);
        texture = createTexture();
        this.sizex = sizex;
        this.sizey = sizey;
        allShapes.add(new WeakReference<glShape>(this));//добавить ссылку на glShape
    }
    public String getCreatorClassName(){
        return creatorClassName;
    }

    public static void allShapesRedrawSetup() {
        Iterator<WeakReference<glShape>> iterator = allShapes.iterator();
        while (iterator.hasNext()) {
            WeakReference<glShape> e = iterator.next();
            if (e.get() != null) {
                e.get().deleteTexture();
                if (!e.get().saveMemory) {
                    e.get().bitmap = Bitmap.createBitmap((int) e.get().sizex, (int) e.get().sizey, Bitmap.Config.ARGB_8888);
                    e.get().canvas = new Canvas(e.get().bitmap);
                } else {
                    e.get().setRedrawNeeded(true);
                }
            }
            else {
                iterator.remove();
            }
        }
    }

    public static void onPageChanged(){
        for (int i = 0; i < allShapes.size(); i++) {
            if (allShapes.get(i).get() != null) {
                if (allShapes.get(i).get().getCreatorClassName() != null) {
                    if (!allShapes.get(i).get().getCreatorClassName().equals(OpenGLRenderer.getPageClassName())) {
                        allShapes.get(i).get().deleteTexture();
                        allShapes.get(i).get().setRedrawNeeded(true);
                    }
                }
            }
        }
        Iterator<WeakReference<glShape>> iterator = allShapes.iterator();
        while (iterator.hasNext()) {
            WeakReference<glShape> e = iterator.next();
            if (e.get() == null) {
                iterator.remove();
            }
        }
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

    public void prepareData(float x, float y, float a, float b, float z) {
        float[] vertices = glrect(x , y , a, b, z);

        vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);
    }

    public void prepareData(float[] vertices) {
        vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);
    }

    public void prepareData(float rot, float x, float y, float a, float b, float z) {
        float[] vertices = glrectRotated(rot, x , y , a, b, z);

        vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);
    }

    public void prepareData(float x, float y, float a, float b, float z, float texx, float texy, float texa, float texb) {
        float[] vertices = glrect(x, y, a, b, z, texx, texy, texa, texb);

        vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);
    }

    public void prepareData(float rot, float n, float m, float x, float y, float a, float b, float z) {
        float[] vertices = glrectRotatedFromPoint(rot, n, m, x, y , a, b, z);

        vertexData = ByteBuffer
                .allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(vertices);
    }

    public float[] glrect(float x, float y, float a, float b, float z) {
        float[] vertices = {
                x, y, z, 0, 0,
                x + a, y, z, 1, 0,
                x, y + b, z, 0, 1,

                x, y + b, z, 0, 1,
                x + a, y + b, z, 1, 1,
                x + a, y, z, 1, 0
        };
        return vertices;
    }

    public float[] glrect(float x, float y, float a, float b, float z, float texx, float texy, float texa, float texb) {
        float[] vertices = {
                x, y, z, texx, texy,
                x + a, y, z, texx + texa, texy,
                x, y + b, z, texx, texy + texb,

                x, y + b, z, texx, texy + texb,
                x + a, y + b, z, texx + texa, texy + texb,
                x + a, y, z, texx + texa, texy
        };
        return vertices;
    }


    public float[] glrectRotated(float r, float x, float y, float a, float b, float z) {
        float[][] ver = {
                {x, y}, {x + a, y}, {x, y + b}, {x + a, y + b} //where vectries are now
        };
        x += a / 2;//x,y  - теперь центр прямоугльника
        y += b / 2;
        for (int i = 0; i < ver.length; i++) {
            float d = getDirectoin(x, y, ver[i][0], ver[i][1]) + r;//rotate them
            float dist = sqrt(sq(x - ver[i][0]) + sq(y - ver[i][1]));
            ver[i][0] = x + dist * cos(d);
            ver[i][1] = y + dist * sin(d);
        }
        float[] vertices = {
                ver[0][0], ver[0][1], z, 0, 0,
                ver[1][0], ver[1][1], z, 1, 0,
                ver[2][0], ver[2][1], z, 0, 1,

                ver[2][0], ver[2][1], z, 0, 1,
                ver[3][0], ver[3][1], z, 1, 1,
                ver[1][0], ver[1][1], z, 1, 0
        };
        return vertices;
    }

    public float[] glrectRotatedFromPoint(float r, float n, float m, float x, float y, float a, float b, float z) {
        //(n,m) is our point
        float[][] ver = {
                {x, y}, {x + a, y}, {x, y + b}, {x + a, y + b} //where vectries are now
        };
        for (int i = 0; i < ver.length; i++) {
            float d = getDirectoin(n, m, ver[i][0], ver[i][1]) + r;//rotate them
            float dist = sqrt(sq(n - ver[i][0]) + sq(m - ver[i][1]));
            ver[i][0] = n + dist * cos(d);
            ver[i][1] = m + dist * sin(d);
        }
        float[] vertices = {
                ver[0][0], ver[0][1], z, 0, 0,
                ver[1][0], ver[1][1], z, 1, 0,
                ver[2][0], ver[2][1], z, 0, 1,

                ver[2][0], ver[2][1], z, 0, 1,
                ver[3][0], ver[3][1], z, 1, 1,
                ver[1][0], ver[1][1], z, 1, 0
        };
        return vertices;
    }

    public float sq(float a) {
        return a * a;
    }

    public float sin(float a) {
        return ((float) Math.sin(a));
    }

    public float cos(float a) {
        return ((float) Math.cos(a));
    }

    public float sqrt(float a) {
        return (float) Math.sqrt(a);
    }

    public float getDirectoin(float px, float py, float tx, float ty) {
        //returns direction to point in radinas. Calculated from north direction (0, top of screen) along the hour line move direction
        float a;
        a = (degrees((atan((py - ty) / (px - tx)))) + 180);
        if (tx <= px) {
            a += 180;
        }
        a += 180;
        a %= 360;
        return (radians(a));
    }

    public void text(String s, float x, float y, float k) {    //Такая же функция, но можно задавать расстояние между строками
        String[] lines = split1(s, '\n');
        y = (int) y + (paint.descent() + paint.ascent()) / 2;
        if (!upperText) {
            for (int i = 0; i < lines.length; i++) {
                canvas.drawText(lines[i], x + translationX, y + (i + 1) * textSize * k + translationY, paint);
            }
        } else {
            for (int i = 0; i < lines.length; i++) {
                canvas.drawText(lines[i], x + translationX, y + translationY + (i) * textSize * k, paint);
            }
        }
    }


    public float atan(float a) {
        return ((float) Math.atan(a));
    }

    public void postToGL() {
        postToGlNeeded = false;
        if (isTextureDeleted()) {
            texture = createTexture();
            setTextureDeleted(false);
        }
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, this.texture);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GL_RGBA, bitmap, 0);
        //GLUtils.texImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bitmap.getWidth(), bitmap.getHeight(), 0, GL_RGBA, ,GL_UNSIGNED_BYTE,bitmap);
        // glBindTexture(GL_TEXTURE_2D, 0);
        if (saveMemory) {
            bitmap.recycle();
        }

    }

    public void background(int i) {
        canvas.drawColor(Color.rgb(i, i, i));
    }

    public void background(float i) {
        background((int) i);
    }

    public void background(int r, int g, int b) {
        canvas.drawColor(Color.rgb(r, g, b));
    }

    public void background(float r, float b, float g) {
        background(parseInt(r), parseInt(g), parseInt(b));
    }

    public void background(float r, float g, float b, float a) {
        canvas.drawColor(Color.argb((int) a, (int) r, (int) g, (int) b));
    }

    int parseInt(float i) {
        return (int) i;
    }

    public void roundRect(float x, float y, float a, float b, float rx, float ry) {
        a = x + a;
        b = y + b;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(x, y, a, b, rx, ry, stroke);
        } else {
            rect(x, y, a, b);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(x, y, a, b, rx, ry, paint);
        }

        //Log.i("rect",String.valueOf(x)+", "+String.valueOf(y)+", "+String.valueOf(a)+", "+String.valueOf(b)+", ");
    }

    public void rect(float x, float y, float a, float b) {
        a = x + a;
        b = y + b;
        canvas.drawRect(x, y, a, b, stroke);
        canvas.drawRect(x, y, a, b, paint);
    }

    public void fill(int r, int g, int b) {
        paint.setColor(Color.rgb(r, g, b));
    }

    public void fill(int r, int g, int b, int a) {
        paint.setARGB(a, r, g, b);
    }

    public void fill(int i) {
        paint.setColor(Color.rgb(i, i, i));
    }

    public void fill(float i) {
        fill(parseInt(i));
    }

    public void fill(float r, float g, float b) {
        fill(parseInt(r), parseInt(g), parseInt(b));
    }

    public void fill(float r, float g, float b, float a) {
        fill(parseInt(r), parseInt(g), parseInt(b), parseInt(a));
    }

    public void strokeWeight(float i) {
        paint.setStrokeWidth(0);
        stroke.setStrokeWidth(i);
    }

    public void stroke(int i) {
        stroke(i, i, i);
    }

    public void stroke(float i) {
        stroke((int) i, (int) i, (int) i);
    }

    public void stroke(float r, float g, float b) {
        stroke((int) r, (int) g, (int) b);
    }

    public void stroke(int r, int g, int b) {
        stroke.setColor(Color.rgb(r, g, b));
        stroke.setStyle(Paint.Style.STROKE);

    }

    public void stroke(int r, int g, int b, int a) {
        stroke.setColor(Color.argb(a, r, g, b));
        stroke.setStyle(Paint.Style.STROKE);

    }

    public void stroke(float r, float g, float b, float a) {
        stroke((int) r, (int) g, (int) b, (int) a);
    }


    public void text(float s, float x, float y) {
        text(String.valueOf(s), x, y);
    }

    public void text(int s, float x, float y) {
        text(String.valueOf(s), x, y);
    }

    public void text(String s, float x, float y) {
        String[] lines = split1(s, '\n');
        y = (int) y + (paint.descent() + paint.ascent()) / 2;
        float k = 1.3f;
        if (!upperText) {
            for (int i = 0; i < lines.length; i++) {
                canvas.drawText(lines[i], x + translationX, y + (i + 1) * textSize * k + translationY, paint);
            }
        } else {
            for (int i = 0; i < lines.length; i++) {
                canvas.drawText(lines[i], x + translationX, y + translationY + (i) * textSize * k, paint);
            }
        }
    }

    public String[] split1(String s, char a) {
        String[] out;
        out = s.split(String.valueOf(a));
        return out;
    }

    public void textSize(float s) {
        textSize = s;
        paint.setTextSize(s);
    }

    public void textAlign(Paint.Align a) {
        paint.setTextAlign(a);
    }

    public void image(PImage img, float x, float y) {
        //Paint paintImg = new Paint();
        canvas.drawBitmap(img.bitmap, x - img.bitmap.getWidth() / 2 + translationX, y - img.bitmap.getHeight() / 2 + translationY, paintImg);
    }

    public void image(PImage img, float x, float y, float a, float b) {
        Matrix matrixImg = new Matrix();

        float ikx, iky;
        ikx = a / img.width;
        iky = b / img.height;
        matrixImg.preTranslate(x / ikx - img.bitmap.getWidth() / (2), y / iky - img.bitmap.getHeight() / (2));
        matrixImg.postScale(ikx, iky);
        //  Paint paintImg = new Paint();
        canvas.drawBitmap(img.bitmap, matrixImg, paintImg);
    }

    public void image(PImage img, float x, float y, float scale) {

        Matrix matrixImg = new Matrix();
        matrixImg.postScale(scale, scale);
        matrixImg.preTranslate(x / scale - img.bitmap.getWidth() / (2) + translationX, y / scale - img.bitmap.getHeight() / (2) + translationY);
        // Paint paintImg = new Paint();
        canvas.drawBitmap(img.bitmap, matrixImg, paintImg);


    }

    public void rotImage(PImage img, float x, float y, float scale, float rot) {
        Matrix matrixImg = new Matrix();
        matrixImg.postScale(scale, scale);
        matrixImg.preTranslate(x / scale - img.bitmap.getWidth() / (2), y / scale - img.bitmap.getHeight() / (2));
        Paint paintImg = new Paint();
        canvas.rotate(degrees(rot), x, y);
        canvas.drawBitmap(img.bitmap, matrixImg, paintImg);
        canvas.rotate(-degrees(rot), x, y);
    }

    /* public class PImage {
         Bitmap bitmap;
         float width, height;
     }


     */
    public float degrees(float a) {
        return ((float) Math.toDegrees(a));
    }

    public float radians(float a) {
        return ((float) Math.toRadians(a));
    }

    public void ellipse(float px, float py, float r1, float r2) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bigRounds) {
                r1 /= 2.0f;
                r2 /= 2.0f;
            }
            paint.setAntiAlias(true);
            stroke.setAntiAlias(true);
            canvas.drawOval(px - r1 + translationX, py - r2 + translationY, px + r1 + translationX, py + r2 + translationY, stroke);
            canvas.drawOval(px - r1 + translationX, py - r2 + translationY, px + r1 + translationX, py + r2 + translationY, paint);
            stroke.setAntiAlias(false);
            paint.setAntiAlias(false);
        }
    }

    public void line(float x1, float y1, float x2, float y2) {
        canvas.drawLine(x1 + translationX, y1 + translationY, x2 + translationX, y2 + translationY, stroke);
    }

    public void translate(float x, float y) {
        translationX += x;
        translationY += y;
        if (x == 0 && y == 0) {
            translationX = translationY = 0;
        }
    }

    public void noStroke() {
        stroke.setStrokeWidth(0);
        stroke.setARGB(0, 0, 0, 0);
    }

    public void noFill() {
        paint.setARGB(0, 0, 0, 100);
        stroke.setStyle(Paint.Style.STROKE);

    }

    public boolean isRedrawNeeded() {
        if (saveMemory && redrawNeeded) {
            bitmap = Bitmap.createBitmap((int) sizex, (int) sizey, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
        }
        return redrawNeeded;
    }

    public void setRedrawNeeded(boolean r) {
        redrawNeeded = r;
        if (r) {
            postToGlNeeded = r;
            // bitmap = Bitmap.createBitmap((int) sizex, (int) sizey, Bitmap.Config.ARGB_4444);
        }
    }

    public boolean isPostToGlNeeded() {
        return postToGlNeeded;
    }

    public void triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
        Point point1_draw = new Point((int) (x1), (int) (y1));
        Point point2_draw = new Point((int) (x2), (int) y2);
        Point point3_draw = new Point((int) x3, (int) y3);


        Path path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(point1_draw.x, point1_draw.y);
        path.lineTo(point2_draw.x, point2_draw.y);
        path.lineTo(point3_draw.x, point3_draw.y);
        path.lineTo(point1_draw.x, point1_draw.y);
        path.close();

        canvas.drawPath(path, paint);
    }

    public void deleteTexture() {
        //удалаяет все: и битмап, и текстуру с видюхи. Ставит флаги redrawNeeded и postToGlNeeded;
        //если не трогать флаги, то текустура будет автоматически создана при следующей рисовке
        //если не пререрисовать текстуру - ведет себя непредсказуемо
        //но если перед каждой рисовкой стоит проверка на redrawNeed - все нормально
        glDeleteTextures(1, new int[]{texture}, 0);//удалить текстуру с id texture, отступ ноль длина массива 1
        setTextureDeleted(true);
        texture = -1;
        if (bitmap != null) {
            bitmap.recycle();
        }
        setRedrawNeeded(true);
    }

    public boolean isTextureDeleted() {
        return isTextureDeleted;
    }

    public void setTextureDeleted(boolean textureDeleted) {
        isTextureDeleted = textureDeleted;
    }
}
