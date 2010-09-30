/*
 * gvNIX. Spring Roo based RAD tool for Conselleria d'Infraestructures
 * i Transport - Generalitat Valenciana
 * Copyright (C) 2010 CIT - Generalitat Valenciana
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gvnix.service.layer.roo.addon;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.StringUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * gvNix Wsdl parser utilities.
 * 
 * <p>
 * Compatible address should be SOAP protocol version 1.1 and 1.2.
 * </p>
 * 
 * @author Mario Martínez Sánchez( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public class WsdlParserUtils {

    public static final String SOAP_11_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String SOAP_11_NAMESPACE_WITHOUT_SLASH = SOAP_11_NAMESPACE
	    .substring(0, SOAP_11_NAMESPACE.length() - 1);
    public static final String SOAP_12_NAMESPACE = "http://schemas.xmlsoap.org/wsdl/soap12/";
    public static final String SOAP_12_NAMESPACE_WITHOUT_SLASH = SOAP_12_NAMESPACE
	    .substring(0, SOAP_12_NAMESPACE.length() - 1);

    public static final String HTTP_PROTOCOL_PREFIX = "http://";
    public static final String URN_PROTOCOL_PREFIX = "urn:";
    public static final String XML_NAMESPACE_PREFIX = "xmlns:";
    public static final String NAMESPACE_SEPARATOR = ":";
    public static final String URL_SEPARATOR = "/";
    public static final String FILE_SEPARATOR = "/";
    public static final String DOMAIN_SEPARATOR = ".";
    public static final String XPATH_SEPARATOR = "/";
    public static final String PACKAGE_SEPARATOR = ".";

    public static final String DEFINITIONS_ELEMENT = "definitions";
    public static final String BINDING_ELEMENT = "binding";
    public static final String PORT_TYPE_ELEMENT = "portType";
    public static final String SERVICE_ELEMENT = "service";
    public static final String PORT_ELEMENT = "port";
    public static final String ADDRESS_ELEMENT = "address";

    public static final String TARGET_NAMESPACE_ATTRIBUTE = "targetNamespace";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String BINDING_ATTRIBUTE = "binding";
    public static final String TYPE_ATTRIBUTE = "type";

    public static final String BINDINGS_XPATH = XPATH_SEPARATOR
	    + DEFINITIONS_ELEMENT + XPATH_SEPARATOR + BINDING_ELEMENT;
    public static final String PORT_TYPES_XPATH = XPATH_SEPARATOR
	    + DEFINITIONS_ELEMENT + XPATH_SEPARATOR + PORT_TYPE_ELEMENT;
    public static final String ADDRESSES_XPATH = XPATH_SEPARATOR
	    + DEFINITIONS_ELEMENT + XPATH_SEPARATOR + SERVICE_ELEMENT
	    + XPATH_SEPARATOR + PORT_ELEMENT + XPATH_SEPARATOR
	    + ADDRESS_ELEMENT;

    /**
     * Constructs a valid java package path from target namespace of root wsdl.
     * 
     * <p>
     * Package ends with the package separator. If target namespace has not a
     * compatible prefix, empty string will be returned.
     * </p>
     * 
     * @param root
     *            Root element of the wsdl
     * @return Equivalent java package or empty
     */
    public static String getTargetNamespaceRelatedPackage(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	// Get the namespace attribute from root wsdl in lower case
	String namespace = root.getAttribute(TARGET_NAMESPACE_ATTRIBUTE).toLowerCase();

	// First string separator and second one
	String separator1 = null;
	String separator2 = null;

	if (namespace.startsWith(HTTP_PROTOCOL_PREFIX)) {

	    // Remove http prefix and final url separator from namespace
	    namespace = namespace.substring(HTTP_PROTOCOL_PREFIX.length());
	    if (namespace.endsWith(URL_SEPARATOR)) {

		namespace = namespace.substring(0, namespace.length() - 1);
	    }

	    separator1 = URL_SEPARATOR;
	    separator2 = DOMAIN_SEPARATOR;
	    
	} else if (namespace.startsWith(URN_PROTOCOL_PREFIX)) {

	    // Remove urn prefix from namespace
	    namespace = namespace.substring(URN_PROTOCOL_PREFIX.length());

	    separator1 = ":";
	    separator2 = "-";
	}

	// Revert namespace and replace url and domain with package separator
	String path = "";

	// Url tokens
	StringTokenizer urlTokens = new StringTokenizer(namespace, separator1);
	if (urlTokens.hasMoreTokens()) {

	    // Domain is the first token of the Url
	    StringTokenizer domainTokens = new StringTokenizer(urlTokens
		    .nextToken(), separator2);
	    while (domainTokens.hasMoreTokens()) {

		path = domainTokens.nextToken() + PACKAGE_SEPARATOR + path;
	    }

	    while (urlTokens.hasMoreTokens()) {

		path = path + urlTokens.nextToken() + PACKAGE_SEPARATOR;
	    }
	}

	return path;
    }

    /**
     * Find the first compatible address element of the root.
     * 
     * <p>
     * Compatible address should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root
     *            Root element of wsdl.
     * @return First compatible address element or null if no element.
     */
    public static Element findFirstCompatibleAddress(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	// Find all address elements
	List<Element> addresses = XmlUtils.findElements(ADDRESSES_XPATH, root);

	// Separate on a list the addresses prefix
	List<String> prefixes = new ArrayList<String>();
	for (int i = 0; i < addresses.size(); i++) {

	    String nodeName = addresses.get(i).getNodeName();
	    prefixes.add(i, getNamespace(nodeName));
	}

	// Separate on a list the addresses namespace
	List<String> namespaces = new ArrayList<String>();
	for (int i = 0; i < prefixes.size(); i++) {

	    namespaces.add(i, getNamespaceURI(root, prefixes.get(i)));
	}

	// Any namepace is a SOAP namespace with or whitout final slash ?
	int index;
	if ((index = namespaces.indexOf(SOAP_12_NAMESPACE)) != -1
		|| (index = namespaces.indexOf(SOAP_12_NAMESPACE_WITHOUT_SLASH)) != -1) {

	    // First preference: SOAP 1.2 protocol

	} else if ((index = namespaces.indexOf(SOAP_11_NAMESPACE)) != -1
		|| (index = namespaces.indexOf(SOAP_11_NAMESPACE_WITHOUT_SLASH)) != -1) {

	    // Second preference: SOAP 1.1 protocol

	} else {

	    // Other protocols not supported
	    return null;
	}

	return addresses.get(index);
    }

    /**
     * Obtain from the list the element with the reference on the root wsdl.
     * 
     * @param root
     *            Root wsdl
     * @param elements
     *            Elements list to search in
     * @param reference
     *            Reference to be searched
     * @return Element found or null if not
     */
    private static Element getReferencedElement(Element root,
	    List<Element> elements, String reference) {

	Assert.notNull(root, "Wsdl root element required");
	Assert.notNull(elements, "Elements list required");
	Assert.notNull(reference, "Reference required");

	String prefix = getNamespace(reference);
	String sufix = getLocalName(reference);
	String namespace = getNamespaceURI(root, prefix);

	Element element = null;
	for (Element elementIter : elements) {

	    String referenceIter = elementIter.getAttribute(NAME_ATTRIBUTE);
	    String prefixIter = getNamespace(referenceIter);
	    String sufixIter = getLocalName(referenceIter);
	    String namespaceIter = getNamespaceURI(root, prefixIter);
	    if (sufixIter.equals(sufix) && namespaceIter.equals(namespace)) {

		element = elementIter;
	    }
	}

	return element;
    }

    /**
     * Get the path to the generated service class.
     * 
     * @param root
     *            Wsdl root element
     * @return Path to the class
     */
    public static String getServiceClassPath(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	// Build the classpath related to the namespace
	String path = getTargetNamespaceRelatedPackage(root);

	// Find a compatible service name
	String name = StringUtils
		.capitalize(findFirstCompatibleServiceName(root));

	// Class path is the concat of path and name
	return path + name;
    }

    /**
     * Get the path to the generated port type class.
     * 
     * @param root
     *            Wsdl root element
     * @return Path to the class
     */
    public static String getPortTypeClassPath(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	// Build the classpath related to the namespace
	String path = getTargetNamespaceRelatedPackage(root);

	// Find a compatible port type name
	String name = StringUtils
		.capitalize(findFirstCompatiblePortTypeName(root));

	// Class path is the concat of path and name
	return path + name;
    }

    /**
     * Find the first compatible service name of the root.
     * 
     * <p>
     * Compatible service should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root
     *            Root element of wsdl
     * @return First compatible service name
     */
    private static String findFirstCompatibleServiceName(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	Element port = findFirstCompatiblePort(root);

	// Get the path to the service class defined by the wsdl
	Element service = ((Element) port.getParentNode());
	String name = service.getAttribute(NAME_ATTRIBUTE);
	Assert.hasText(name, "No name attribute in service element");

	return name;
    }

    /**
     * Find the first compatible port element of the root.
     * 
     * <p>
     * Compatible port should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root
     *            Root element of wsdl
     * @return First compatible port element
     */
    public static Element findFirstCompatiblePort(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	// Find a compatible address element
	Element address = findFirstCompatibleAddress(root);
	Assert.notNull(address, "No compatible SOAP 1.1 or 1.2 protocol");

	// Get the port element defined by the wsdl
	Element port = ((Element) address.getParentNode());

	return port;
    }
    

    /**
     * Find the first compatible port element name of the root.
     * 
     * <p>
     * Compatible port should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root
     *            Root element of wsdl
     * @return First compatible port element name
     */
    public static String findFirstCompatiblePortName(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	// Get the the port element name
	return findFirstCompatiblePort(root).getAttribute(NAME_ATTRIBUTE);
    }

    /**
     * Find the first compatible port type name of the root.
     * 
     * <p>
     * Compatible port type should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root
     *            Root element of wsdl
     * @return First compatible port type name
     */
    private static String findFirstCompatiblePortTypeName(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	Element binding = findFirstCompatibleBinding(root);

	// Find all port types elements
	List<Element> portTypes = XmlUtils.findElements(PORT_TYPES_XPATH, root);
	Assert.notEmpty(portTypes, "No valid port type format");
	String portTypeRef = binding.getAttribute(TYPE_ATTRIBUTE);
	Assert.hasText(portTypeRef, "No type attribute in binding element");
	Element portType = getReferencedElement(root, portTypes, portTypeRef);
	Assert.notNull(portType, "No valid port type reference");
	String portTypeName = portType.getAttribute(NAME_ATTRIBUTE);
	Assert.hasText(portTypeName, "No name attribute in port type element");

	return portTypeName;
    }

    /**
     * Find the first compatible binding name of the root.
     * 
     * <p>
     * Compatible binding should be SOAP protocol version 1.1 and 1.2.
     * </p>
     * 
     * @param root
     *            Root element of wsdl
     * @return First compatible binding element
     */
    private static Element findFirstCompatibleBinding(Element root) {

	Assert.notNull(root, "Wsdl root element required");

	// Find all binding elements
	List<Element> bindings = XmlUtils.findElements(BINDINGS_XPATH, root);
	Assert.notEmpty(bindings, "No valid binding format");

	Element port = findFirstCompatiblePort(root);
	String bindingRef = port.getAttribute(BINDING_ATTRIBUTE);
	Assert.hasText(bindingRef, "No binding attribute in port element");
	Element binding = getReferencedElement(root, bindings, bindingRef);
	Assert.notNull(binding, "No valid binding reference");

	return binding;
    }

    /**
     * URI of a wsdl namespace, or target namespace if not exists or null.
     * 
     * <p>
     * URI is defined by the value of the root attributes that starts with
     * 'xmlns' namespace. For example, for element
     * 'xmlns:tns="http://tempuri.org/"' the uri is 'http://tempuri.org/'.
     * </p>
     * 
     * @param root
     *            Wsdl root element
     * @param namespace
     *            Namespace to search it URI
     * @return Namespace URI related to the namespace
     */
    private static String getNamespaceURI(Element root, String namespace) {

	Assert.notNull(root, "Wsdl root element required");

	String namespaceURI = null;

	if (namespace != null && namespace.length() > 0) {

	    // Get the namespace related to the prefix
	    namespaceURI = root.getAttribute(XML_NAMESPACE_PREFIX + namespace);
	}

	if (namespaceURI == null) {

	    namespaceURI = root.getAttribute(TARGET_NAMESPACE_ATTRIBUTE);
	}

	return namespaceURI;
    }

    /**
     * Get the prefix of a name, or empty if not.
     * 
     * <p>
     * Prefix is the text before first namespace separator character. For
     * example, for name '<soap12:address>' the prefix is 'soap12'.
     * </p>
     * 
     * @param elementName
     *            An element name
     * @return Prefix of the name or empty if not
     */
    protected static String getNamespace(String elementName) {

	Assert.notNull(elementName, "Element name required");

	String prefix = "";

	// Get the index of the namespace separator char
	int index = elementName.indexOf(NAMESPACE_SEPARATOR);
	if (index != -1) {

	    // Get the prefix
	    prefix = elementName.substring(0, index);
	}

	return prefix;
    }

    /**
     * Get the local name of an element.
     * 
     * <p>
     * Local name is the text after first namespace separator character. For
     * example, for element name soap12:address the local name is 'address'.
     * </p>
     * 
     * @param elementName
     *            An element name
     * @return Sufix of the name or name if not
     */
    protected static String getLocalName(String elementName) {

	Assert.notNull(elementName, "Element name required");

	return elementName.replaceFirst(getNamespace(elementName)
		+ NAMESPACE_SEPARATOR, "");
    }

}
