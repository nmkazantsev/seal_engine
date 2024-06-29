package com.seal.gl_engine.engine.main.ui;

import com.seal.gl_engine.GamePageInterface;
import com.seal.gl_engine.engine.main.images.PImage;
import com.seal.gl_engine.engine.main.verticles.SimplePolygon;
import com.seal.gl_engine.maths.Vec3;

import java.util.List;

public class TextHolder extends UI_Object {
    private String text="";
    private float sizeX, sizeY;
    private float px, py, pz = 0.1f;
    private Vec3 color = new Vec3(0);
    private SimplePolygon polygon;

    public TextHolder(float sizeX, float sizeY, GamePageInterface page) {
        super(page);
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        polygon = new SimplePolygon(this::redrawFunc, true, 0, page);
    }

    public void setText(String text) {
        this.text = text;
        polygon.setRedrawNeeded(true);
        polygon.redrawNow();
    }

    public void setPos(float x, float y) {
        this.px = x;
        this.py = y;
    }

    public void setZ(float z) {
        this.pz = z;
    }

    public void setColor(Vec3 newColor) {
        this.color = newColor;
        polygon.setRedrawNeeded(true);
        polygon.redrawNow();
    }


    private PImage redrawFunc(List<Object> params) {
        //todo: add text wrap
        PImage img = new PImage(sizeX, sizeY);
        img.background(255);
        img.textSize(20);
        img.fill(color.x, color.y, color.z);
        img.text(text, 0, 0);
        return img;
    }

    @Override
    public void draw() {
        polygon.prepareAndDraw(px, py, sizeX, sizeY, pz);
    }

    @Override
    public boolean checkHitbox(float tx, float ty) {
        return false;
    }
}
