/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudlaunch.distribution.rmi.rmiviajms;

import java.rmi.Remote;

import org.fusesource.cloudlaunch.distribution.Distributable;
import org.fusesource.cloudlaunch.distribution.Oneway;
import org.fusesource.cloudlaunch.distribution.rmi.AbstractExporter;
import org.fusesource.rmiviajms.JMSRemoteObject;

/**
 * RmiViaJmsExporter
 * <p>
 * Description: Exports Objects via RMIviaJMS
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
class RmiViaJmsExporter extends AbstractExporter {

    String providerUri;

    protected <T> T export(Object obj, Class<?>[] interfaces) throws Exception {
        return (T) JMSRemoteObject.export(obj, interfaces);
    }

    public void unexport(Distributable obj) throws Exception {
        if (obj instanceof Remote) {
            JMSRemoteObject.unexportObject((Remote) obj, true);
        }
    }

    public void setProviderUri(String providerUri) {
        this.providerUri = providerUri;
    }

    public void start() {
        System.setProperty("org.fusesource.rmiviajms.REMOTE_SYSTEM_CLASS", CloudLaunchRemoteJMSSystem.class.getName());
        CloudLaunchRemoteJMSSystem.PROVIDER_URI = providerUri;
        JMSRemoteObject.addOneWayAnnotation(Oneway.class);
    }

    public void destroy() throws InterruptedException, Exception {
        JMSRemoteObject.resetSystem();
    }

    public String toString() {
        return "RmiViaJmsExporter at " + CloudLaunchRemoteJMSSystem.PROVIDER_URI;
    }

}
