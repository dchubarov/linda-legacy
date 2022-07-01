
/*
 * TWOWLS.ORG PROPRIETARY/CONFIDENTIAL
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.twowls.lab.legacy.linde.producer;

import org.twowls.lab.legacy.linde.producer.Interpreter;
import org.twowls.lab.legacy.linde.producer.ProducerException;

import java.io.PrintStream;

/**
 *
 * @author tim
 */
public class PrintingInterpreter implements Interpreter {

    private PrintStream out = null;

    public PrintingInterpreter() {
        this(System.out);
    }

    public PrintingInterpreter(PrintStream out) {
        this.out = out;
    }


    @Override
    public void start(int order) throws ProducerException {
        // do nothing
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
        out.print(c);
    }
}
