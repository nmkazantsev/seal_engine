package com.seal.gl_engine.maths;

import static com.seal.gl_engine.utils.Utils.abs;

public class Line {
    private final float a, b, c, p1, p2, p3;


    public Line(Vec3 A, Vec3 B) {
        Vec3 v = new Vec3(A.x - B.x, A.y - B.y, A.z - B.z);
        a = A.x;
        b = A.y;
        c = A.z;
        // float[] f = v.getArray();
        p1 = -v.x;
        p2 = -v.y;
        p3 = -v.z;
    }

    Vec3 crossWithPlane(Plane p) {
        //todo: for p1-p3 = 0
        //todo: test

        float[] plane = p.getEquation();
        float A = plane[0];
        float B = plane[1];
        float C = plane[2];
        float D = plane[3];
        float x = (B * p2 / p1 * a - B * b + C * p3 / p1 * a - C * c) / (A + B * p2 / p1 + C * p3 / p1);
        float y = p2 * (x - a) / p1 + b;
        float z = p3 * (x - a) / p1 + c;
        return new Vec3(x, y, z);
    }

    public Vec3 getDirectionVector() {
        return new Vec3(p1, p2, p3);
    }

    public Vec3 getBaseVector() {
        return new Vec3(a, b, c);
    }

    //inverse 2x2 matrix
    private float[] invertMat(float[] mat, float det) {
        return new float[]{mat[3] / det, -mat[1] / det, -mat[2] / det, mat[0] / det};
    }

    //mat 2x2 * vec 2
    private float[] multiply_MV(float[] mat, float[] vec) {
        return new float[]{mat[0] * vec[0] + mat[1] * vec[1], mat[2] * vec[0] + mat[3] * vec[1]};
    }

    public Vec3 findCross(Line n) {
        Vec3 a, b, c, d;
        a = this.getBaseVector();
        b = this.getDirectionVector();//Vec3.add(a, this.getDirectionVector());
        c = n.getBaseVector();
        d = n.getDirectionVector();//Vec3.add(c, n.getDirectionVector());
        Vec3 d1 = Vec3.sub(b, a);
        Vec3 d2 = Vec3.sub(d, c);
        float[] mat = new float[]{d1.x, d2.x, d1.y, d2.y};
        float det = mat[0] * mat[2] - mat[1] * mat[3];
        if (det < 10e-9) {
            return null;
        }
        float[] mat_inv;
        mat_inv = invertMat(mat, det);
        float[] vector_awns = new float[]{c.x + d.x - (a.x + b.x), c.y + d.y - (a.y + b.y)};
        float[] result;
        result = multiply_MV(mat_inv, vector_awns); //find k1 and k2
        if (result[0] < 0 || result[0] > 1 || result[1] < 0 || result[1] > 1) {
            return null;//out of bounds
        }
        if (abs(a.z + b.z * result[0] - (c.z + d.z * result[1])) > 109 - 9) {
            return null;// check z
        }
        return a.add(b.mul(result[1]));
    }
}
