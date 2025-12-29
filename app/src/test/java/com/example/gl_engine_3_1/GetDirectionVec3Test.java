package com.example.gl_engine_3_1;

import static com.seal.gl_engine.maths.Vec3.getDirection;
import static com.seal.gl_engine.utils.Utils.cos;
import static com.seal.gl_engine.utils.Utils.radians;
import static com.seal.gl_engine.utils.Utils.sin;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.seal.gl_engine.maths.Vec3;

import org.junit.Test;

public class GetDirectionVec3Test {
    @Test
    public void getDirection_isCorrect() {
        float[] arr = new float[360];
        float[] res = new float[360];
        for (int i = 0; i < 360; i++) {
            res[i] = get_vec_angle(i);
            arr[i] = i;
        }
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
}
