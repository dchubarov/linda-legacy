
/*
 * TWOWLS.ORG PROPRIETARY/CONFIDENTIAL
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.twowls.lab.legacy.linde.render;

import org.junit.jupiter.api.Test;
import org.twowls.lab.legacy.linde.producer.Producer;
import org.twowls.lab.legacy.linde.producer.ProducerException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author da
 */
public class TurtleCacheTest {

    @Test
    public void cacheFractal() throws ProducerException {
        PlanarTurtleRenderer renderer = new PlanarTurtleRenderer(0.0, 60.0, 1.0);
        Producer p = new Producer();
        p.addInterpreter(renderer);
        p.addRule('A', "B-A-B", 'F');
        p.addRule('B', "A+B+A", 'F');
        p.setAxiom("A");

        p.produce(10);

        renderer.render(new Surface() {
            @Override
            public void draw(double srcX, double srcY, double dstX, double dstY) {
                // do nothing
            }

            @Override
            public void setExtent(double north, double east, double south, double west) {
                // do nothing
            }
        });
    }

    @Test
    public void restoreDouble() throws ProducerException {
        PlanarTurtleRenderer.Cache cache = new PlanarTurtleRenderer.Cache(256, 256);
        double d1 = Math.random();
        double d2 = Math.random();
        cache.writeMove(d1, d2, false);
        cache.flush();

        assertEquals(PlanarTurtleRenderer.Cache.MOVE_START, cache.readByte());
        assertEquals(d1, cache.readDouble(), 0.0);
        assertEquals(d2, cache.readDouble(), 0.0);
    }
}
