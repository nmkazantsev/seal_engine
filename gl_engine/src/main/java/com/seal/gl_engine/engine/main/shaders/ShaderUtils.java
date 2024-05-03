package com.seal.gl_engine.engine.main.shaders;

import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES30.GL_COMPILE_STATUS;
import static android.opengl.GLES30.GL_LINK_STATUS;
import static android.opengl.GLES30.glAttachShader;
import static android.opengl.GLES30.glCompileShader;
import static android.opengl.GLES30.glCreateProgram;
import static android.opengl.GLES30.glCreateShader;
import static android.opengl.GLES30.glDeleteProgram;
import static android.opengl.GLES30.glDeleteShader;
import static android.opengl.GLES30.glGetError;
import static android.opengl.GLES30.glGetProgramiv;
import static android.opengl.GLES30.glGetShaderiv;
import static android.opengl.GLES30.glLinkProgram;
import static android.opengl.GLES30.glShaderSource;
import static android.opengl.GLES32.GL_GEOMETRY_SHADER;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import com.seal.gl_engine.utils.Utils;
import com.seal.gl_engine.utils.FileUtils;


public class ShaderUtils {

    protected static int createProgram(int vertexShaderId, int fragmentShaderId) {

        final int programId = glCreateProgram();
        if (programId == 0) {
            return 0;
        }
        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);

        glLinkProgram(programId);
        final int[] linkStatus = new int[1];
        glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            Log.e("error compiling shaders", String.valueOf(glGetError()));
            Log.e("Load Shader Failed" , "loading\n" + GLES30.glGetProgramInfoLog(programId));
            glDeleteProgram(programId);
            return 0;
        }
        return programId;

    }

    protected static int createProgram(int vertexShaderId, int fragmentShaderId, int geomShaderId) {

        final int programId = glCreateProgram();
        if (programId == 0) {
            return 0;
        }
        glAttachShader(programId, vertexShaderId);
        glAttachShader(programId, fragmentShaderId);
        glAttachShader(programId, geomShaderId);
        glLinkProgram(programId);
        final int[] linkStatus = new int[1];
        glGetProgramiv(programId, GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            Log.e("error compiling shaders", String.valueOf(glGetError()));
            Log.e("Load Shader Failed" , "loading\n" + GLES30.glGetProgramInfoLog(programId));
            glDeleteProgram(programId);
            return 0;
        }
        return programId;

    }

    protected static int createShader(Context context, int type, int shaderRawId) {
        String shaderText = FileUtils
                .readTextFromRaw(context, shaderRawId);
        return ShaderUtils.createShader(type, shaderText);
    }

    static int createShader(int type, String shaderText) {
        final int shaderId = glCreateShader(type);
        if (shaderId == 0) {
            Log.e("Load Shader Failed" + shaderText, "loading\n" + GLES30.glGetShaderInfoLog(shaderId));

            return 0;
        }
        glShaderSource(shaderId, shaderText);
        glCompileShader(shaderId);
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderId, GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            Log.e("Load Shader Failed" + shaderText, "Compilation\n" + GLES30.glGetShaderInfoLog(shaderId));
            glDeleteShader(shaderId);
            return 0;
        }
        return shaderId;
    }

    protected static int createShaderProgram(int vertexShader, int fragmentShader) {
        int programId;
        int vertexShaderId = ShaderUtils.createShader(Utils.context, GL_VERTEX_SHADER, vertexShader);
        int fragmentShaderId = ShaderUtils.createShader(Utils.context, GL_FRAGMENT_SHADER, fragmentShader);
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId);
        return programId;
    }

    protected static int createShaderProgram(int vertexShader, int fragmentShader, int geomShader) {
        int programId;
        int vertexShaderId = ShaderUtils.createShader(Utils.context, GL_VERTEX_SHADER, vertexShader);
        int fragmentShaderId = ShaderUtils.createShader(Utils.context, GL_FRAGMENT_SHADER, fragmentShader);
        int geomShaderId = ShaderUtils.createShader(Utils.context, GL_GEOMETRY_SHADER, geomShader);
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId, geomShaderId);
        return programId;
    }

    static int prevProgramId;

    protected static void applyShader(int programId) {
        if (programId != prevProgramId) {
            glUseProgram(programId);
            prevProgramId = programId;
        }
    }

}
