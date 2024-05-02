package com.manateam.main;

import static android.opengl.GLES20.glClearColor;
import static com.seal.gl_engine.OpenGLRenderer.fps;
import static com.seal.gl_engine.OpenGLRenderer.mMatrix;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyCameraSettings;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyMatrix;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyProjectionMatrix;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.resetTranslateMatrix;
import static com.seal.gl_engine.engine.main.shaders.Shader.applyShader;
import static com.seal.gl_engine.utils.Utils.cos;
import static com.seal.gl_engine.utils.Utils.kx;
import static com.seal.gl_engine.utils.Utils.ky;
import static com.seal.gl_engine.utils.Utils.map;
import static com.seal.gl_engine.utils.Utils.millis;
import static com.seal.gl_engine.utils.Utils.radians;
import static com.seal.gl_engine.utils.Utils.sin;
import static com.seal.gl_engine.utils.Utils.x;
import static com.seal.gl_engine.utils.Utils.y;

import android.opengl.Matrix;
import android.util.Log;

import com.example.gl_engine_3_1.R;
import com.manateam.main.adaptors.LightShaderAdaptor;
import com.manateam.main.adaptors.MainShaderAdaptor;
import com.manateam.main.redrawFunctions.MainRedrawFunctions;
import com.seal.gl_engine.GamePageInterface;
import com.seal.gl_engine.OpenGLRenderer;
import com.seal.gl_engine.engine.main.camera.CameraSettings;
import com.seal.gl_engine.engine.main.camera.ProjectionMatrixSettings;
import com.seal.gl_engine.engine.main.light.AmbientLight;
import com.seal.gl_engine.engine.main.light.DirectedLight;
import com.seal.gl_engine.engine.main.light.Material;
import com.seal.gl_engine.engine.main.light.SourceLight;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.verticles.Poligon;
import com.seal.gl_engine.engine.main.verticles.Shape;
import com.seal.gl_engine.engine.main.verticles.SkyBox;
import com.seal.gl_engine.maths.Point;
import com.seal.gl_engine.maths.Vec3;
import com.seal.gl_engine.utils.SkyBoxShaderAdaptor;

public class SecondRenderer implements GamePageInterface {
    private final Poligon fpsPoligon;
    private final Shader shader, lightShader, skyBoxShader;
    private final ProjectionMatrixSettings projectionMatrixSettings;
    private final CameraSettings cameraSettings;
    private final Shape s;
    private SkyBox skyBox;
    private SourceLight sourceLight;
    private final AmbientLight ambientLight;
    private DirectedLight directedLight1;
    private Material material;


    public SecondRenderer() {
        shader = new Shader(R.raw.vertex_shader, R.raw.fragment_shader, this, new MainShaderAdaptor());
        lightShader = new Shader(R.raw.vertex_shader_light, R.raw.fragment_shader_light, this, new LightShaderAdaptor());
        fpsPoligon = new Poligon(MainRedrawFunctions::redrawFps, true, 1, this);
        cameraSettings = new CameraSettings(x, y);
        cameraSettings.resetFor3d();
        projectionMatrixSettings = new ProjectionMatrixSettings(x, y);
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

        material=new Material(this);
        material.ambient=new Vec3(1);
        material.specular=new Vec3(1);
        material.diffuse=new Vec3(1);
        material.shininess=1.1f;

        skyBox = new SkyBox("skybox/", "jpg", this);
        skyBoxShader = new Shader(R.raw.skybox_vertex, R.raw.skybox_fragment, this, new SkyBoxShaderAdaptor());
    }

    @Override
    public void draw() {
        cameraSettings.resetFor3d();
        projectionMatrixSettings.resetFor3d();
        cameraSettings.eyeZ = 0f;
        cameraSettings.eyeX = 5f;
        cameraSettings.centerX = 0.5f * sin(millis() / 1000.0f);
        cameraSettings.centerY = 0;
        cameraSettings.centerZ = 0;
        applyShader(skyBoxShader);
        applyProjectionMatrix(projectionMatrixSettings);
        applyCameraSettings(cameraSettings);

        skyBox.prepareAndDraw();

        applyShader(lightShader);
        material.apply();

        glClearColor(1f, 1, 1, 1);

        applyCameraSettings(cameraSettings);
        applyProjectionMatrix(projectionMatrixSettings);
        mMatrix = resetTranslateMatrix(mMatrix);
        Matrix.rotateM(mMatrix, 0, map(millis() % 10000, 0, 10000, 0, 360), 1, 0.5f, 0);
        Matrix.scaleM(mMatrix, 0, 1.5f, 1.5f, 1.5f);
        applyMatrix(mMatrix);
        s.prepareAndDraw();


        applyShader(shader);
        fpsPoligon.setRedrawNeeded(true);
        cameraSettings.resetFor2d();
        projectionMatrixSettings.resetFor2d();
        applyProjectionMatrix(projectionMatrixSettings, false);
        applyCameraSettings(cameraSettings);
        mMatrix = resetTranslateMatrix(mMatrix);
        applyMatrix(mMatrix);
        fpsPoligon.redrawParams.set(0, String.valueOf(fps));
        fpsPoligon.redrawNow();
        fpsPoligon.prepareAndDraw(new Point(0 * kx, 0, 1), new Point(100 * kx, 0, 1), new Point(0 * kx, 100 * ky, 1));
    }

    @Override
    public void touchStarted() {
        Log.e("touch", "statred");
        OpenGLRenderer.startNewPage(new MainRenderer());
    }

    @Override
    public void touchMoved() {

    }

    @Override
    public void touchEnded() {

    }
}
