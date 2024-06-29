package com.manateam.main;

import static android.opengl.GLES20.glClearColor;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyCameraSettings;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyMatrix;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.applyProjectionMatrix;
import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.resetTranslateMatrix;
import static com.seal.gl_engine.engine.main.shaders.Shader.applyShader;
import static com.seal.gl_engine.utils.Utils.kx;
import static com.seal.gl_engine.utils.Utils.ky;
import static com.seal.gl_engine.utils.Utils.x;
import static com.seal.gl_engine.utils.Utils.y;

import com.example.gl_engine_3_1.R;
import com.manateam.main.adaptors.MainShaderAdaptor;
import com.seal.gl_engine.GamePageInterface;
import com.seal.gl_engine.engine.main.camera.CameraSettings;
import com.seal.gl_engine.engine.main.camera.ProjectionMatrixSettings;
import com.seal.gl_engine.engine.main.shaders.Shader;
import com.seal.gl_engine.engine.main.ui.TextHolder;

public class StartPage implements GamePageInterface {
    private TextHolder welcomeText;
    private final Shader shader;
    private final ProjectionMatrixSettings projectionMatrixSettings;
    private final CameraSettings cameraSettings;
    float[] matrix = resetTranslateMatrix(new float[16]);

    public StartPage() {
        welcomeText = new TextHolder(400 * kx, 100 * ky, this);
        welcomeText.setText("WELCOME TO GLENGINE-3!");
        welcomeText.setPos(x / 2 - 200 * kx, 150 * ky);
        shader = new Shader(R.raw.vertex_shader, R.raw.fragment_shader, this, new MainShaderAdaptor());
        cameraSettings = new CameraSettings(x, y);
        projectionMatrixSettings = new ProjectionMatrixSettings(x, y);

    }

    @Override
    public void draw() {
        glClearColor(1f, 1f, 1f, 1);
        applyShader(shader);
        projectionMatrixSettings.resetFor2d();
        cameraSettings.resetFor2d();
        applyCameraSettings(cameraSettings);
        applyProjectionMatrix(projectionMatrixSettings, false);
        applyMatrix(matrix);
        welcomeText.draw();
    }

    @Override
    public void touchStarted() {

    }

    @Override
    public void touchMoved() {

    }

    @Override
    public void touchEnded() {

    }
}
