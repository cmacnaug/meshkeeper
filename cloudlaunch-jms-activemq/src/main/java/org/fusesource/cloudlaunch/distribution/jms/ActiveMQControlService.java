/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.cloudlaunch.distribution.jms;

import java.net.URI;
import java.util.List;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.fusesource.cloudlaunch.control.ControlService;

/**
 * ActiveMQControlService
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class ActiveMQControlService implements ControlService {

    BrokerService controlBroker;
    String serviceUrl;
    String dataDirectory = "activemq-control-service";

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.cloudlaunch.control.ControlService#setDataDirectory()
     */
    public void setDataDirectory(String path) {
        this.dataDirectory = path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.cloudlaunch.control.ControlService#start()
     */
    public void start() throws Exception {
        controlBroker.setDataDirectory(dataDirectory);
        controlBroker.start();
        List<TransportConnector> connectors = controlBroker.getTransportConnectors();
        serviceUrl = "activemq:" + connectors.get(0).getConnectUri();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.cloudlaunch.control.ControlService#destroy()
     */
    public void destroy() throws Exception {
        if (controlBroker != null) {
            controlBroker.stop();
            controlBroker.waitUntilStopped();
            controlBroker = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.cloudlaunch.control.ControlService#getName()
     */
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.cloudlaunch.control.ControlService#getServiceUri()
     */
    public String getServiceUri() {
        return serviceUrl;
    }

    static ActiveMQControlService create(URI uri) throws Exception {
        ActiveMQControlService rc = new ActiveMQControlService();
        BrokerService controlBroker = null;
        try {
            controlBroker = BrokerFactory.createBroker(uri);
        } catch (Exception e) {
            controlBroker = new BrokerService();
            controlBroker.setBrokerName("CloudLaunchControlBroker");
            controlBroker.addConnector(uri.toString());
            //controlBroker.setPersistent(false);
            controlBroker.setDeleteAllMessagesOnStartup(true);
            controlBroker.setUseJmx(false);
        }
        rc.controlBroker = controlBroker;
        return rc;
    }

}
