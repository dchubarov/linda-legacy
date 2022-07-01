
/*
 * TWOWLS.ORG PROPRIETARY/CONFIDENTIAL
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.twowls.lab.legacy.linde.producer;

/**
 *
 * @author da
 */
public interface Interpreter {

    /**
     * Invoked when production is started.
     * @param order Producer order.
     * @throws ProducerException if failed to start.
     */
    void start(int order) throws ProducerException;

    /**
     * Invoked when branch is finished.
     * @param depth Depth after end of branch.
     * @throws ProducerException if failed to ascend.
     */
    void ascend(int depth) throws ProducerException;

    /**
     * Invoked when producer is about to start a new branch.
     * @param depth Depth after new branch is started.
     * @throws ProducerException if failed to descend.
     */
    void descend(int depth) throws ProducerException;

    /**
     * Invoked when new input character should be interpreted.
     * @param c Character to interpret
     * @throws ProducerException if failed to interpret.
     */
    void interpret(char c) throws ProducerException;

}
