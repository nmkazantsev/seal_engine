package com.example.gl_engine_3_1;

import static com.seal.gl_engine.utils.Utils.degrees;

import static org.junit.Assert.assertArrayEquals;

import com.seal.gl_engine.utils.Utils;

import org.junit.Test;

public class GetDirectionTest {
    @Test
    public void testGetDirection() {

        float[] arr = new float[360];
        float[] res = new float[360];
        for (int i = 1; i < 360; i++) {
            res[i] = degrees(Utils.getDirection(1, 0, Utils.cos(Utils.radians(i)), Utils.sin(Utils.radians(i))));
            arr[i] = (i / 2.0f + 90) % 360;
        }

        assertArrayEquals(arr, res, 0.001f);

    }
}
