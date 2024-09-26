package com.manateam.main;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.glClearColor;
import static com.seal.gl_engine.OpenGLRenderer.mMatrix;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyMatrix;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.resetTranslateMatrix;
import static com.seal.gl_engine.engine.main.frameBuffers.FrameBufferUtils.createGBuffer;
import static com.seal.gl_engine.engine.main.shaders.Shader.applyShader;
import static com.seal.gl_engine.utils.Utils.cos;
import static com.seal.gl_engine.utils.Utils.map;
import static com.seal.gl_engine.utils.Utils.millis;
import static com.seal.gl_engine.utils.Utils.radians;
import static com.seal.gl_engine.utils.Utils.x;
import static com.seal.gl_engine.utils.Utils.y;

import android.opengl.GLES30;
import android.opengl.Matrix;

import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.OpenGLRenderer;
import com.seal.gl_engine.default_adaptors.LightShaderAdaptor;
import com.seal.gl_engine.default_adaptors.MainShaderAdaptor;
import com.seal.gl_engine.engine.main.camera.Camera;
import com.seal.gl_engine.engine.main.debugger.DebugValueFloat;
import com.seal.gl_engine.engine.main.debugger.Debugger;
import com.seal.gl_engine.engine.main.frameBuffers.FrameBufferUtils;
import com.seal.gl_engine.engine.main.frameBuffers.GBuffer;
import com.seal.gl_engine.engine.main.light.AmbientLight;
import com.seal.gl_engine.engine.main.light.DirectedLight;
import com.seal.gl_engine.engine.main.light.Material;
import com.seal.gl_engine.engine.main.light.SourceLight;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.touch.TouchProcessor;
import com.seal.gl_engine.engine.main.verticles.Shape;
import com.seal.gl_engine.engine.main.verticles.SkyBox;
import com.seal.gl_engine.maths.Point;
import com.seal.gl_engine.maths.Vec3;
import com.seal.gl_engine.utils.SkyBoxShaderAdaptor;
import com.seal.gl_engine.utils.Utils;

public class SecondRenderer extends GamePageClass {
    private final Shader shader, lightShader, skyBoxShader, firstPassDefredShader, renderPassDeferedShader;
    Camera camera, lightCamera;
    private final Shape s, s2;
    private final SkyBox skyBox;
    private  SourceLight sourceLight;
    private final AmbientLight ambientLight;
    private final DirectedLight directedLight1;
    private final Material material;
    private GBuffer frameBuffer;
    TouchProcessor touchProcessor;

    DebugValueFloat camPos, turn, zShift;

    public SecondRenderer() {
        shader = new Shader(com.example.gl_engine.R.raw.vertex_shader, com.example.gl_engine.R.raw.fragment_shader, this, new MainShaderAdaptor());
        lightShader = new Shader(com.example.gl_engine.R.raw.vertex_shader_light, com.example.gl_engine.R.raw.fragment_shader_light, this, new LightShaderAdaptor());
        firstPassDefredShader = new Shader(com.example.gl_engine.R.raw.vertex_deffered_geometry_shader, com.example.gl_engine.R.raw.fragment_deferred_geometry_shader, this, new LightShaderAdaptor());
        renderPassDeferedShader = new Shader(com.example.gl_engine.R.raw.vertex_deffered_render_shader, com.example.gl_engine.R.raw.fragment_deffered_render_shader, this, new LightShaderAdaptor());
        camera = new Camera();
        lightCamera = new Camera(x, y);
        s = new Shape("ponchik.obj", "texture.png", this);
        s2 = new Shape("cube.obj", "texture.png", this);
        s.addNormalMap("noral_tex.png");

        ambientLight = new AmbientLight(this);
        // ambientLight.color = new Vec3(0.3f, 0.3f, 0.3f);

        directedLight1 = new DirectedLight(this);
        directedLight1.direction = new Vec3(1, 1, 0);
        directedLight1.color = new Vec3(0.9f);
        directedLight1.diffuse = 0.9f;
        directedLight1.specular = 0.8f;
       /* directedLight2 = new DirectedLight(this);
        directedLight2.direction = new Vec3(0, 1, 0);
        directedLight2.color = new Vec3(0.6f);
        directedLight2.diffuse = 0.9f;
        directedLight2.specular = 0.8f;

        */
        for(int i=0;i<3;i++) {
            sourceLight = new SourceLight(this);
            sourceLight.diffuse = 0.8f;
            sourceLight.specular = 0.9f;
            sourceLight.constant = 0.1f;
            sourceLight.linear = 0.01f;
            sourceLight.quadratic = 0.01f;
            sourceLight.color = new Vec3(0.5f);
            sourceLight.position = new Vec3(2.7f+i, 0, 0);
            sourceLight.direction = new Vec3(-0.3f, 0, 0);
            sourceLight.outerCutOff = cos(radians(40));
            sourceLight.cutOff = cos(radians(30f));
        }

        material = new Material(this);
        material.ambient = new Vec3(1);
        material.specular = new Vec3(1);
        material.diffuse = new Vec3(1);
        material.shininess = 1.1f;

        skyBox = new SkyBox("skybox/", "jpg", this);
        skyBoxShader = new Shader(com.example.gl_engine.R.raw.skybox_vertex, com.example.gl_engine.R.raw.skybox_fragment, this, new SkyBoxShaderAdaptor());

        touchProcessor = new TouchProcessor(MotionEvent -> true, touchPoint -> {
            OpenGLRenderer.startNewPage(new MainRenderer());
            return null;
        }, null, null, this);
        frameBuffer = createGBuffer(3, x/2, y/2, this);

        camPos = Debugger.addDebugValueFloat(1, 5, "cam pos");
        camPos.value = 2;
        turn = Debugger.addDebugValueFloat(0, 360, "turn");
        turn.value = 0;
        zShift = Debugger.addDebugValueFloat(0.5f, 4, "z shift");
        zShift.value = 1;
        lightCamera.resetFor3d();
    }


    @Override
    public void draw() {
        GLES30.glDisable(GL_BLEND);
        frameBuffer.apply();
        camera.resetFor3d();
        camera.cameraSettings.eyeY = camPos.value;
        camera.cameraSettings.eyeZ = zShift.value;
        camera.cameraSettings.eyeX = 1.5f;
        float x = 2.5f * Utils.sin(millis() / 1000.0f) * 0;
        camera.cameraSettings.centerY = 0;
        camera.cameraSettings.centerZ = 0;
        camera.cameraSettings.centerX = 0;
        applyShader(firstPassDefredShader);
        // lightCamera.apply(false);
        camera.apply();
        drawScene();
        //s.setRedrawNeeded(false);
        FrameBufferUtils.connectDefaultFrameBuffer();
        //applyShader(skyBoxShader);
        //camera.apply();
        // skyBox.prepareAndDraw();
        applyShader(renderPassDeferedShader);
        //camera.apply();
        // drawScene();
        int i= GLES30.glGetUniformLocation(15, "normalMap");
        camera.resetFor2d();
        camera.apply();
        material.apply();
        mMatrix = resetTranslateMatrix(mMatrix);
        applyMatrix(mMatrix);
        //pass contents to shader
        frameBuffer.drawAllTextures( new Point(Utils.x, y),  new Point(0, y, 1), new Point(Utils.x,  1));

        applyShader(shader);
        camera.apply();
        applyMatrix(mMatrix);
        frameBuffer.drawTexture(2, new Point(0,0, 2),  new Point(Utils.x / 3, 0, 2), new Point(0, y / 3, 2));
    }

    private void drawScene() {
        material.apply();
        glClearColor(1f, 1, 1, 1);
        mMatrix = resetTranslateMatrix(mMatrix);
        Matrix.translateM(mMatrix, 0, 0, -3, 0);
        Matrix.scaleM(mMatrix, 0, 6, 0.1f, 6);
        applyMatrix(mMatrix);
        s2.prepareAndDraw();
        mMatrix = resetTranslateMatrix(mMatrix);
        Matrix.rotateM(mMatrix, 0, map(millis() % 10000, 0, 10000, turn.value, turn.value), 1, 1, 0);
        Matrix.translateM(mMatrix, 0, 0, -0f, 0);
        Matrix.scaleM(mMatrix, 0, 0.5f, 0.5f, 0.55f);
        applyMatrix(mMatrix);
        s.prepareAndDraw();
    }
}
