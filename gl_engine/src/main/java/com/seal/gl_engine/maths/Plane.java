package com.seal.gl_engine.maths;

import java.io.Serializable;

public class Plane implements Serializable {
    private static final long serialVersionUID = 1L;
    private final float a,b,c,d;//for equastion
    public Plane(PVector A, PVector B, PVector C){
        float [] temp = equationPlane(A.x, A.y, A.z, B.x, B.y, B.z, C.x, C.y, C.z);
        a=temp[0];
        b=temp[1];
        c=temp[2];
        d=temp[3];
    }
    private float[] equationPlane(float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
        float a1 = x2 - x1;
        float b1 = y2 - y1;
        float c1 = z2 - z1;
        float a2 = x3 - x1;
        float b2 = y3 - y1;
        float c2 = z3 - z1;
        float a = b1 * c2 - b2 * c1;
        float b = a2 * c1 - a1 * c2;
        float c = a1 * b2 - b1 * a2;
        float d = (- a * x1 - b * y1 - c * z1);
        return new float[]{a,b,c,d};
    }
    public boolean pointInPlane(PVector A){
        return a*A.x+b*A.y+c*A.z+d==0;
    }
    public float[] getEquation(){
        return new float[]{a,b,c,d};
    }
    public PVector getNormal(){
        return new PVector(a,b,c);
    }
}
