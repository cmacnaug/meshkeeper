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
package org.meshkeeper.deployer;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.fusesource.meshkeeper.HostProperties;

/**
 * {@link Component}s are the building blocks of a {@link Deployment}. The deployment
 * will install and launch all defined {@link Component}s. 
 * 
 * SandboxComponents are discovered 
 * 
 * @author cmacnaug
 */
public interface Component {
  
  public static enum STATE {
    UNITIALIZED,
    INITIALIZED,
    UNDEPLOYED,
    DEPLOYED,
    STARTED;
    
    STATE() {
    }
    
    public boolean isGreaterThan (STATE state) {
      return ordinal() > state.ordinal();
    }
    
    public boolean isLessThan (STATE state) {
      return ordinal() < state.ordinal();
    }
    
    public boolean isDeployed() { 
      return !isLessThan(DEPLOYED); 
    }
    
    public boolean isUndeployed() { 
      return isLessThan(DEPLOYED); 
    }
    
    public boolean isStarted() { 
      return !isLessThan(STARTED); 
    }
    
    public boolean isInitialized() { 
      return !isLessThan(INITIALIZED); 
    }
    
    public boolean isUninitialized() { 
      return equals(UNITIALIZED); 
    }
  }
  
  /**
   * A unique id representing the component.
   * @return A unique id representing the component.
   */
  public String getId();
  
  /**
   * @return A short user friendly descriptive name for the component. 
   */
  public String getName();
  
  public String getProduct();
  
  public String getVersion();
  
  public boolean matchesRole(ComponentRole role);
  
  public Collection<String> getRoles();

  public List<String> getDeploymentDependencies();
  
  public List<String> getRuntimeDependencies();
  
  public String getInstallPath();
  
  public void initialize(BaseDeployment deployment) throws DeploymentException;
  
  public void deploy() throws DeploymentException;
  
  public void undeploy() throws DeploymentException;
  
  public void launch() throws DeploymentException;
  
  /**
   * Components can return properties used to configure them. These
   * properties are used, for example, to tailor files laid down
   * during the component configuration. 
   *  
   * @return Gets properties used to configure this component. 
   */
  public Properties getConfigurationProperties();
  
  /**
   * Sets the component state.
   * @param state
   */
  public void setState(STATE state);
  
  /**
   * Gets the component state.
   * @param state the component state.
   */
  public STATE getState();
  
  /**
   * If the component {@link #isLaunchable()} then this will be called by the
   * deployer.
   * @throws DeploymentException
   */
  public void shutdown() throws DeploymentException;
  
  /**
   * Gets the properties of the host on which the component is deployed.
   * 
   * @return The {@link HostProperties} on which the component is deployed.
   */
  public HostProperties getHostProperties();
  
  /**
   * Marks this component as being deployable. By default components are
   * deployable.
   */
  public void setDeployable(boolean deployable);
  
  /**
   * Test whether this component is deployable. A component that is not deployable
   * can't be installed or configured. It is assumed that non deployable components
   * are already deployed outside of the sandbox. 
   * 
   * Deployable components are capable of installation, backup and configuration. 
   */
  public boolean isDeployable();
  
  /**
   * Test whether this component is Installable. 
   */
  public boolean isInstallable();
  
  /**
   * Test whether this component is configurable. Configuration actions will 
   * be attempted for configurable components.
   */
  public boolean isConfigurable();
  
  /**
   * Marks this component as being launchable. By default components are
   * launchable.
   */
  public void setLaunchable(boolean launchable);
  
  /**
   * Test whether this component is launchable. Components that are launchable
   * will be have their {@link Component#launch()} method called by the deployer. 
   */
  public boolean isLaunchable();

  /**
   * @return A String containing product and version information
   */
  String getVersionInformation();
}
