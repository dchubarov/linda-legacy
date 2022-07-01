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
public class ConstantAngleDeviator implements AngleDeviator {

    private double deviationCW;
    private double deviationCCW;

    public ConstantAngleDeviator(double deviationCW, double deviationCCW) {
        this.deviationCW = deviationCW;
        this.deviationCCW = deviationCCW;
    }

    @Override
	public double deviate(double angle, int turn) {
        if (turn == CW) {
            angle += Math.abs(deviationCW);
        }
        else if (turn == CCW) {
            angle -= Math.abs(deviationCCW);
        }

        return angle;
    }
}
