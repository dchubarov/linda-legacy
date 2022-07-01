
/*
 * TWOWLS.ORG PROPRIETARY/CONFIDENTIAL
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.twowls.lab.legacy.linde.gui;

import java.awt.event.KeyEvent;

import javax.swing.JTextArea;

/**
 *
 * @author da
 */
public class NoTabTextArea extends JTextArea {

	private static final long serialVersionUID = 1L;

	@Override
    protected void processComponentKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED
                && e.getKeyCode() == KeyEvent.VK_TAB) {

            e.consume();
            if (e.isShiftDown()) {
                transferFocusBackward();
            }
            else {
                transferFocus();
            }
        }
        else {
            super.processComponentKeyEvent(e);
        }
    }
}
