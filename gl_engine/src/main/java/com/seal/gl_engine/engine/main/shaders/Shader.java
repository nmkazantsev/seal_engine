package com.seal.gl_engine.engine.main.shaders;

import static com.seal.gl_engine.engine.main.shaders.ShaderUtils.createShaderProgram;

import android.opengl.GLES20;

import com.seal.gl_engine.OpenGLRenderer;
import com.seal.gl_engine.GamePageInterface;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Shader { //means shader program
    private static List<Shader> allShaders = new ArrayList<>();
    private int link;
    private int vertex, fragment, geom = -1;
    private String page = "";
    private boolean reloadNeeded = false;
    private Adaptor adaptor;
    private static Shader activeShader;

    public Shader(int vertex, int fragment, GamePageInterface page, Adaptor adaptor) {
        link = createShaderProgram(vertex, fragment);
        this.vertex = vertex;
        this.fragment = fragment;
        if (page != null) {
            this.page = (String) page.getClass().getName();
        }
        allShaders.add(this);
        this.adaptor = adaptor;
        adaptor.setProgramId(link);
    }

    public Shader(int vertex, int fragment, int geom, GamePageInterface page, Adaptor adaptor) {
        link = createShaderProgram(vertex, fragment, geom);
        this.vertex = vertex;
        this.fragment = fragment;
        this.geom = geom;
        if (page != null) {
            this.page = (String) page.getClass().getName();
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
        if (this.page.equals("")) {
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
        if (s.reloadNeeded) {
            s.reload();
            s.reloadNeeded = false;
        }
        ShaderUtils.applyShader(s.link);
        activeShader = s;
        s.adaptor.programId = s.link;
        s.adaptor.updateLocations();
        Adaptor.updateLightLocations();
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
