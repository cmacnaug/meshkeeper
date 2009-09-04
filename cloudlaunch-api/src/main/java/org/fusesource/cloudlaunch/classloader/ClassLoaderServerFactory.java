/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudlaunch.classloader;

import org.fusesource.cloudlaunch.util.internal.URISupport;
import org.fusesource.cloudlaunch.Distributor;
import org.fusesource.cloudlaunch.distribution.FactoryFinder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * ClassLoaderServerFactory provies an extenisble way to create custom ClassLoaderServer implementations.
 *
 * @author chirino
 * @version 1.0
 */
public abstract class ClassLoaderServerFactory {

    private static final Log LOG = LogFactory.getLog(ClassLoaderServerFactory.class);
    private static final FactoryFinder FACTORY_FINDER = new FactoryFinder("META-INF/services/org/fusesource/cloudlaunch/classloader/");

    public static final ClassLoaderServer create(String uri, Distributor distributor) throws Exception {
        String parts[] = URISupport.extractScheme(uri);
        ClassLoaderServerFactory factory = FACTORY_FINDER.create(parts[0]);
        return factory.createClassLoaderManager(parts[1], distributor);
    }

    protected abstract ClassLoaderServer createClassLoaderManager(String uri, Distributor distributor) throws Exception;

}