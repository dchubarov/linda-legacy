
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
public interface Surface {

    void setExtent(double north, double east, double south, double west);

    /**
     * Draw line segment.
     * @param srcX Source position X-coordinate.
     * @param srcY Source position Y-coordinate.
     * @param dstX Target position X-coordinate.
     * @param dstY Target position Y-coordinate.
     */
    void draw(double srcX, double srcY, double dstX, double dstY);
}
