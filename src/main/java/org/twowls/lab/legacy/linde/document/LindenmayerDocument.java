
/*
 * TWOWLS.ORG PROPRIETARY/CONFIDENTIAL
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.twowls.lab.legacy.linde.document;

import org.twowls.lab.legacy.linde.render.DeviatorFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author da
 */
public class LindenmayerDocument {

    private static final int DEFAULT_ORDER = 0;
    private static final int DEFAULT_WIDTH = 640;
    private static final int DEFAULT_HEIGHT = 480;
    private static final Color DEFAULT_BACKGROUND = Color.BLACK;
    private static final Color DEFAULT_FOREGROUND = Color.GREEN;
    private static final double DEFAULT_INITIAL_ANGLE = 0.0;
    private static final double DEFAULT_TURN_ANGLE = 90.0;
    private static final double DEFAULT_GROW_FACTOR = 1.0;

    private static final String ELEMENT_ROOT = "l-system";
    private static final String ELEMENT_DESCRIPTION = "description";
    private static final String ELEMENT_OPTIONS = "options";
    private static final String ELEMENT_OPTION = "option";
    private static final String ELEMENT_RULES = "rules";
    private static final String ELEMENT_RULE = "rule";
    private static final String ELEMENT_DEVIATIONS = "deviations";
    private static final String ELEMENT_DEVIATION = "deviation";
    private static final String ELEMENT_PARAMETER = "param";

    private static final String ATTRIBUTE_VERSION = "version";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_SYMBOL = "symbol";
    private static final String ATTRIBUTE_MEAN = "mean";
    private static final String ATTRIBUTE_DEVIATION_TARGET = "target";
    private static final String ATTRIBUTE_DEVIATION_CLASS = "class";
    private static final String ATTRIBUTE_PARAMETER_NAME = "name";
    private static final String ATTRIBUTE_AXIOM = "axiom";

    private static final String OPTION_ORDER = "defaultOrder";
    private static final String OPTION_WIDTH = "width";
    private static final String OPTION_HEIGHT = "height";
    private static final String OPTION_BACKGROUND = "background";
    private static final String OPTION_FOREGROUND = "foreground";
    private static final String OPTION_INITIAL_ANGLE = "turtle.initialAngle";
    private static final String OPTION_TURN_ANGLE = "turtle.turnAngle";
    private static final String OPTION_GROW_FACTOR = "turtle.growFactor";

    private static final String DEVIATION_TARGET_ANGLE = "angle";

    private int order;
    private int width;
    private int height;
    private Color background;
    private Color foreground;
    private List<String> rules;
    private String description;
    private String axiom;
    private double initialAngle;
    private double turnAngle;
    private double growFactor;
    private List<Object> deviators;

    private LindenmayerDocument() {
        order = DEFAULT_ORDER;
        width = DEFAULT_WIDTH;
        height = DEFAULT_HEIGHT;
        background = DEFAULT_BACKGROUND;
        foreground = DEFAULT_FOREGROUND;
        initialAngle = DEFAULT_INITIAL_ANGLE;
        turnAngle = DEFAULT_TURN_ANGLE;
        growFactor = DEFAULT_GROW_FACTOR;
        description = null;
        rules = new ArrayList<>();
        deviators = new ArrayList<>();
        axiom = null;
    }

    public String getDescription() {
    	return description;
    }

    public int getOrder() {
        return order;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Dimension getDocumentSize() {
        return new Dimension(width, height);
    }

    public Color getBackground() {
        return background;
    }

    public Color getForeground() {
        return foreground;
    }

    public String getAxiom() {
        return axiom;
    }

    public String getRules() {
        StringBuilder sb = new StringBuilder();
        for (String rule : rules) {
            sb.append(rule);
            sb.append("\n");
        }
        return sb.toString();
    }

    public double getInitialAngle() {
        return initialAngle;
    }

    public double getTurnAngle() {
        return turnAngle;
    }

    public double getGrowFactor() {
        return growFactor;
    }

    public List<Object> getDeviators() {
    	return deviators;
    }

    private void loadFromFile(File sourceFile) throws DocumentException {

    	StreamSource schemaSource = new StreamSource(getClass().getResourceAsStream("lindenmayer.xsd"));

    	SchemaFactory scf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
    	Schema sc = null;
    	try {
			sc = scf.newSchema(schemaSource);
		} catch (SAXException e) {
			throw new DocumentException(e);
		}

		Validator validator = sc.newValidator();
    	try {
			validator.validate(new StreamSource(sourceFile));
		} catch (SAXException e) {
            throw new DocumentException("XML validation failed:\n" + e.getLocalizedMessage(), e);
		} catch (IOException e) {
            throw new DocumentException("I/O error validating document.", e);
		}

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        Document xmldoc = null;
        Element root = null, elem = null;

        try {
            db = dbf.newDocumentBuilder();
        }
        catch (ParserConfigurationException e) {
            throw new DocumentException(e);
        }

        try {
            xmldoc = db.parse(sourceFile);
        }
        catch (IOException e) {
            throw new DocumentException("I/O error reading document.", e);
        }
        catch (SAXException e) {
            throw new DocumentException("Parse error.", e);
        }

        root = xmldoc.getDocumentElement();

        if (root.getTagName().compareToIgnoreCase(ELEMENT_ROOT) != 0) {
            throw new DocumentException("Document element must be " + ELEMENT_ROOT);
        }

        if (root.getAttribute(ATTRIBUTE_VERSION).compareTo("1.0") != 0) {
            throw new DocumentException("Document version must be 1.0");
        }

        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                elem = (Element) node;

                if (ELEMENT_DESCRIPTION.compareToIgnoreCase(elem.getTagName()) == 0) {
                    description = elem.getTextContent();
                }
                else if (ELEMENT_OPTIONS.compareToIgnoreCase(elem.getTagName()) == 0) {
                    loadOptions(elem.getElementsByTagName(ELEMENT_OPTION));
                }
                else if (ELEMENT_RULES.compareToIgnoreCase(elem.getTagName()) == 0) {
                    axiom = elem.getAttribute(ATTRIBUTE_AXIOM);
                    loadRules(elem.getElementsByTagName(ELEMENT_RULE));
                }
                else if (ELEMENT_DEVIATIONS.compareToIgnoreCase(elem.getTagName()) == 0) {
                	loadDeviations(elem.getElementsByTagName(ELEMENT_DEVIATION));
                }
            }
        }
    }

    private void loadDeviations(NodeList deviationElements) {
    	if (deviationElements != null) {
	    	for (int i = 0; i < deviationElements.getLength(); i++) {
	    		Element deviationElement = (Element) deviationElements.item(i);

	    		String deviationTarget = deviationElement.getAttribute(ATTRIBUTE_DEVIATION_TARGET);
	    		String deviationClassName = deviationElement.getAttribute(ATTRIBUTE_DEVIATION_CLASS);

	    		Map<String, String> parameters = new HashMap<>();
	    		NodeList paramElements = deviationElement.getElementsByTagName(ELEMENT_PARAMETER);
	    		if (paramElements != null) {
	    			for (int j = 0; j < paramElements.getLength(); j++) {
	    				Element paramElement = (Element) paramElements.item(j);
	    				String paramName = paramElement.getAttribute(ATTRIBUTE_PARAMETER_NAME);
	    				if (paramName != null && !paramName.isEmpty()) {
	    					parameters.put(paramName, paramElement.getTextContent());
	    				}
	    			}
	    		}

	    		if (DEVIATION_TARGET_ANGLE.compareToIgnoreCase(deviationTarget) == 0) {
	    			deviators.add(DeviatorFactory.createAngleDeviator(deviationClassName, parameters));
	    		}
	    	}
    	}
    }

    private void loadRules(NodeList ruleElements) {
        for (int i = 0; i < ruleElements.getLength(); i++) {
            Element ruleElement = (Element) ruleElements.item(i);
            String rule, tmp;

            tmp = ruleElement.getAttribute(ATTRIBUTE_SYMBOL);
            if (tmp.length() > 0) {
                rule = tmp.substring(0, 1);
                tmp = ruleElement.getAttribute(ATTRIBUTE_MEAN);
                if (tmp.length() > 0) {
                    rule += "(" + tmp.substring(0, 1) + ")";
                }
                rule += "=";
                rule += ruleElement.getTextContent();
                rules.add(rule);
            }
        }
    }

    private void loadOptions(NodeList options) {
        for (int i = 0; i < options.getLength(); i++) {
            Element optionElement = (Element) options.item(i);
            String optionName = optionElement.getAttribute(ATTRIBUTE_NAME);
            String rawValue = optionElement.getTextContent();

            if (optionName.compareToIgnoreCase(OPTION_ORDER) == 0) {
                try {
                    order = Integer.parseInt(rawValue);
                }
                catch (NumberFormatException e) { }
            }
            if (optionName.compareToIgnoreCase(OPTION_WIDTH) == 0) {
                try {
                    width = Integer.parseInt(rawValue);
                }
                catch (NumberFormatException e) { }
            }
            else if (optionName.compareToIgnoreCase(OPTION_HEIGHT) == 0) {
                try {
                    height = Integer.parseInt(rawValue);
                }
                catch (NumberFormatException e) { }
            }
            else if (optionName.compareToIgnoreCase(OPTION_BACKGROUND) == 0) {
                background = colorFromString(rawValue);
                if (background == null) {
                    background = DEFAULT_BACKGROUND;
                }
            }
            else if (optionName.compareToIgnoreCase(OPTION_FOREGROUND) == 0) {
                foreground = colorFromString(rawValue);
                if (foreground == null) {
                    foreground = DEFAULT_FOREGROUND;
                }
            }
            else if (optionName.compareToIgnoreCase(OPTION_INITIAL_ANGLE) == 0) {
                try {
                    initialAngle = Double.parseDouble(rawValue);
                }
                catch (NumberFormatException e) { }
            }
            else if (optionName.compareToIgnoreCase(OPTION_TURN_ANGLE) == 0) {
                try {
                    turnAngle = Double.parseDouble(rawValue);
                }
                catch (NumberFormatException e) { }
            }
            else if (optionName.compareToIgnoreCase(OPTION_GROW_FACTOR) == 0) {
                try {
                    growFactor = Double.parseDouble(rawValue);
                }
                catch (NumberFormatException e) { }
            }
        }
    }

    private static Color colorFromString(String color) {
        if (color != null && !color.isEmpty()) {
            try {
                Pattern pattern = Pattern.compile(
                        "^#([0-9a-z]{2})([0-9a-z]{2})([0-9a-z]{2})$",
                        Pattern.CASE_INSENSITIVE);

                Matcher matcher = pattern.matcher(color.trim());
                if (matcher.matches()) {
                    int r = 0, g = 0, b = 0;
                    r = Integer.parseInt(matcher.group(1), 16);
                    g = Integer.parseInt(matcher.group(2), 16);
                    b = Integer.parseInt(matcher.group(3), 16);
                    return new Color(r, g, b);
                }
            }
            catch (Exception e) { }
        }
        return null;
    }

    public static LindenmayerDocument create() {
        return new LindenmayerDocument();
    }

    public static LindenmayerDocument createFromFile(File sourceFile)
            throws DocumentException {
        LindenmayerDocument doc = new LindenmayerDocument();
        doc.loadFromFile(sourceFile);
        return doc;
    }
}
