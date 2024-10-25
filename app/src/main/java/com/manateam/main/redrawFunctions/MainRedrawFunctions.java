package com.manateam.main.redrawFunctions;

import static com.seal.gl_engine.utils.Utils.loadImage;

import com.seal.gl_engine.engine.main.images.PImage;
import com.seal.gl_engine.maths.Line;
import com.seal.gl_engine.maths.Vec3;

import java.util.List;

public class MainRedrawFunctions {
    public static PImage redrawBox(List<Object> params) {
        PImage image;
        image = loadImage("box.jpg");
        return image;
    }

    public static PImage redrawPolig(List<Object> params) {
        PImage image;
        image = loadImage("box.jpg");
        image.ellipse(0, 0, 100, 100);
        if (!params.isEmpty()) {
            image.textSize(50);
            // image.text(params.get(0),200,100);
        }
        return image;
    }

    public static PImage redrawBox2(List<Object> params) {
        PImage image;
        image = loadImage("box.jpg");
        image.fill(255);
        //image.ellipse(100, 100, 50, 50);
        image.strokeWeight(5);
        image.stroke(255);
        Line line = new Line(new Vec3(0,0, 0), new Vec3(150, 50, 50));
        Line line2 = new Line(new Vec3(140, 20, 0), new Vec3(0, 110, 0));
        image.line(line);
        image.line(line2);
        Vec3 p = line.findCross(line2);
        if(p!=null) {
            image.ellipse(p.x, p.y, 10, 10);
        }
        return image;
    }
}
