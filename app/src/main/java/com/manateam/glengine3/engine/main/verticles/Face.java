package com.manateam.glengine3.engine.main.verticles;

import com.manateam.glengine3.maths.Point;

public class Face {
    public final Point[] vertices;
    public final Point[] textureCoordinates;
    public final Point normal;

    public Face(Point[] vertices, Point[] textureCoordinates, Point normal) {
        this.vertices = vertices;
        this.textureCoordinates = textureCoordinates;
        this.normal = normal;
    }

    // Это работает только если полигон из 3 точек
    public float[] getArrayRepresentation() {
        float[] out = new float[this.vertices.length * 8];
        for (int i = 0; i < this.vertices.length; i++) {
            out[i * 8] = vertices[i].x;
            out[i * 8 + 1] = vertices[i].y;
            out[i * 8 + 2] = vertices[i].z;
            out[i * 8 + 3] = textureCoordinates[i].x;
            out[i * 8 + 4] = textureCoordinates[i].y;
            out[i * 8 + 5] = normal.x;
            out[i * 8 + 6] = normal.y;
            out[i * 8 + 7] = normal.z;
        }
        return out;
    }
    public int verticesNumber(){
        return 24;
    }
}