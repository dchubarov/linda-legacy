
/*
 * TWOWLS.ORG PROPRIETARY/CONFIDENTIAL
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.twowls.lab.legacy.linde.producer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author tim
 */
public class ProducerTest {

    @Test
    public void algae() throws ProducerException {
        StringInterpreter interp = new StringInterpreter();
        Producer p = new Producer();
        p.addInterpreter(interp);
        p.addRule('A', "AB");
        p.addRule('B', "A");
        p.setAxiom("A");

        p.produce(0);
        assertEquals("A", interp.getResult());
        p.produce(1);
        assertEquals("AB", interp.getResult());
        p.produce(2);
        assertEquals("ABA", interp.getResult());
        p.produce(3);
        assertEquals("ABAAB", interp.getResult());
        p.produce(4);
        assertEquals("ABAABABA", interp.getResult());
        p.produce(5);
        assertEquals("ABAABABAABAAB", interp.getResult());
        p.produce(6);
        assertEquals("ABAABABAABAABABAABABA", interp.getResult());
        p.produce(7);
        assertEquals("ABAABABAABAABABAABABAABAABABAABAAB", interp.getResult());
    }

    @Test
    public void fibonacci() throws ProducerException {
        CountingInterpreter interp = new CountingInterpreter();
        Producer p = new Producer();
        p.addInterpreter(interp);
        p.addRule('A', "B");
        p.addRule('B', "AB");
        p.setAxiom("A");

        p.produce(0);
        assertEquals(1, interp.getCount());
        p.produce(1);
        assertEquals(1, interp.getCount());
        p.produce(2);
        assertEquals(2, interp.getCount());
        p.produce(3);
        assertEquals(3, interp.getCount());
        p.produce(4);
        assertEquals(5, interp.getCount());
        p.produce(5);
        assertEquals(8, interp.getCount());
        p.produce(6);
        assertEquals(13, interp.getCount());
        p.produce(7);
        assertEquals(21, interp.getCount());
        p.produce(8);
        assertEquals(34, interp.getCount());
        p.produce(9);
        assertEquals(55, interp.getCount());
        p.produce(10);
        assertEquals(89, interp.getCount());
    }
}
