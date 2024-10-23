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

    Vec3 crossWithPlane(Plane p){
        //todo: for p1-p3 = 0
        //todo: test

        float [] plane = p.getEquation();
        float A=plane[0];
        float B=plane[1];
        float C=plane[2];
        float D=plane[3];
        float x=(B*p2/p1*a-B*b+C*p3/p1*a-C*c)/(A+B*p2/p1+C*p3/p1);
        float y=p2*(x-a)/p1+b;
        float z=p3*(x-a)/p1+c;
        return new Vec3(x,y,z);
    }
}
