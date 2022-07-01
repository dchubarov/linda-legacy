
/*
 * TWOWLS.ORG PROPRIETARY/CONFIDENTIAL
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.twowls.lab.legacy.linde.producer;

import org.twowls.lab.legacy.linde.producer.Interpreter;
import org.twowls.lab.legacy.linde.producer.ProducerException;

/**
 *
 * @author tim
 */
public class CountingInterpreter implements Interpreter {

    private int count = 0;

    @Override
    public void start(int order) throws ProducerException {
        count = 0;
    }

    @Override
    public void ascend(int depth) throws ProducerException {
        // do nothing
    }

    @Override
    public void descend(int depth) throws ProducerException {
        // do nothing
    }

    @Override
    public void interpret(char c) throws ProducerException {
        count++;
    }

    public int getCount() {
        return count;
    }
}
