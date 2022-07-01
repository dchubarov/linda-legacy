
/*
 * TWOWLS.ORG PROPRIETARY/CONFIDENTIAL
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.twowls.lab.legacy.linde.render;

/**
 *
 * @author da
 */
public class StochasticAngleDeviator implements AngleDeviator {

    private double maximumCW;
    private double maximumCCW;

    public StochasticAngleDeviator(double maximumCW, double maximumCCW) {
        this.maximumCW = maximumCW;
        this.maximumCCW = maximumCCW;
    }

    @Override
	public double deviate(double value, int turn) {
        if (turn == CW) {
            if (maximumCW != .0) {
                value += Math.random() * Math.abs(maximumCW);
            }
        }
        else if (turn == CCW) {
            if (maximumCCW != .0) {
                value -= Math.random() * Math.abs(maximumCCW);
            }
        }
        return value;
    }
}
