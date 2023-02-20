package com.manateam.glengine3.maths;

import com.manateam.glengine3.utils.Utils;

public class Point {
    public float x,y,z;
    public Point(float x,float y, float z){
    this.x=x;
    this.y=y;
    this.z=z;
    }
    public float getDistanceToPoint(Point p){
        return Utils.sqrt(Utils.sq(p.x-this.x)+Utils.sq(p.y-this.y)+ Utils.sq(this.z-p.z));
    }
    public static Vec3 normal(Point A, Point B, Point C) {
        Vec3 a= new Vec3(A.x-B.x,A.y-B.y,A.z-B.z);
        Vec3 b = new Vec3(B.x-C.x,B.y-C.y,B.z-C.z);
        Vec3 v=new Vec3(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x);
        v.normalize();
        return v;
    }
}
