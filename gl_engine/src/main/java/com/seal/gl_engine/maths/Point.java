/*package com.seal.gl_engine.maths;

import com.seal.gl_engine.utils.Utils;

public class Vec3 {
    public float x, y, z;

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getDistanceToPoint(Vec3 p) {
        return Utils.sqrt(Utils.sq(p.x - this.x) + Utils.sq(p.y - this.y) + Utils.sq(this.z - p.z));
    }

    public static Vec3 normal(Vec3 A, Vec3 B, Vec3 C) {
        Vec3 a = new Vec3(A.x - B.x, A.y - B.y, A.z - B.z);
        Vec3 b = new Vec3(B.x - C.x, B.y - C.y, B.z - C.z);
        Vec3 v = new Vec3(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
        v.normalize();
        return v;
    }

    public Vec3 toVec3() {
        return new Vec3(x, y, z);
    }
}
*/