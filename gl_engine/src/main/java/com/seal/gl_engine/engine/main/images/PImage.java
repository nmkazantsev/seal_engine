package com.seal.gl_engine.engine.main.images;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.seal.gl_engine.maths.Section;
import com.seal.gl_engine.utils.Utils;

public class PImage {
    public Bitmap bitmap;
    public float width, height;
    private boolean isLoaded = false;

    float textSize;
    public Paint paintImg = new Paint();
    public Canvas canvas;
    public Paint paint, stroke;

    public boolean upperText = false;

    public void setFont(String font) {
        Typeface tf = Typeface.createFromAsset(Utils.context.getAssets(), font);
        paint.setTypeface(tf);
    }

    public PImage(Bitmap b) {
        bitmap = Bitmap.createBitmap(b.getWidth(), b.getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        stroke = new Paint();
        paint.setAntiAlias(true);
        stroke.setAntiAlias(true);
        canvas.drawBitmap(b, 0, 0, paintImg);
    }

    public PImage(float x, float y) {
        bitmap = Bitmap.createBitmap((int) x, (int) y, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        stroke = new Paint();
        paint.setAntiAlias(true);
        stroke.setAntiAlias(true);
    }

    //после нее сам img не равне null, но bitmap удаляется
    public void delete() {
        if (isLoaded) {
            bitmap.recycle();
            bitmap = null;
            isLoaded = false;
        }
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean loaded) {
        isLoaded = loaded;
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
        background(Utils.parseInt(r), Utils.parseInt(g), Utils.parseInt(b));
    }

    public void background(float r, float g, float b, float a) {
        canvas.drawColor(Color.argb((int) a, (int) r, (int) g, (int) b));
    }

    public void roundRect(float x, float y, float a, float b, float rx, float ry) {
        a = x + a;
        b = y + b;
        canvas.drawRoundRect(x, y, a, b, rx, ry, paint);
        canvas.drawRoundRect(x, y, a, b, rx, ry, stroke);
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
        fill(Utils.parseInt(i));
    }

    public void fill(float r, float g, float b) {
        fill(Utils.parseInt(r), Utils.parseInt(g), Utils.parseInt(b));
    }

    public void fill(float r, float g, float b, float a) {
        fill(Utils.parseInt(r), Utils.parseInt(g), Utils.parseInt(b), Utils.parseInt(a));
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
                canvas.drawText(lines[i], x, y + (i + 1) * textSize * k, paint);
            }
        } else {
            for (int i = 0; i < lines.length; i++) {
                canvas.drawText(lines[i], x, y + (i) * textSize * k, paint);
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
        canvas.drawBitmap(img.bitmap, x - (float) img.bitmap.getWidth() / 2, y - (float) img.bitmap.getHeight() / 2, paintImg);
    }

    public void image(PImage img, float x, float y, float a, float b) {
        Matrix matrixImg = new Matrix();

        float ikx, iky;
        ikx = a / img.width;
        iky = b / img.height;
        matrixImg.preTranslate(x / ikx - (float) img.bitmap.getWidth() / (2), y / iky - (float) img.bitmap.getHeight() / (2));
        matrixImg.postScale(ikx, iky);
        //  Paint paintImg = new Paint();
        canvas.drawBitmap(img.bitmap, matrixImg, paintImg);
    }

    public void image(PImage img, float x, float y, float scale) {

        Matrix matrixImg = new Matrix();
        matrixImg.postScale(scale, scale);
        matrixImg.preTranslate(x / scale - (float) img.bitmap.getWidth() / (2), y / scale - (float) img.bitmap.getHeight() / (2));
        // Paint paintImg = new Paint();
        canvas.drawBitmap(img.bitmap, matrixImg, paintImg);


    }

    public void rotImage(PImage img, float x, float y, float scale, float rot) {
        Matrix matrixImg = new Matrix();
        matrixImg.postScale(scale, scale);
        matrixImg.preTranslate(x / scale - (float) img.bitmap.getWidth() / (2), y / scale - (float) img.bitmap.getHeight() / (2));
        Paint paintImg = new Paint();
        canvas.rotate(degrees(rot), x, y);
        canvas.drawBitmap(img.bitmap, matrixImg, paintImg);
        canvas.rotate(-degrees(rot), x, y);
    }

    public float degrees(float a) {
        return ((float) Math.toDegrees(a));
    }

    public float radians(float a) {
        return ((float) Math.toRadians(a));
    }

    public void ellipse(float px, float py, float r1, float r2) {
        paint.setAntiAlias(true);
        stroke.setAntiAlias(true);
        canvas.drawOval(px - r1, py - r2, px + r1, py + r2, stroke);
        canvas.drawOval(px - r1, py - r2, px + r1, py + r2, paint);
        stroke.setAntiAlias(false);
        paint.setAntiAlias(false);
    }

    public void line(float x1, float y1, float x2, float y2) {
        canvas.drawLine(x1, y1, x2, y2, stroke);
    }

    public void line(Section section) {
        canvas.drawLine(section.getBaseVector().x, section.getBaseVector().y, section.getBaseVector().x + section.getDirectionVector().x, section.getBaseVector().y + section.getDirectionVector().y, stroke);
    }

    public void noStroke() {
        stroke.setStrokeWidth(0);
        stroke.setARGB(0, 0, 0, 0);
    }

    public void drawSector(float centerX,
                           float centerY,
                           float radius,
                           float startAngle,
                           float sweepAngle,
                           boolean includeCenter) {
        //use magic from depp seek
        drawSect(canvas, centerX, centerY, radius, startAngle, sweepAngle, paint, includeCenter);
    }

    //magic from depp seek
    private static void drawSect(
            Canvas canvas,
            float centerX,
            float centerY,
            float radius,
            float startAngle,
            float sweepAngle,
            Paint paint,
            boolean includeCenter
    ) {
        RectF rect = new RectF(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );

        canvas.drawArc(rect, startAngle, sweepAngle, includeCenter, paint);
    }


}