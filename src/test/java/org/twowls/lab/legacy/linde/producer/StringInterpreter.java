
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
public class StringInterpreter implements Interpreter {

    private StringBuffer sb = new StringBuffer();

    @Override
    public void start(int order) throws ProducerException {
        sb.setLength(0);
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
        sb.append(c);
    }

    public String getResult() {
        return sb.toString();
    }
}
