package com.seal.gl_engine.maths;

import static com.seal.gl_engine.utils.Utils.abs;

public class Line {
    private final Vec3 base, direction;


    public Line(Vec3 A, Vec3 B) {
        this.direction =Vec3.sub(B, A);
        this.base = new Vec3(A);

    }

    public Vec3 getDirectionVector() {
        return new Vec3(direction);
    }

    public Vec3 getBaseVector() {
        return new Vec3(base);
    }

    //inverse 2x2 matrix
    private float[] invertMat(float[] mat, float det) {
        return new float[]{mat[3]/det,  -mat[1]/det, -mat[2]/det, mat[0]/det};
    }

    //mat 2x2 * vec 2
    private float[] multiply_MV(float[] mat, float[] vec) {
        return new float[]{mat[0] * vec[0] + mat[1] * vec[1], mat[2] * vec[0] + mat[3] * vec[1]};
    }

    public Vec3 findCross(Line n) {
        Vec3 a, b, c, d;
        b = this.getBaseVector();
        a = this.getDirectionVector();
        d = n.getBaseVector();
        c = n.getDirectionVector();
        float[] mat = new float[]{a.x, -c.x, a.y, -c.y};
        float det = mat[0] * mat[3] - mat[1] * mat[2];
        if (abs(det) < 10e-9) {
            return null;
        }
        float[] mat_inv;
        mat_inv = invertMat(mat, det);
        float[] vector_right_part = new float[]{d.x - b.x, d.y - b.y};
        float[] result;
        result = multiply_MV(mat_inv, vector_right_part); //find k1 and k2
        if (result[0] < 0 || result[0] > 1 || result[1] < 0 || result[1] > 1) {
            return null;//out of bounds
        }
        if (abs(a.z + b.z * result[0] - (c.z + d.z * result[1])) > 10e9) {
            return null;// check z
        }
        return b.add(a.mul(result[0]));
    }
}
