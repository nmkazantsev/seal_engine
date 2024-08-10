package com.seal.gl_engine.engine.main.shaders;

import static com.seal.gl_engine.engine.main.shaders.ShaderUtils.createShaderProgram;

import android.opengl.GLES20;

import com.seal.gl_engine.OpenGLRenderer;
import com.seal.gl_engine.GamePageClass;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Shader { //means shader program
    private static final List<Shader> allShaders = new ArrayList<>();
    private int link;
    private final int vertex;
    private final int fragment;
    private int geom = -1;
    private Class<?> page;
    private boolean reloadNeeded = false;
    private final Adaptor adaptor;
    private static Shader activeShader;

    public Shader(int vertex, int fragment, GamePageClass page, Adaptor adaptor) {
        link = createShaderProgram(vertex, fragment);
        this.vertex = vertex;
        this.fragment = fragment;
        if (page != null) {
            this.page = page.getClass();
        }
        allShaders.add(this);
        this.adaptor = adaptor;
        adaptor.setProgramId(link);
    }

    public Shader(int vertex, int fragment, int geom, GamePageClass page, Adaptor adaptor) {
        link = createShaderProgram(vertex, fragment, geom);
        this.vertex = vertex;
        this.fragment = fragment;
        this.geom = geom;
        if (page != null) {
            this.page = page.getClass();
        }
        allShaders.add(this);
        this.adaptor = adaptor;
        adaptor.setProgramId(link);
    }

    private void reload() {
        //this.delete();
        if (geom == -1) {
            link = createShaderProgram(vertex, fragment);
        } else {
            link = createShaderProgram(vertex, fragment, geom);
        }
    }

    public static void updateAllLocations() {
        ShaderUtils.prevProgramId = -1;
        for (int i = 0; i < allShaders.size(); i++) {
            if (allShaders.get(i) != null) {
                allShaders.get(i).reloadNeeded = true;
            }
        }
    }

    private boolean unneeded() {
        if (this.page == null) {
            return false;
        }
        if (!(this.page == OpenGLRenderer.getPageClass())) {
            this.delete();
            return true;
        }
        return false;
    }

    public void delete() {
        GLES20.glDeleteProgram(link);
    }


    public static void applyShader(Shader s) {
        if (s.reloadNeeded) {
            s.reload();
            s.reloadNeeded = false;
        }
        ShaderUtils.applyShader(s.link);
        activeShader = s;
        s.adaptor.programId = s.link;
        s.adaptor.updateLocations();
        Adaptor.updateShaderDataLocations();
        Adaptor.forwardData();
    }


    public static void onPageChange() {
        Iterator<Shader> iterator = allShaders.iterator();
        while (iterator.hasNext()) {
            Shader e = iterator.next();
            if (e == null) {
                iterator.remove();
            } else if (e.unneeded()) {
                e.delete();
                iterator.remove();
            }
        }
    }

    public Adaptor getAdaptor() {
        return adaptor;
    }

    public static Shader getActiveShader() {
        return activeShader;
    }
}
