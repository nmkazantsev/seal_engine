package com.seal.gl_engine.maths;

import static java.lang.Math.acos;
import static java.lang.Math.tan;

import com.seal.gl_engine.utils.Utils;

public class Vec3 {
    public float x, y, z;

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

    public Vec3(Vec3 v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
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

    public static Vec3 normalize(Vec3 v) {
        Vec3 b = new Vec3(v);
        b.normalize();
        return b;
    }

    public float length() {
        return Utils.sqrt(Utils.sq(x) + Utils.sq(y) + Utils.sq(z));
    }

    public Vec3 add(Vec3 v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        return this;
    }

    public static Vec3 add(Vec3 v, Vec3 u) {
        return new Vec3(v.x + u.x, v.y + u.y, v.z + u.z);
    }

    public Vec3 sub(Vec3 v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        return this;
    }

    public static Vec3 sub(Vec3 v, Vec3 u) {
        return new Vec3(v.x - u.x, v.y - u.y, v.z - u.z);
    }

    public Vec3 mul(float i) {
        this.x *= i;
        this.y *= i;
        this.z *= i;
        return this;
    }

    public static Vec3 mul(Vec3 v, float a) {
        return new Vec3(v.x * a, v.y * a, v.z * a);
    }
    // cross product of two vectors
    public static Vec3 cross(Vec3 v, Vec3 u) {
        return new Vec3(v.y * u.z - v.z * u.y, v.z * u.x - v.x * u.z, v.x * u.y - v.y * u.x);
    }


    public Vec3 div(float i) {
        this.x /= i;
        this.y /= i;
        this.z /= i;
        return this;
    }

    public static Vec3 div(Vec3 v, float a) {
        return new Vec3(v.x / a, v.y / a, v.z / a);
    }

    public static float getAngle(Vec3 v, Vec3 u) {
        return (float) acos((v.x * u.x + v.y * u.y + v.z * u.z) / v.length() / u.length());
    }

    public static Vec3 rotateToVec(Vec3 v, Vec3 u, float alpha) {
        Vec3 n = cross(v, u);
        Vec3 c1 = cross(v, n);
        Vec3 c = normalize(c1).mul(v.length());
        Vec3 b = (new Vec3(v)).add(c.mul((float) tan(alpha)));
        b.normalize();
        return b.mul(v.length());
    }
}
