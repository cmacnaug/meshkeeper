/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudlaunch.distribution.event.vm;

import org.fusesource.cloudlaunch.distribution.event.EventClient;
import org.fusesource.cloudlaunch.distribution.event.EventClientFactory;

/**
 * VMEventClientFactory
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class VMEventClientFactory extends EventClientFactory {

    /*
     * (non-Javadoc)
     * 
     * @seeorg.fusesource.cloudlaunch.distribution.event.EventClientFactory#
     * createEventClient(java.lang.String)
     */
    @Override
    protected EventClient createEventClient(String uri) throws Exception {
        return new VMEventClient();
    }

}
