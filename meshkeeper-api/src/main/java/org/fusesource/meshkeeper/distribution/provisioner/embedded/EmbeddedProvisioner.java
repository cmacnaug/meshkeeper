/**
 *  Copyright (C) 2009 Progress Software, Inc. All rights reserved.
 *  http://fusesource.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.fusesource.meshkeeper.distribution.provisioner.embedded;


import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.fusesource.meshkeeper.MeshKeeperFactory;
import org.fusesource.meshkeeper.distribution.provisioner.Provisioner;

/**
 * EmbeddedProvisioner
 * <p>
 * The embedded provisioner provisions local MeshKeeper controller. It supports
 * in process provisioning and shutdown of a controller running in another
 * process
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class EmbeddedProvisioner implements Provisioner {

    private static LocalServer SERVER;
    private static final Object SYNC = new Object();
    private boolean machineOwnerShip;
    private String deploymentUri;
    private int registryPort = 0;
    private long provisioningTimeout = 30000;
    private boolean spawn = false;
    private boolean createWindow = true;
    private boolean leaveRunning = false;
    private boolean pauseWindow = false;
    
    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.meshkeeper.MeshProvisioner#deploy()
     */
    public void deploy() throws MeshProvisioningException {
        synchronized (SYNC) {
            if (SERVER == null) {
                if(spawn) {
                    SERVER = new SpawnedServer();
                }
                else {
                    SERVER = new EmbeddedServer();
                }
                configure(SERVER);
            }
            
            try {
                if(!SERVER.isStarted()) {
                  SERVER.start();
                }
            } catch (Exception e) {
                SERVER = null;
                throw new MeshProvisioningException("Error starting embedded server", e);
            }
        }
    }

    private void configure(LocalServer server) throws MeshProvisioningException {
        server.setRegistryPort(registryPort);
        server.setCreateWindow(createWindow);
        server.setProvisioningTimeout(provisioningTimeout);
        server.setServerDirectory(getControlServerDirectory());
        server.setPauseWindow(pauseWindow);
    }

    /**
     * Forcefully undeploys MeshKeeper
     * @throws MeshProvisioningException
     */
    public void undeploy() throws MeshProvisioningException {
      //NOTE: This method is fired by introspection via provisioner.Main.
      unDeploy(true);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.meshkeeper.MeshProvisioner#unDeploy(boolean)
     */
    public void unDeploy(boolean force) throws MeshProvisioningException {
        synchronized (SYNC) {
            if (isDeployed() && (force || !isLeaveRunning())) {
                try {
                    SERVER.stop();
                    SERVER = null;
                } catch (Exception e) {
                    throw new MeshProvisioningException("Error starting embedded server", e);
                }
            }
            
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.meshkeeper.MeshProvisioner#findMeshRegistryUri()
     */
    public String findMeshRegistryUri() throws MeshProvisioningException {
        if (SERVER != null) {
            return SERVER.getRegistryUri();
        } else {
            
            SpawnedServer server = new SpawnedServer();
            configure(server);
            
            return server.getRegistryUri();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.meshkeeper.MeshProvisioner#getDeploymentUri()
     */
    public String getDeploymentUri() {
        return "embedded:";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.meshkeeper.MeshProvisioner#getPreferredControlHost()
     */
    public String getPreferredControlHost() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.meshkeeper.MeshProvisioner#getRequestedAgentHosts()
     */
    public String[] getRequestedAgentHosts() {
        try {
            return new String[] { InetAddress.getLocalHost().getCanonicalHostName() };
        } catch (UnknownHostException e) {
            return new String[] { "localhost" };
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.fusesource.meshkeeper.MeshProvisioner#getStatus(java.lang.StringBuffer
     * )
     */
    public synchronized StringBuffer getStatus(StringBuffer buffer) throws MeshProvisioningException {
        if (buffer == null) {
            buffer = new StringBuffer(512);
        }

        if (SERVER != null) {
            buffer.append("Embedded MeshKeeper is deployed at: " + findMeshRegistryUri());
        } else {
            buffer.append("Embedded MeshKeeper is not deployed\n");
        }

        return buffer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.meshkeeper.MeshProvisioner#isDeployed()
     */
    public synchronized boolean isDeployed() throws MeshProvisioningException {
        if (SERVER != null) {
            return true;
        }
        //Possible that this is deployed locally in another process.
        else if (deploymentUri != null) {

            SpawnedServer server = new SpawnedServer();
            configure(server);
            
            //If a spawned server is started we'll use it:
            if(server.isStarted()) {
                SERVER = server;
                return true;
            }
        }
        return false;
    }

    File getControlServerDirectory() throws MeshProvisioningException {
        if (deploymentUri != null) {
            return new File(deploymentUri);
        } else {
            return MeshKeeperFactory.getDefaultServerDirectory();
        }
    }

   

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.meshkeeper.MeshProvisioner#reDeploy(boolean)
     */
    public void reDeploy(boolean force) throws MeshProvisioningException {
        unDeploy(true);
        deploy();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.meshkeeper.MeshProvisioner#setDeploymentUri()
     */
    public void setDeploymentUri(String deploymentUri) {
        this.deploymentUri = deploymentUri;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.meshkeeper.MeshProvisioner#setPreferredControlHost()
     */
    public void setPreferredControlHost(String preferredControlHost) {
        // No-Op we'll always go local
    }

    /**
     * Indicates that if the control server is not running it should be spawned. 
     * @param spawn true if a control server should be spawned
     */
    public void setSpawn(boolean spawn) {
        this.spawn = spawn;
    }
    
    /**
     * Indicates that if the control server is not running it should be spawned. 
     * @param spawn true if a control server should be spawned
     */
    public boolean getSpawn() {
        return spawn;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.fusesource.meshkeeper.MeshProvisioner#setRequestedAgentHosts(java
     * .lang.String[])
     */
    public void setRequestedAgentHosts(String[] agentHosts) {
        // No-Op we'll always go local
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.fusesource.meshkeeper.distribution.provisioner.Provisioner#
     * getAgentMachineOwnership()
     */
    public boolean getAgentMachineOwnership() {
        return machineOwnerShip;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.fusesource.meshkeeper.distribution.provisioner.Provisioner#getMaxAgents
     * ()
     */
    public int getMaxAgents() {
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.fusesource.meshkeeper.distribution.provisioner.Provisioner#
     * setAgentMachineOwnership(boolean)
     */
    public void setAgentMachineOwnership(boolean machineOwnerShip) {
        this.machineOwnerShip = machineOwnerShip;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.fusesource.meshkeeper.distribution.provisioner.Provisioner#setMaxAgents
     * (int)
     */
    public void setMaxAgents(int maxAgents) {
        //No-Op for now only one locally.
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.fusesource.meshkeeper.distribution.provisioner.Provisioner#
     * setRegistryPort(int)
     */
    public void setRegistryPort(int port) {
        this.registryPort = port;
    }

    /**
     * 
     * @return The time allows to wait for each provisioned component to come
     *         online.
     */
    public long getProvisioningTimeout() {
        return provisioningTimeout;
    }

    /**
     * sets the time allows to wait for each provisioned component to come
     * online.
     * 
     * @param provisioningTimeout
     *            the time allows to wait for each provisioned component to come
     *            online.
     */
    public void setProvisioningTimeout(long provisioningTimeout) {
        this.provisioningTimeout = provisioningTimeout;
    }

    /**
     * When spawning a new process this indicates whether it should be launched in a new window
     * 
     * @param createWindow
     */
    public void setCreateWindow(boolean createWindow) {
        this.createWindow  = createWindow;
    }

    public void setPauseWindow(boolean pauseWindow) {
        this.pauseWindow = pauseWindow;
    }

    /**
     * When the meshkeeper is spawned setting this to true will leave
     * the spawned instance running unless {@link #unDeploy(boolean)} is 
     * called with true.
     * 
     * @param leaveRunning Leave meshkeeper running on exit.
     */
    public void setLeaveRunning(boolean leaveRunning) {
        this.leaveRunning = leaveRunning;
    }

    /**
     * When the meshkeeper is spawned setting this to true will leave
     * the spawned instance running on exit
     */
    public boolean isLeaveRunning() {
        return leaveRunning;
    }

}
