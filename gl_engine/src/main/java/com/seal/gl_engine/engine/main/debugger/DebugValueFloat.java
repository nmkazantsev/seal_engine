package com.seal.gl_engine.engine.main.debugger;

import static com.seal.gl_engine.utils.Utils.x;
import static com.seal.gl_engine.utils.Utils.y;

import android.graphics.Paint;

import com.seal.gl_engine.engine.main.images.PImage;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Create object of this class to create a debuggable from engine value
 * public value is your source value. Use it like a usual value, but it can be changed with debugger.
 */
public class DebugValueFloat {
    public float value;
    protected float min;
    protected float max;
    protected String name;

    /**
     * Create a debug value.
     *
     * @param min  minimum on slider
     * @param max  maximum on slider
     * @param name shown name of this value, unique not null
     */
    public DebugValueFloat(float min, float max, @NotNull String name) {
        this.max = max;
        this.min = min;
        this.name = name;
        Debugger.addDebugValue(this);
    }

    protected PImage drawImage(List<Objects> params) {
        PImage image = new PImage(x, y); //fuck memory economy
        image.background(255, 255, 255, 150);
        image.textAlign(Paint.Align.CENTER);
        image.text(value, x / 2, x / 2);
        return image;
    }
}
