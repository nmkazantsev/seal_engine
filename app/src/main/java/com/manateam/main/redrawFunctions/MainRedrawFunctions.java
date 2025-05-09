package com.manateam.main.redrawFunctions;

import static com.seal.gl_engine.utils.Utils.loadImage;

import com.seal.gl_engine.engine.main.images.PImage;
import com.seal.gl_engine.maths.Section;
import com.seal.gl_engine.maths.PVector;

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
        Section section = new Section(new PVector(0,0, 0), new PVector(150, 150, 50));
        Section section2 = new Section(new PVector(140, 120, 0), new PVector(0, 110, 0));
        image.line(section);
        image.line(section2);
        PVector p = section.findCross(section2);
        if(p!=null) {
            image.ellipse(p.x, p.y, 5, 5);
        }
        return image;
    }
}
