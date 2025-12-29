package com.example.gl_engine_3_1;

import static com.seal.gl_engine.utils.Utils.cos;
import static com.seal.gl_engine.utils.Utils.degrees;
import static com.seal.gl_engine.utils.Utils.radians;
import static com.seal.gl_engine.utils.Utils.sin;
import static com.seal.gl_engine.utils.Utils.sq;
import static com.seal.gl_engine.utils.Utils.sqrt;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.seal.gl_engine.maths.Vec3;

import org.junit.Test;

public class GetDirectionTest {
    @Test
    public void getDirection_isCorrect() {
        float[] arr = new float[360];
        float[] res = new float[360];
        for (int i = 0; i < 360; i++) {
            res[i] = get_vec_angle(i);
            arr[i] = i;
        }
        int i = 0;
        assertArrayEquals(arr, res, 0.1f);
    }

    @Test
    public void test_ang_45() {
        check_vec_angle(45);
    }

    @Test
    public void test_ang_90() {
        check_vec_angle(90);
    }

    @Test
    public void test_ang_135() {
        check_vec_angle(135);
    }

    @Test
    public void test_ang_180() {
        check_vec_angle(180);
    }

    @Test
    public void test_ang_225() {
        check_vec_angle(225);
    }

    @Test
    public void test_ang_270() {
        check_vec_angle(270);
    }

    @Test
    public void test_rotated_A_and_B() {
        for (int i = 0; i < 360; i += 36) {
            for (int g = 0; g < 360; g += 36) {
                Vec3 a = new Vec3(1 * cos(radians(i)), 1 * sin(radians(i)), 0);
                Vec3 b = new Vec3(1 * cos(radians(g)), 1 * sin(radians(g)), 0);
                float res = getDirection(a, b);
                float expect = g - i;
                System.out.println(expect + " " + i + " " + g);
                if (expect < 0) {
                    expect = 360 + expect;
                }
                assertEquals(expect % 360, res, 0.1);
            }
        }
    }

    public void check_vec_angle(float ang) {
        Vec3 a = new Vec3(1, 0, 0);
        Vec3 b = new Vec3(1 * cos(radians(ang)), 1 * sin(radians(ang)), 0);
        assertEquals(ang, getDirection(a, b), 0.1);
    }

    public float get_vec_angle(float ang) {
        Vec3 a = new Vec3(1, 0, 0);
        Vec3 b = new Vec3(1 * cos(radians(ang)), 1 * sin(radians(ang)), 0);
        return getDirection(a, b);
    }


    private float getDirection(Vec3 a, Vec3 b) {
        /*
                     b
                   *
                 *  ) alpha
        -------*------> a

         */
        Vec3 an = a.normalize();
        Vec3 bn = b.normalize();
        float projection = an.dot(bn);
        Vec3 an_ort = an.cross(new Vec3(0, 0, 1)).mul(-1);
        float ort = bn.dot(an_ort);
        float alpha = 0;
        if (projection > 0) {
            if (projection > 0.5) {
                alpha = degrees((float) Math.atan(ort / projection));
                if (ort <= 0) {
                    alpha = 360 + alpha;
                }
            } else {
                float d = sqrt(sq(projection) + sq(ort));
                alpha = degrees((float) Math.acos(projection / d));
                if (ort <= 0) {
                    alpha = 360 - alpha;
                }
            }
        } else if (projection < 0) {
            if (projection < -0.5) {
                alpha = 180 + degrees((float) Math.atan(ort / projection));
            } else {
                float d = sqrt(sq(projection) + sq(ort));
                alpha = degrees((float) Math.acos(projection / d));
                if (ort <= 0) {
                    alpha = 360 - alpha;
                }
            }
        }
        if (alpha >= 360) {
            alpha -= 360;
        }
        return alpha;
    }
}
