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
package org.gvnix.service.roo.addon.ws;

import java.io.IOException;

import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaType;
import org.w3c.dom.Document;

/**
 * Service to manage the Web Services.
 * 
 * <p>
 * It is required to check the configuration with install(CommunicationSense)
 * before executing any operation.
 * </p>
 * 
 * @author Ricardo García at <a href="http://www.disid.com">DiSiD Technologies
 *         S.L.</a> made for <a href="http://www.cit.gva.es">Conselleria
 *         d'Infraestructures i Transport</a>
 * @author Mario Martínez Sánchez ( mmartinez at disid dot com ) at <a
 *         href="http://www.disid.com">DiSiD Technologies S.L.</a> made for <a
 *         href="http://www.cit.gva.es">Conselleria d'Infraestructures i
 *         Transport</a>
 */
public interface WSConfigService {

    /**
     * Sense of the Communication.
     * <ul>
     * <li>EXPORT: From this to external systems.</li>
     * <li>EXPORT_WSDL: From external systems to this WSDL2JAVA.</li>
     * <li>IMPORT: From external systems to this.</li>
     * <li>IMPORT_RPC_ENCODED: From external systems to this RPC Encoded.</li>
     * </ul>
     */
    public enum CommunicationSense {
        EXPORT, EXPORT_WSDL, IMPORT, IMPORT_RPC_ENCODED
    };

    static final String GENERATE_SOURCES = "clean generate-sources";

    static final String GENERATE_SOURCES_INFO = "Generating sources";

    /**
     * Install and configure Web Service library, if not already installed.
     * 
     * @param type
     *            Communication type
     */
    public void install(CommunicationSense type);

    /**
     * Publish a class as Web Service.
     * 
     * @param className
     *            to be published as Web Service.
     * @param annotationMetadata
     *            with all necessary values to define a Web Service.
     * @return true if className has changed to update annotation.
     */
    public boolean exportClass(JavaType className,
            AnnotationMetadata annotationMetadata);

    /**
     * Converts package name to a Target Namespace.
     * 
     * @param packageName
     *            Initial String split with dots.
     * @return Initial String reverted the order.
     */
    public String convertPackageToTargetNamespace(String packageName);

    /**
     * Configure Web Service class to generate wsdl contract in jax2ws plugin in
     * pom.xml.
     * 
     * @param serviceClass
     *            Service to generate Wsdl.
     * @param serviceName
     *            Service name for wsdl file.
     * @param addressName
     *            Address to access the service.
     * @param fullyQualifiedTypeName
     *            class name location defined in annotation.
     */
    public void jaxwsBuildPlugin(JavaType serviceClass, String serviceName,
            String addressName, String fullyQualifiedTypeName);

    /**
     * Add a wsdl location to import.
     * 
     * @param wsdlLocation
     *            WSDL file location
     * @param type
     *            Communication sense type
     * @return wsdl location added, or false if already exists
     */
    public boolean addImportLocation(String wsdlLocation,
            CommunicationSense type);

    /**
     * Add a wsdl location to export.
     * 
     * @param wsdlLocation
     *            WSDL file location.
     * @param wsdlDocument
     *            WSDL file.
     * @param type
     *            Communication sense type
     */
    public void addExportLocation(String wsdlLocation, Document wsdlDocument,
            CommunicationSense type);

    /**
     * Imports a Web Service to class.
     * 
     * @param className
     *            class to import
     * @param wsdlLocation
     *            contract wsdl url to import
     * @param type
     *            Communication sense type
     * @return Generate sources required ?
     */
    public boolean importService(JavaType className, String wsdlLocation,
            CommunicationSense type);

    /**
     * Run maven command with parameters.
     * <p>
     * On development mode maven details will be showed, else input message.
     * </p>
     * 
     * @param parameters
     *            to run with maven.
     * @param message
     *            Information showed if no development mode.
     * @throws IOException
     */
    public void mvn(String parameters, String message) throws IOException;

    /**
     * Create Jax-WS plugin configuration in pom.xml.
     */
    public void installJaxwsBuildPlugin();

    /**
     * Checks if library is properly configured in a project.
     * <p>
     * Library dependencies can be different depending of communication sense.
     * </p>
     * 
     * @param type
     *            Communication type
     * @return true or false if it's configurated
     */
    public boolean isLibraryInstalled(CommunicationSense type);

    /**
     * Is this a web project ?
     * 
     * @return true if this is a web project.
     */
    boolean isProjectWebAvailable();

    /**
     * Is this a project ?
     * 
     * @return true if this is a project.
     */
    boolean isProjectAvailable();

    /**
     * Add project properties values to pom.xml.
     * 
     * @param type
     *            of {@link CommunicationSense}
     */
    public void addProjectProperties(CommunicationSense type);
}
