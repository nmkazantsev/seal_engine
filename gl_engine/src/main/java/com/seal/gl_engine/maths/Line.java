package com.seal.gl_engine.maths;

public class Line {
    private final float a, b, c, p1, p2, p3;

    public Line(Vec3 A, Vec3 B) {
        Vec3 v = new Vec3(A.x - B.x, A.y - B.y, A.z - B.z);
        a = -A.x;
        b = -A.y;
        c = -A.z;
        float[] f = v.getArray();
        p1 = f[0];
        p2 = f[1];
        p3 = f[2];
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
        return new Vec3(a, b, c);
    }

    public Vec3 getBaseVector() {
        return new Vec3(p1, p2, p3);
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
        //mat[0] = a.x;
        //mat[2] = c.x;
        float detx = a.x * c.x - mat[1] * mat[3];
        float k1 = detx / det;
        /*mat[0] = d1.x;
        mat[2] = d1.y;
        mat[1] = a.y;
        mat[3] = c.y;
        float dety = mat[0] * mat[2] - mat[1] * mat[3];
        float k2 = dety/det;
        if(k1*)
         */
        Vec3 point = a.mul(k1).add(b);
        if (b.z * k1 + a.z == point.z) {
            return point;
        } else {
            return null;
        }
    }
}
