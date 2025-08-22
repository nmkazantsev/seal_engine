package com.seal.gl_engine.engine.main.vertices;

import com.seal.gl_engine.GamePageClass;
import com.seal.gl_engine.engine.main.images.PImage;
import com.seal.gl_engine.maths.PVector;
import com.seal.gl_engine.utils.Utils;

import java.util.List;
import java.util.function.Function;

public class SimplePolygon extends Polygon {
    public SimplePolygon(Function<List<Object>, PImage> redrawFunction, boolean saveMemory, int paramSize, GamePageClass page) {
        super(redrawFunction, saveMemory, paramSize, page);
    }

    public SimplePolygon(Function<List<Object>, PImage> redrawFunction, boolean saveMemory, int paramSize, GamePageClass page, boolean mipMap) {
        super(redrawFunction, saveMemory, paramSize, page, mipMap);
    }

    public void prepareAndDraw(float x, float y, float b, float z) {
        PVector A = new PVector(x, y, z);
        PVector B = new PVector(x + b, y, z);
        PVector C = new PVector(x, y + b, z);
        super.prepareAndDraw(A, B, C);
    }

    public void prepareAndDraw(float rot, float x, float y, float a, float b, float z) {
        float[][] ver = glrectRotated(rot, x, y, a, b, z);
        PVector A = new PVector(ver[0][0], ver[0][1], z);
        PVector B = new PVector(ver[1][0], ver[1][1], z);
        PVector C = new PVector(ver[2][0], ver[2][1], z);
        super.prepareAndDraw(A, B, C);
    }

    public void prepareAndDraw(float x, float y, float a, float b, float z) {
        float[][] ver = {
                {x, y}, {x + a, y}, {x, y + b}, {x + a, y + b} //where vectries are now
        };
        PVector A = new PVector(ver[0][0], ver[0][1], z);
        PVector B = new PVector(ver[1][0], ver[1][1], z);
        PVector C = new PVector(ver[2][0], ver[2][1], z);
        super.prepareAndDraw(A, B, C);
    }

    //r in radians!
    private float[][] glrectRotated(float r, float x, float y, float a, float b, float z) {
        float[][] ver = {
                {x, y}, {x + a, y}, {x, y + b}, {x + a, y + b} //where vectries are now
        };
        x += a / 2;//x,y  - теперь центр прямоугльника
        y += b / 2;
        for (int i = 0; i < ver.length; i++) {
            float d = Utils.getDirection(x, y, ver[i][0], ver[i][1]) + r;//rotate them
            float dist = Utils.sqrt(Utils.sq(x - ver[i][0]) + Utils.sq(y - ver[i][1]));
            ver[i][0] = x + dist * Utils.cos(d);
            ver[i][1] = y + dist * Utils.sin(d);
        }
        return ver;
    }
}
