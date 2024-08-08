package com.manateam.main;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.glClearColor;
import static com.seal.gl_engine.OpenGLRenderer.fps;
import static com.seal.gl_engine.OpenGLRenderer.mMatrix;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyMatrix;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.resetTranslateMatrix;
import static com.seal.gl_engine.engine.main.shaders.Shader.applyShader;
import static com.seal.gl_engine.utils.Utils.cos;
import static com.seal.gl_engine.utils.Utils.kx;
import static com.seal.gl_engine.utils.Utils.ky;
import static com.seal.gl_engine.utils.Utils.map;
import static com.seal.gl_engine.utils.Utils.millis;
import static com.seal.gl_engine.utils.Utils.radians;

import android.opengl.GLES30;
import android.opengl.Matrix;

import com.example.gl_engine_3_1.R;
import com.manateam.main.adaptors.LightShaderAdaptor;
import com.manateam.main.adaptors.MainShaderAdaptor;
import com.manateam.main.redrawFunctions.MainRedrawFunctions;
import com.seal.gl_engine.GamePageInterface;
import com.seal.gl_engine.OpenGLRenderer;
import com.seal.gl_engine.engine.main.camera.Camera;
import com.seal.gl_engine.engine.main.light.AmbientLight;
import com.seal.gl_engine.engine.main.light.DirectedLight;
import com.seal.gl_engine.engine.main.light.Material;
import com.seal.gl_engine.engine.main.light.SourceLight;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.touch.TouchProcessor;
import com.seal.gl_engine.engine.main.verticles.Poligon;
import com.seal.gl_engine.engine.main.verticles.Shape;
import com.seal.gl_engine.engine.main.verticles.SkyBox;
import com.seal.gl_engine.maths.Point;
import com.seal.gl_engine.maths.Vec3;
import com.seal.gl_engine.utils.SkyBoxShaderAdaptor;
import com.seal.gl_engine.utils.Utils;

public class SecondRenderer implements GamePageInterface {
    private final Poligon fpsPoligon;
    private final Shader shader, lightShader, skyBoxShader;
    Camera camera;
    private final Shape s;
    private final SkyBox skyBox;
    private final SourceLight sourceLight;
    private final AmbientLight ambientLight;
    private final DirectedLight directedLight1;
    private final Material material;

    TouchProcessor touchProcessor;


    public SecondRenderer() {
        shader = new Shader(R.raw.vertex_shader, R.raw.fragment_shader, this, new MainShaderAdaptor());
        lightShader = new Shader(R.raw.vertex_shader_light, R.raw.fragment_shader_light, this, new LightShaderAdaptor());
        fpsPoligon = new Poligon(MainRedrawFunctions::redrawFps, true, 1, this);
        camera = new Camera();
        s = new Shape("ponchik.obj", "texture.png", this);
        s.addNormalMap("noral_tex.png");

        ambientLight = new AmbientLight(this);
        // ambientLight.color = new Vec3(0.3f, 0.3f, 0.3f);

        directedLight1 = new DirectedLight(this);
        directedLight1.direction = new Vec3(-1, 0, 0);
        directedLight1.color = new Vec3(0.9f);
        directedLight1.diffuse = 0.2f;
        directedLight1.specular = 0.8f;
       /* directedLight2 = new DirectedLight(this);
        directedLight2.direction = new Vec3(0, 1, 0);
        directedLight2.color = new Vec3(0.6f);
        directedLight2.diffuse = 0.9f;
        directedLight2.specular = 0.8f;

        */
        sourceLight = new SourceLight(this);
        sourceLight.diffuse = 0.8f;
        sourceLight.specular = 0.9f;
        sourceLight.constant = 1f;
        sourceLight.linear = 0.01f;
        sourceLight.quadratic = 0.01f;
        sourceLight.color = new Vec3(0.5f);
        sourceLight.position = new Vec3(2.7f, 0, 0);
        sourceLight.direction = new Vec3(-0.3f, 0, 0);
        sourceLight.outerCutOff = cos(radians(40));
        sourceLight.cutOff = cos(radians(30f));

        material = new Material(this);
        material.ambient = new Vec3(1);
        material.specular = new Vec3(1);
        material.diffuse = new Vec3(1);
        material.shininess = 1.1f;

        skyBox = new SkyBox("skybox/", "jpg", this);
        skyBoxShader = new Shader(R.raw.skybox_vertex, R.raw.skybox_fragment, this, new SkyBoxShaderAdaptor());

        touchProcessor = new TouchProcessor(MotionEvent -> true, touchPoint -> {
            OpenGLRenderer.startNewPage(new MainRenderer());
            return null;
        }, null, null, this);
    }

    @Override
    public void initialize() {}

    @Override
    public void draw() {
        GLES30.glDisable(GL_BLEND);
        camera.resetFor3d();
        camera.cameraSettings.eyeZ = 0f;
        camera.cameraSettings.eyeX = 5f;
        float x = 3.5f * Utils.sin(millis() / 1000.0f);
        camera.cameraSettings.centerY = 0;
        camera.cameraSettings.centerZ = x;
        applyShader(skyBoxShader);
        camera.apply();
        skyBox.prepareAndDraw();
        applyShader(lightShader);
        material.apply();
        glClearColor(1f, 1, 1, 1);
        camera.apply();
        mMatrix = resetTranslateMatrix(mMatrix);
        Matrix.rotateM(mMatrix, 0, map(millis() % 10000, 0, 10000, 0, 360), 1, 0.5f, 0);
        Matrix.translateM(mMatrix, 0, 0, -0f, 0);
        Matrix.scaleM(mMatrix, 0, 0.5f, 0.5f, 0.55f);
        applyMatrix(mMatrix);
        //FrameBufferUtils.connectFrameBuffer(frameBuffer.getFrameBuffer());
        s.prepareAndDraw();
       // FrameBufferUtils.connectDefaultFrameBuffer();

        applyShader(shader);
        fpsPoligon.setRedrawNeeded(true);
        camera.resetFor2d();
       camera.apply();
        mMatrix = resetTranslateMatrix(mMatrix);
        applyMatrix(mMatrix);
        fpsPoligon.redrawParams.set(0, String.valueOf(fps));
        fpsPoligon.redrawNow();
        fpsPoligon.prepareAndDraw(new Point(0 * kx, 0, 1), new Point(100 * kx, 0, 1), new Point(0 * kx, 100 * ky, 1));
        //frameBuffer.drawTexture(new Point(0, 0, 1), new Point(Utils.x, 0, 1), new Point(Utils.x, y, 1));
    }
}
