package com.manateam.glengine3.engine.main.verticles;

import com.manateam.glengine3.maths.Point;

public class Face {
    private Point[] vertices;
    private Point[] textureCoordinates;
    private Point normal;
    public Face(Point[] vertices, Point[] textureCoordinates, Point normal) {
        this.vertices = vertices;
        this.textureCoordinates = textureCoordinates;
        this.normal = normal;
    }
    // Это работает только если полигон из 3 точек
    public float[] getArrayRepresentation() {
        float[] out = new float[this.vertices.length * 5];
        for (int i = 0; i < this.vertices.length; i++) {
            out[i * 5] = vertices[i].x;
            out[i * 5 + 1] = vertices[i].y;
            out[i * 5 + 2] = vertices[i].z;
            out[i * 5 + 3] = textureCoordinates[i].x;
            out[i * 5 + 4] = textureCoordinates[i].y;
        }
        return out;
    }
}