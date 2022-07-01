
/*
 * TWOWLS.ORG PROPRIETARY/CONFIDENTIAL
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.twowls.lab.legacy.linde.render;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public final class DeviatorFactory {

	private static final String PARAM_DEVIATION_CW = "deviationCW";
	private static final String PARAM_DEVIATION_CCW = "deviationCCW";

	private DeviatorFactory() {

	}

	public static AngleDeviator createAngleDeviator(String className, Map<String, String> parameters) {
		Class<?> clazz = null;
		AngleDeviator deviator = null;

		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (clazz != null && AngleDeviator.class.isAssignableFrom(clazz)) {
			Constructor<?> ctor = null;
			try {
				ctor = clazz.getDeclaredConstructor(double.class, double.class);
			} catch (SecurityException | NoSuchMethodException e) {
				e.printStackTrace();
			}

			if (ctor != null) {
				try {
					deviator = (AngleDeviator) ctor.newInstance(
							new Double(parameters.get(PARAM_DEVIATION_CW)),
							new Double(parameters.get(PARAM_DEVIATION_CCW)));
				} catch (IllegalArgumentException | InstantiationException
                        | IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}

		}

		return deviator;
	}
}
