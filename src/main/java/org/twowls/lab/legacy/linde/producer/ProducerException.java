
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
public class ProducerException extends Exception {

	private static final long serialVersionUID = 1L;

	public ProducerException() {
        super();
    }

    public ProducerException(String message) {
        super(message);
    }

    public ProducerException(Throwable cause) {
        super(cause);
    }

    public ProducerException(String message, Throwable cause) {
        super(message, cause);
    }
}
