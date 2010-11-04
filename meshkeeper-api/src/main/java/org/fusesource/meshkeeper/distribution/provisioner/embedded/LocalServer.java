package org.fusesource.meshkeeper.distribution.provisioner.embedded;

import java.io.File;

import org.fusesource.meshkeeper.distribution.provisioner.Provisioner.MeshProvisioningException;

public interface LocalServer {

    public boolean isStarted();
    
    void setServerDirectory(File controlServerDirectory);
    
    public void setProvisioningTimeout(long timeout);
    
    public void setCreateWindow(boolean createWindow);
    
    public void setPauseWindow(boolean pauseWindow);
    

    void setRegistryPort(int registryPort);

    String getRegistryUri() throws MeshProvisioningException;
    
    
    void start() throws MeshProvisioningException ;

    void stop() throws MeshProvisioningException ;

    

    

}
