
/*
 * TWOWLS.ORG PROPRIETARY/CONFIDENTIAL
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.twowls.lab.legacy.linde.document;

/**
 *
 * @author da
 */
public class DocumentException extends Exception {

	private static final long serialVersionUID = 1L;

	public DocumentException() {
    }

    public DocumentException(String msg) {
        super(msg);
    }

    public DocumentException(Throwable cause) {
        super(cause);
    }

    public DocumentException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
