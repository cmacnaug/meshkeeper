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

import org.fusesource.meshkeeper.MeshKeeperFactory;
import org.fusesource.meshkeeper.control.ControlServer;
import org.fusesource.meshkeeper.distribution.provisioner.Provisioner.MeshProvisioningException;

/**
 * @author chirino
 */
public class EmbeddedServer implements LocalServer {

    ControlServer controlServer;
    File dataDirectory;

    String registryURI;
    private int registryPort = 0;

    public File getDataDirectory() {
        return dataDirectory;
    }

    public void setServerDirectory(File dataDirectory) {
        this.dataDirectory = dataDirectory;
    }


    public boolean isStarted() {
        return registryURI != null;
    }
    
    public void start() throws MeshProvisioningException {
        // We need to start up..
        if (registryURI != null) {
            return;
        } else {
            registryURI = "zk:tcp://0.0.0.0:" + registryPort;
            if (dataDirectory == null) {
                dataDirectory = MeshKeeperFactory.getDefaultServerDirectory();
            }
            try {
                
                controlServer = MeshKeeperFactory.createControlServer(registryURI, dataDirectory);
                registryURI = controlServer.getRegistryConnectUri();
                
                //Add embedded agent to control server:
                controlServer.setEmbeddedLaunchAgent(MeshKeeperFactory.createAgent(controlServer.getMeshKeeper(), dataDirectory));
            }
            catch (Exception e) {
                try {
                    controlServer.destroy();
                }
                catch (Throwable thrown) {
                    thrown.printStackTrace();
                }
                
                throw new MeshProvisioningException(e.getMessage(), e);
                
            }
        }
    }

    public void stop() throws MeshProvisioningException {
        Exception first = null;

        try {
            controlServer.destroy();
        } catch (Exception e) {
            first = first == null ? e : first;
        } finally {
            controlServer = null;
            registryURI = null;
        }
        
        if(first != null) {
            throw new MeshProvisioningException("Error stopping embedded server", first);
        }
    }

    public String getRegistryUri() {
        if (controlServer != null) {
            return controlServer.getRegistryConnectUri();
        } else {
            return registryURI;
        }
    }

    /**
     * @param registryPort The registry port for the registry server to listen on
     */
    public void setRegistryPort(int registryPort) {
        this.registryPort = registryPort;
    }

    public void setCreateWindow(boolean createWindow) {
        // Noop
    }

    public void setProvisioningTimeout(long timeout) {
        // Noop
    }

    public void setPauseWindow(boolean pauseWindow) {
        // Noop
    }

}