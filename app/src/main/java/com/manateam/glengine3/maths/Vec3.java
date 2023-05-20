package com.manateam.glengine3.maths;

import static com.manateam.glengine3.utils.Utils.programStartTime;
import static com.manateam.glengine3.utils.Utils.sq;
import static com.manateam.glengine3.utils.Utils.sqrt;

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

    public float[] getVector() {
        return new float[]{x, y, z};
    }

    public void normalize() {
        float k = 1 / length();
        x = x * k;
        y = y * k;
        z = z * k;
    }

    public float length() {
        return sqrt(sq(x) + sq(y) + sq(z));
    }

    public Vec3 add(Vec3 v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        return this;
    }

    public Vec3 minus(Vec3 v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        return this;
    }

    public Vec3 mult(float i) {
        this.x *= i;
        this.y *= i;
        this.z *= i;
        return this;
    }


}
