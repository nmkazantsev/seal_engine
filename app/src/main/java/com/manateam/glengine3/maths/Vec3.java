package com.manateam.glengine3.maths;

import static com.manateam.glengine3.utils.Utils.sq;
import static com.manateam.glengine3.utils.Utils.sqrt;

public class Vec3 {
    public float x,y,z;
    public Vec3(float x,float y,float z){
        this.x=x;
        this.y=y;
        this.z=z;
    }
    public float[] getVector(){
        return new float[] {x,y,z};
    }
    public void normalize(){
        float k = 1/length();
        x=x*k;
        y=y*k;
        z=z*k;
    }
    public float length(){
        return sqrt(sq(x)+sq(y)+sq(z));
    }


}
