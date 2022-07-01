
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
public interface AngleDeviator {

    public static final int CW = 1;
    public static final int CCW = 2;

    double deviate(double value, int turn);
}
