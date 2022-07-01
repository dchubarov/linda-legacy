
/*
 * TWOWLS.ORG PROPRIETARY/CONFIDENTIAL
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.twowls.lab.legacy.linde;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;
import org.twowls.lab.legacy.linde.gui.LindenmayerFrame;


/**
 *
 * @author da
 */
public class LindenmayerApp extends SingleFrameApplication {

    public static void main(String[] args) {
        Application.launch(LindenmayerApp.class, args);
    }

    @Override
    protected void startup() {
        show(new LindenmayerFrame());
    }
}
