/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.meshkeeper.distribution.jms;

import org.fusesource.meshkeeper.control.ControlService;
import org.fusesource.meshkeeper.control.ControlServiceFactory;
import org.fusesource.meshkeeper.distribution.FactoryFinder;

/**
 * JMSServerFactory
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class JMSServerFactory extends ControlServiceFactory {

    private static final FactoryFinder FACTORY_FINDER = new FactoryFinder("META-INF/services/org/fusesource/meshkeeper/distribution/jms/server/");

    public final FactoryFinder getFactoryFinder() {
        return FACTORY_FINDER;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.fusesource.meshkeeper.control.ControlServiceFactory#createControlService
     * (java.lang.String)
     */
    @Override
    public ControlService createPlugin(String providerUri) throws Exception {

        return create(providerUri);
    }

}
