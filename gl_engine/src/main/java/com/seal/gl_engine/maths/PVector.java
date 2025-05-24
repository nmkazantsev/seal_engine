package com.seal.gl_engine.maths;

import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.resetTranslateMatrix;
import static java.lang.Math.acos;
import static java.lang.Math.tan;

import android.opengl.Matrix;

import com.seal.gl_engine.utils.Utils;

import java.io.Serializable;

public class PVector implements Serializable {
    private static final long serialVersionUID = 1L;
    public float x, y, z;

    public PVector(Vec3 v) {
        x = v.x;
        y = v.y;
        z = v.z;
    }

    public PVector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PVector() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    // Creates vector with values taken from give array. Reads 3 values, starting from i index.
    public PVector(float[] arr, int i) {
        x = arr[i];
        y = arr[i + 1];
        z = arr[i + 2];
    }

    public PVector(PVector v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public PVector(float v) {
        this.x = v;
        this.y = v;
        this.z = v;
    }

    public PVector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float[] getArray() {
        return new float[]{x, y, z};
    }

    public void normalize() {
        float k = 1 / length();
        x = x * k;
        y = y * k;
        z = z * k;
    }

    public static PVector normalize(PVector v) {
        PVector b = new PVector(v);
        b.normalize();
        return b;
    }

    public float length() {
        return Utils.sqrt(Utils.sq(x) + Utils.sq(y) + Utils.sq(z));
    }

    public static float length(PVector v) {
        return v.length();
    }

    public PVector add(PVector v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        return this;
    }

    public static PVector add(PVector v, PVector u) {
        return new PVector(v.x + u.x, v.y + u.y, v.z + u.z);
    }

    public PVector sub(PVector v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        return this;
    }

    public static PVector sub(PVector v, PVector u) {
        return new PVector(v.x - u.x, v.y - u.y, v.z - u.z);
    }

    public PVector mul(float i) {
        this.x *= i;
        this.y *= i;
        this.z *= i;
        return this;
    }

    public static PVector mul(PVector v, float a) {
        return new PVector(v.x * a, v.y * a, v.z * a);
    }

    // cross product of two vectors
    public static PVector cross(PVector v, PVector u) {
        return new PVector(v.y * u.z - v.z * u.y, v.z * u.x - v.x * u.z, v.x * u.y - v.y * u.x);
    }

    public void cross(PVector u) {
        this.x = this.y * u.z - this.z * u.y;
        this.y = this.z * u.x - this.x * u.z;
        this.z = this.x * u.y - this.y * u.x;
    }

    public PVector div(float i) {
        this.x /= i;
        this.y /= i;
        this.z /= i;
        return this;
    }

    public static PVector div(PVector v, float a) {
        return new PVector(v.x / a, v.y / a, v.z / a);
    }

    public static float getAngle(PVector v, PVector u) {
        return (float) acos((v.x * u.x + v.y * u.y + v.z * u.z) / v.length() / u.length());
    }

    public static PVector rotateToVec(PVector v, PVector u, float alpha) {
        PVector n = cross(v, u);
        PVector c1 = cross(v, n);
        PVector c = normalize(c1).mul(v.length());
        PVector b = (new PVector(v)).add(c.mul((float) tan(alpha)));
        b.normalize();
        return b.mul(v.length());
    }

    /**
     * rotates vector around axis for a specified angle
     *
     * @param vec  source vector
     * @param axis axis, around which to rotate
     * @param a    angle in degrees
     * @return new Vec3, equal to rotated vector
     */
    public static PVector rotateVec3(PVector vec, PVector axis, float a) {
        //create empty translate matrix
        float[] matrix;
        matrix = resetTranslateMatrix(new float[16]);
        Matrix.rotateM(matrix, 0, a, axis.x, axis.y, axis.z);
        float[] resultVec = new float[4];
        Matrix.multiplyMV(resultVec, 0, matrix, 0, new float[]{vec.x, vec.y, vec.z, 0}, 0);
        return new PVector(resultVec[0], resultVec[1], resultVec[2]);
    }

    /**
     * rotates vector around axis for a specified angle
     *
     * @param axis axis, around which to rotate
     * @param a    angle in degrees
     */
    public void rotateVec3(PVector axis, float a) {
        //create empty translate matrix
        float[] matrix;
        matrix = resetTranslateMatrix(new float[16]);
        Matrix.rotateM(matrix, 0, a, axis.x, axis.y, axis.z);
        float[] resultVec = new float[4];
        Matrix.multiplyMV(resultVec, 0, matrix, 0, new float[]{this.x, this.y, this.z, 0}, 0);
        //return new Vec3(resultVec[0], resultVec[1], resultVec[2]);
        this.x = resultVec[0];
        this.y = resultVec[1];
        this.z = resultVec[2];
    }

    public static float dot(PVector a, PVector b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }
}
