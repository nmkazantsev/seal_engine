package com.manateam.glengine3.engine.main.verticles;

import com.manateam.glengine3.maths.Point;
import com.manateam.glengine3.maths.Vec3;

public class Face {
    public final Point[] vertices;
    public final Point[] textureCoordinates;
    public final Point normal;
    private Vec3 tangent, bitangent;//tangent, bitangent
    private final int SEGMENT_LENGTH = 14, SEGMENT_LENGTH_NO_TS = 14 - 6;

    public Face(Point[] vertices, Point[] textureCoordinates, Point normal) {
        this.vertices = vertices;
        this.textureCoordinates = textureCoordinates;
        this.normal = normal;
        //calculate tangent space (https://learnopengl.com/Advanced-Lighting/Normal-Mapping)
        Vec3 edge1 = (vertices[1].toVec3().minus(vertices[0].toVec3()));
        Vec3 edge2 = (vertices[2].toVec3().minus(vertices[0].toVec3()));
        Vec3 deltaUV1 = (textureCoordinates[0].toVec3().minus(textureCoordinates[1].toVec3()));
        Vec3 deltaUV2 = (textureCoordinates[2].toVec3().minus(textureCoordinates[1].toVec3()));
        float f = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV2.x * deltaUV1.y);

        tangent = new Vec3();
        bitangent = new Vec3();
        tangent.x = f * (deltaUV2.y * edge1.x - deltaUV1.y * edge2.x);
        tangent.y = f * (deltaUV2.y * edge1.y - deltaUV1.y * edge2.y);
        tangent.z = f * (deltaUV2.y * edge1.z - deltaUV1.y * edge2.z);

        bitangent.x = f * (-deltaUV2.x * edge1.x + deltaUV1.x * edge2.x);
        bitangent.y = f * (-deltaUV2.x * edge1.y + deltaUV1.x * edge2.y);
        bitangent.z = f * (-deltaUV2.x * edge1.z + deltaUV1.x * edge2.z);
    }

    // Это работает только если полигон из 3 точек
    public float[] getArrayRepresentationTangentSpace() {
        float[] out = new float[this.vertices.length * SEGMENT_LENGTH];
        for (int i = 0; i < this.vertices.length; i++) {
            out[i * SEGMENT_LENGTH] = vertices[i].x;
            out[i * SEGMENT_LENGTH + 1] = vertices[i].y;
            out[i * SEGMENT_LENGTH + 2] = vertices[i].z;
            out[i * SEGMENT_LENGTH + 3] = textureCoordinates[i].x;
            out[i * SEGMENT_LENGTH + 4] = textureCoordinates[i].y;
            out[i * SEGMENT_LENGTH + 5] = normal.x;
            out[i * SEGMENT_LENGTH + 6] = normal.y;
            out[i * SEGMENT_LENGTH + 7] = normal.z;
            out[i * SEGMENT_LENGTH + 8] = tangent.x;
            out[i * SEGMENT_LENGTH + 9] = tangent.y;
            out[i * SEGMENT_LENGTH + 10] = tangent.z;
            out[i * SEGMENT_LENGTH + 11] = bitangent.x;
            out[i * SEGMENT_LENGTH + 12] = bitangent.y;
            out[i * SEGMENT_LENGTH + 13] = bitangent.z;
        }
        return out;
    }

    // Это работает только если полигон из 3 точек
    public float[] getArrayRepresentation() {
        float[] out = new float[this.vertices.length * SEGMENT_LENGTH_NO_TS];
        for (int i = 0; i < this.vertices.length; i++) {
            out[i * SEGMENT_LENGTH_NO_TS] = vertices[i].x;
            out[i * SEGMENT_LENGTH_NO_TS + 1] = vertices[i].y;
            out[i * SEGMENT_LENGTH_NO_TS + 2] = vertices[i].z;
            out[i * SEGMENT_LENGTH_NO_TS + 3] = textureCoordinates[i].x;
            out[i * SEGMENT_LENGTH_NO_TS + 4] = textureCoordinates[i].y;
            out[i * SEGMENT_LENGTH_NO_TS + 5] = normal.x;
            out[i * SEGMENT_LENGTH_NO_TS + 6] = normal.y;
            out[i * SEGMENT_LENGTH_NO_TS + 7] = normal.z;
        }
        return out;
    }

    public int verticesNumberTangentSpace() {
        return 3 * SEGMENT_LENGTH;
    }

    public int verticesNumber() {
        return 3 * SEGMENT_LENGTH_NO_TS;//6 for tangent space
    }
}