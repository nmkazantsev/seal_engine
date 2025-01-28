package com.seal.gl_engine.maths;

import static com.seal.gl_engine.engine.config.MainConfigurationFunctions.resetTranslateMatrix;
import static java.lang.Math.acos;

import android.opengl.Matrix;

import com.seal.gl_engine.utils.Utils;

public class Vec3 {
    public float x, y, z;

    public Vec3(Vec3 v) {
        x = v.x;
        y = v.y;
        z = v.z;
    }

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    // Creates vector with values taken from give array. Reads 3 values, starting from i index.
    public Vec3(float[] arr, int i) {
        x = arr[i];
        y = arr[i + 1];
        z = arr[i + 2];
    }

    public Vec3(com.seal.gl_engine.maths.PVector v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public Vec3(float v) {
        this.x = v;
        this.y = v;
        this.z = v;
    }

    public Vec3(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float[] getArray() {
        return new float[]{x, y, z};
    }

    public Vec3 normalize() {
        float k = 1 / length();
        return new Vec3(this.x * k, this.y * k, this.z * k);
    }

    public float length() {
        return Utils.sqrt(Utils.sq(x) + Utils.sq(y) + Utils.sq(z));
    }

    public Vec3 add(Vec3 v) {
        return new Vec3(
                this.x + v.x,
                this.y + v.y,
                this.z + v.z);

    }

    public Vec3 sub(Vec3 v) {
        return new Vec3(
                this.x - v.x,
                this.y - v.y,
                this.z - v.z);

    }

    public Vec3 mul(float i) {
        return new Vec3(
                this.x * i,
                this.y * i,
                this.z * i);
    }


    // cross product of two vectors
    public Vec3 cross(Vec3 a) {
        return new Vec3(this.y * a.z - this.z * a.y, this.z * a.x - this.x * a.z, this.x * a.y - this.y * a.x);
    }

    public Vec3 div(float i) {
        return new Vec3(
                this.x / i,
                this.y / i,
                this.z / i);
    }


    public static float getAngle(Vec3 v, Vec3 u) {
        return (float) acos((v.x * u.x + v.y * u.y + v.z * u.z) / v.length() / u.length());
    }

    /**
     * rotates vector around axis for a specified angle
     *
     * @param axis axis, around which to rotate
     * @param a    angle in degrees
     */
    public Vec3 rotateVec3(Vec3 axis, float a) {
        //create empty translate matrix
        float[] matrix;
        matrix = resetTranslateMatrix(new float[16]);
        Matrix.rotateM(matrix, 0, a, axis.x, axis.y, axis.z);
        float[] resultVec = new float[4];
        Matrix.multiplyMV(resultVec, 0, matrix, 0, new float[]{this.x, this.y, this.z, 0}, 0);
        return new Vec3(
                resultVec[0],
                resultVec[1],
                resultVec[2]);
    }

    public float dot(Vec3 b) {
        return x * b.x + y * b.y + z * b.z;
    }

}
