package com.seal.gl_engine.utils;


import static java.lang.Float.parseFloat;
import static java.lang.Thread.sleep;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.seal.gl_engine.engine.main.animator.Animator;
import com.seal.gl_engine.engine.main.images.PImage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Utils {
    public static Context context;
    public static long programStartTime;
    public static float kx, ky, x, y;

    private static long stopTime;

    public static void background(int r, int b, int g) {
        GLES20.glClearColor(r / 255.0f, g / 255.0f, b / 255.0f, 1);
    }

    public static void showToast(final String text) {
        //Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }


    //лень искать нормальное решение
    public static boolean intInArray(int n, int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == n) {
                return true;
            }
        }
        return false;
    }

    public static float findDrot(float rot, float aimRot) {
        aimRot %= 360;
        rot %= 360;
        if (rot < 0) {
            rot = 360 + rot;
        }
        if (aimRot < 0) {
            aimRot = 360 + aimRot;
        }
        float drot = aimRot - rot;
        if (abs(drot) > 180) {
            if (drot > 0) {
                drot = (360 - drot) % 360;
                return -drot;
            } else {
                drot = 360 - abs(drot);
            }
        }
        return drot;
    }

    public static float sq(float a) {
        return a * a;
    }

    public static float sqrt(float a) {
        return (float) Math.sqrt(a);
    }

    public static float[] contactArray(float[] a, float[] b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        float[] r = new float[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

    public static Animator.Animation[] contactArray(Animator.Animation[] a, Animator.Animation[] b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        Animator.Animation[] r = new Animator.Animation[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

    public static Animator.Animation[] popFromArray(Animator.Animation[] a, Animator.Animation anim) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == anim) {
                Animator.Animation[] buffer = new Animator.Animation[a.length - 1];
                System.arraycopy(a, 0, buffer, 0, i);
                System.arraycopy(a, i + 1, buffer, i, a.length - i - 1);
                return buffer;
            }
        }
        return a;
    }

    public static void onPause() {
        stopTime = millis();
    }

    public static void onResume() {
        long dt = millis() - stopTime;
        programStartTime += dt;
    }

    public static void delay(long t) {
        if (t > 0) {
            try {
                sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static float pow(float a, float b) {
        return (float) Math.pow(a, b);
    }

    public static float cutTail(float i, int s) {
        //оставляет s знаков после запятой у числа i
        float p = (int) Math.pow(10, s);
        return parseInt(i * p) / p;
    }

    public static String getString(int p) {
        return context.getString(p);
    }

    public static PImage scaleImg(PImage img, float w, float h) {
        img.bitmap = Bitmap.createScaledBitmap(img.bitmap, parseInt(w), parseInt(h), true);
        img.width = img.bitmap.getWidth();
        img.height = img.bitmap.getHeight();
        return img;
    }

    public static PImage scaleImg(PImage img, float scale) {
        img.width = img.bitmap.getWidth();
        img.height = img.bitmap.getHeight();
        img.bitmap = Bitmap.createScaledBitmap(img.bitmap, parseInt(img.width * scale), parseInt(img.height * scale), true);
        img.width = img.bitmap.getWidth();
        img.height = img.bitmap.getHeight();
        return img;
    }

    //todo: delete
    public static PImage deleteBackground(PImage img) {
                /*
                if (img == null) {
                    return (null);
                } else {
                    int colors[] = new int[img.bitmap.getWidth() * img.bitmap.getHeight()];
                    int src[] = new int[img.bitmap.getWidth() * img.bitmap.getHeight()];
                    int width = img.bitmap.getWidth();
                    int height = img.bitmap.getHeight();
                    img.bitmap.getPixels(src, 0, width, 0, 0, width, height);
                    for (int x = 0; x < width * height; x++) {

                        if (src[x] > Color.rgb(170, 170, 170)) {
                            //img.bitmap.setPixel(x,y,0);
                            colors[x] = Color.argb(0, 0, 0, 0);
                        } else {
                            //  colors[x] = src[x];
                            colors[x] = Color.rgb(10, 10, 10);
                        }
                    }
                    img.bitmap = Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
                }

                 */
        return (img);


    }

    public static PImage convertTo(PImage img, int red, int green, int blue) {
        if (img != null) {
            int[] colors = new int[img.bitmap.getWidth() * img.bitmap.getHeight()];
            int[] src = new int[img.bitmap.getWidth() * img.bitmap.getHeight()];
            int width = img.bitmap.getWidth();
            int height = img.bitmap.getHeight();
            img.bitmap.getPixels(src, 0, width, 0, 0, width, height);
            int porog = 90;
            for (int x = 0; x < width * height; x++) {
                if (src[x] < Color.rgb(porog, porog, porog)) {
                    //img.bitmap.setPixel(x,y,0);
                    colors[x] = Color.rgb(red, green, blue);
                } else {
                    //colors[x] =src[x];
                }
            }
            img.bitmap = Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
            return (img);
        }


        return (null);
    }

    public static float getDirection(float px, float py, float tx, float ty) {
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


    public static PImage loadImage(String name) {
        try {
            PImage img = new PImage(getBitmapFromAssets(name, context));
            img.width = img.bitmap.getWidth();
            img.height = img.bitmap.getHeight();
            if (img == null) {
                Log.e("ERROR LOADING", name);
            }
            img.setLoaded(true);
            return img;
        } catch (Exception e) {
            Log.e("ERROR LOADING", name + String.valueOf(e.getMessage()));
        }
        return null;
    }

    private static Bitmap getBitmapFromAssets(String fileName, Context context) throws IOException {
        AssetManager assetManager = context.getAssets();

        InputStream istr = assetManager.open(fileName);
        Bitmap bitmap = BitmapFactory.decodeStream(istr);

        return bitmap;
    }

    public static float random(float a, float b) {
        Random random = new Random();
        a *= 100;
        b *= 100;
        if (a > b) {
            float c = b;
            b = a;
            a = c;
        }
        float dif = b - a;
        return (random.nextInt(parseInt(dif + 1)) + a) / 100.0f;
    }

    public static int parseInt(float i) {
        return ((int) i);
    }


    public static int parseInt(String i) {
        return ((int) parseFloat(i));
    }

    public static int parseInt(boolean i) {
        if (i) {
            return (1);
        }
        return (0);
    }

    public static boolean parseBoolean(String s) {
        return parseInt(s) != 0;
    }

    public static boolean parseBoolean(float s) {
        return s != 0;
    }

    public static int[] parseInt(String[] s) {
        int[] out = new int[s.length];
        for (int i = 0; i < s.length; i++) {
            out[i] = parseInt(s[i]);
        }
        return out;
    }

    public static int[] contactArray(int[] a, int[] b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        int[] r = new int[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

    public static int[][] contactArray(int[][] a, int[][] b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        int[][] r = new int[a.length + b.length][(int) max(a[0].length, b[0].length)];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

    public static int[] append(int[] a, int b) {
        int[] r = new int[a.length + 1];
        System.arraycopy(a, 0, r, 0, a.length);
        r[a.length] = b;
        return r;
    }

    public static float[] append(float[] a, float b) {
        float[] r = new float[a.length + 1];
        System.arraycopy(a, 0, r, 0, a.length);
        r[a.length] = b;
        return r;
    }


    public static float degrees(float a) {
        return ((float) Math.toDegrees(a));
    }

    public static float radians(float a) {
        return ((float) Math.toRadians(a));
    }

    public static float atan(float a) {
        return ((float) Math.atan(a));
    }

    public static float atan2(float a, float b) {
        return ((float) Math.atan2(a, b));
    }

    public static float sin(float a) {
        return ((float) Math.sin(a));
    }

    public static float cos(float a) {
        return ((float) Math.cos(a));
    }

    public static float abs(float a) {
        return ((float) Math.abs(a));
    }

    public static float min(float a, float b) {
        if (a < b) {
            return a;
        }
        return b;
    }

    public static float max(float a, float b) {
        return Math.max(a, b);
    }

    public static float max(float a, float b, float c, float d) {
        return (max(max(a, b), max(c, d)));
    }

    public static float tg(float a) {
        return ((float) Math.tan(a));
    }

    public static float map(float val, float vstart, float vstop, float ostart, float ostop) {
        float dif = vstop - vstart;
        //val -= dif;
        val -= vstart;
        float proc = val / dif;
        float dif2 = ostop - ostart;
        float output = dif2 * proc + ostart;
        return output;
    }

    public static long millis() {
        return (System.currentTimeMillis() - programStartTime);
    }

    public static String loadFile(String fileName) {
        String content = "";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(Utils.context.getAssets().open(fileName), StandardCharsets.UTF_8));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                content += mLine + '\n';
            }
        } catch (IOException e) {
            //log the exception
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }
        return content;
    }

    public static int countSubstrs(String str, String target) {
        return (str.length() - str.replace(target, "").length()) / target.length();
    }

    public static String[] split1(String s, char a) {
        String[] out;
        out = s.split(String.valueOf(a));
        return out;
    }

    private static long prevFrame;

    /**
     * Get timing coefficient for physics
     *
     * @return 120/current fps
     */
    public static float getTimeK() {
        float currentFps = 1000.0f / (float) (millis() - prevFrame);
        return 120.0f / currentFps;
    }

}
