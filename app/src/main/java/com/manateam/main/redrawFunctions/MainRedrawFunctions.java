package com.manateam.main.redrawFunctions;

import static com.seal.gl_engine.utils.Utils.loadImage;

import com.seal.gl_engine.engine.main.images.PImage;

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
        image.ellipse(0,0,100,100);
        if(params.size()!=0){
            image.textSize(50);
           // image.text(params.get(0),200,100);
        }
        return image;
    }

    public static PImage redrawBox2(List<Object> params) {
        PImage image;
        image = loadImage("box.jpg");
        image.fill(255);
        image.ellipse(100, 100, 50, 50);
        return image;
    }

    public static PImage redrawFps(List<Object> param) {
        PImage image = new PImage(100, 100);
        image.background(150);
        image.textSize(20);
        image.fill(0);
        if (param.size() > 0) {
            image.text((String) param.get(0), 10, 10);
        }
        return image;
    }
}
