package com.manateam.glengine3.engine.main.shaders;

import static com.manateam.glengine3.engine.main.shaders.ShaderUtils.createShaderProgram;

import android.opengl.GLES20;
import android.util.Log;

import com.manateam.glengine3.GamePageInterface;
import com.manateam.glengine3.OpenGLRenderer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Shader { //means shader program
    private static List<WeakReference<Shader>> allShaders = new ArrayList<>();
    private int link;
    private int vertex, fragment, geom = -1;
    private String page = "";
    private boolean reloadNeeded = false;

    public Shader(int vertex, int fragment, GamePageInterface page) {
        link = createShaderProgram(vertex, fragment);
        this.vertex = vertex;
        this.fragment = fragment;
        if (page != null) {
            this.page = (String) page.getClass().getName();
        }
        allShaders.add(new WeakReference<>(this));
    }

    public Shader(int vertex, int fragment, int geom, GamePageInterface page) {
        link = createShaderProgram(vertex, fragment, geom);
        this.vertex = vertex;
        this.fragment = fragment;
        this.geom = geom;
        if (page != null) {
            this.page = (String) page.getClass().getName();
        }
        allShaders.add(new WeakReference<>(this));
    }

    private void updateLocation() {
        reloadNeeded = true;
    }

    private void reload() {
        this.delete();
        if (geom == -1) {
            link = createShaderProgram(vertex, fragment);
        } else {
            link = createShaderProgram(vertex, fragment, geom);
        }
    }

    public static void updateAllLocations() {
        for (int i = 0; i < allShaders.size(); i++) {
            if (allShaders.get(i).get() != null) {
                allShaders.get(i).get().updateLocation();
            }
        }
    }

    private boolean unneded() {
        if(this.page.equals("")){
            return false;
        }
        if (!this.page.equals(OpenGLRenderer.getPageClassName())) {
            this.delete();
            return true;
        }
        return false;
    }

    public void delete() {
        GLES20.glDeleteProgram(link);
    }

    public static void applyShader(Shader s) {
        if (s.reloadNeeded && redrawPassed) {
            s.reload();
            Log.e("reloaf", "sdf");
            s.reloadNeeded = false;
        }
        ShaderUtils.applyShader(s.link);
    }


    public static void onPageChange() {
        Iterator<WeakReference<Shader>> iterator = allShaders.iterator();
        while (iterator.hasNext()) {
            WeakReference<Shader> e = iterator.next();
            if (e.get() == null) {
                iterator.remove();
            } else if (e.get().unneded()) {
                iterator.remove();
            }
        }
    }

    private static boolean redrawPassed = false;

    public static void redrawSetup() {
        redrawPassed = true;
    }
}
