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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.meshkeeper.HostProperties;
import org.fusesource.meshkeeper.MeshKeeper;

import org.meshkeeper.deployer.util.DependencyGraph;
import org.meshkeeper.deployer.util.DependencyGraph.Dependency;

public class BaseDeployment implements Deployment {

  protected static enum STATE {
    UNITIALIZED, INITIALIZED, UNDEPLOYED, DEPLOYED, STARTED;

    STATE() {
    }

    public boolean isGreaterThan(STATE state) {
      return ordinal() > state.ordinal();
    }

    public boolean isLessThan(STATE state) {
      return ordinal() < state.ordinal();
    }
  }

  Log LOG = LogFactory.getLog(Deployment.class);

  private List<Component> components = new ArrayList<Component>();
  
  HostProperties serverHost;
  
  private STATE state = STATE.UNITIALIZED;
  private String installBaseDirectory;
  private boolean suppressBackups;
  private String backupBaseDirectory;
  protected String configLocation;
  protected String deploymentName;
  protected MeshKeeper mk;

  public synchronized void initialize() throws Exception {
    if (state != STATE.UNITIALIZED) {
      return;
    }

    if (configLocation == null) {
      throw new DeploymentException(BaseDeployment.class.getSimpleName()
          + ": configLocation must be set");
    }

    if (deploymentName == null) {
      throw new DeploymentException(BaseDeployment.class.getSimpleName()
          + ": deploymentName must be set");
    }

    if (mk == null) {
      throw new DeploymentException(BaseDeployment.class.getSimpleName() + ": MeshKeeper must be set");
    }

    if (installBaseDirectory == null) {
      throw new DeploymentException(BaseDeployment.class.getSimpleName()
          + ": baseDirectory must be specified");
    }

    installBaseDirectory = installBaseDirectory + "/" + getDeploymentName();
    setBackupBaseDirectory(installBaseDirectory + "/backups");
    // Wait for available launch agents:
    mk.launcher().waitForAvailableAgents(1, 5, TimeUnit.SECONDS);
    serverHost = mk.launcher().getAvailableAgents()[0];

    // Initialize the components:
    // Make a copy since subclasses are allowed to remove components during
    // initialize:
    for (Component component : new ArrayList<Component>(components)) {
      LOG.info(this + " Initializing " + component.getName());
      if(!component.getState().isInitialized()) {
        component.initialize(this);
      }
    }

    LOG.info(this + " Initialized with components:");
    LOG.info(this + " =============================");
    for(Component component : components) {
      LOG.info(this + " - " + component.getName());
      LOG.info(this + "   Provider: " + component.getVersionInformation());
      LOG.info(this + "   Roles   : " + component.getRoles());
    }
    LOG.info(this + "=============================");
    
    components = Collections.unmodifiableList(components);
    state = STATE.INITIALIZED;
  }

  protected void setState(STATE state) {
    this.state = state;
  }
  
  public synchronized void deploy() throws Exception {
    if (state.isGreaterThan(STATE.DEPLOYED)) {
      return;
    }

    initialize();
    
    DependencyGraph graph = new DependencyGraph(components,  new DependencyGraph.DependencyMapper() {
      
      public Set<Dependency> getDependencies(Component component) {
        HashSet<Dependency> rc = new HashSet<Dependency>();
        for (String dependentRole : component.getDeploymentDependencies()) {
          List<Component> dependencies = getComponentsByRoleName(dependentRole);
          //Not dependent on self:
          dependencies.remove(component);
          if (dependencies.isEmpty()) {
            continue;
          } else {
            for (Component dependency : dependencies) {
              rc.add(new Dependency(dependency, dependentRole));
            }
          }
        }
        return rc;
      }
    });

    List<Component> toDeploy = graph.getPartiallyOrderedSet();

    LOG.info(this + " Deploying ... " + toDeploy);

    // Install the components:
    for (Component component : toDeploy) {

      if(!component.getState().isDeployed()) {
        if(component.isDeployable()) {
          component.deploy();
        } else {
          component.setState(Component.STATE.DEPLOYED);
        }
        LOG.info(this + " Deployed " + component);
      }
    }
    
    state = STATE.DEPLOYED;
    LOG.info(this + " Deployment complete");
  }

  public void unDeploy() throws Exception {
    if (state.isLessThan(STATE.DEPLOYED)) {
      return;
    }

    LOG.info(this + " Undeploying");

    shutdown();

    for (Component component : components) {
      component.undeploy();
    }

    state = STATE.UNDEPLOYED;
  }

  public void launch() throws Exception {
    if (!state.isLessThan(STATE.STARTED)) {
      return;
    }

    long start = System.currentTimeMillis();
    
    deploy();

    DependencyGraph graph = new DependencyGraph(components,  new DependencyGraph.DependencyMapper() {
      
      public Set<Dependency> getDependencies(Component component) {
        HashSet<Dependency> rc = new HashSet<Dependency>();
        for (String dependentRole : component.getRuntimeDependencies()) {
          List<Component> dependencies = getComponentsByRoleName(dependentRole);
          //Not dependent on self:
          dependencies.remove(component);
          if (dependencies.isEmpty()) {
            continue;
          } else {
            for (Component dependency : dependencies) {
              rc.add(new Dependency(dependency, dependentRole));
            }
          }
        }
        return rc;
      }
    });
    
    List<Component> toLaunch = graph.getPartiallyOrderedSet();
    
    LOG.info(this + " Launching ... " + toLaunch);

    for (Component component : toLaunch) {
      if(!component.getState().isStarted()) {
        if(component.isLaunchable()) {
          component.launch();
        } else {
          component.setState(Component.STATE.STARTED);
        }
      }
    }

    state = STATE.STARTED;
    LOG.info(this + " Launched all components in " + (System.currentTimeMillis() - start) / 1000 + " seconds");
  }

  public void shutdown() throws Exception {
    if (state.isLessThan(STATE.STARTED)) {
      return;
    }

    LOG.info(this + " Shutting down");
    for (Component component : components) {
      component.shutdown();
    }

    state = STATE.DEPLOYED;
  }

  public void addComponent(Component component) {
    assertUnitialized();
    components.add(component);
  }

  public void removeComponent(Component component) {
    assertUnitialized();
    components.remove(component);
  }

  public void setComponents(List<Component> components) {
    assertUnitialized();
    this.components = components;
  }

  public List<Component> getComponents() {
    return this.components;
  }

  public String getConfigLocation(Component component) {
    return configLocation + "/" + component.getProduct() + "/" + component.getVersion();
  }

  public void setConfigLocation(String configLocation) {
    this.configLocation = configLocation;
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Component> List<T> getExpectedComponentsByRole(ComponentRole role) {
    return (List<T>) getExpectedComponentsByRoleName(role.getRoleName());
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Component> List<T> getExpectedComponentsByRoleName(String role) {
    List<T> rc = (List<T>) getComponentsByRoleName(role);
    if (rc == null || rc.isEmpty()) {
      throw new DeploymentException("No component found matching the role " + role);
    }

    return rc;
  }

  @SuppressWarnings("unchecked")
  public <T extends Component> T getExpectedComponentByRole(ComponentRole role) {
    return (T) getExpectedComponentByRoleName(role.getRoleName());
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Component> T getExpectedComponentByRoleName(String role) {
    T rc = (T) getComponentByRoleName(role);
    if (rc == null) {
      throw new DeploymentException("No component found matching the role " + role);
    }

    return rc;
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Component> T getComponentByRole(ComponentRole role) {
    return (T) getComponentByRoleName(role.getRoleName());
  }
   
  
  public <T extends Component> T getComponentByRoleName(String role) {
    List<T> matching = getComponentsByRoleName(role);
    
    if (matching.size() > 1) {
      throw new DeploymentException("Too many components found matching the role " + role);
    }
    if(matching.isEmpty()) { 
      return null;
    }
    return matching.get(0);
  }

  @SuppressWarnings("unchecked")
  public <T extends Component> List<T> getComponentsByRole(ComponentRole role) {
    return (List<T>) getComponentsByRoleName(role.getRoleName());
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Component> List<T> getComponentsByRoleName(String role) {
    LinkedList<T> matching = new LinkedList<T>();
    for (Component component : components) {
      if (component.getRoles().contains(role)) {
        matching.add((T) component);
      }
    }

    return matching;
  }

  public String getDeploymentName() {
    return deploymentName;
  }

  public void setDeploymentName(String deploymentName) {
    this.deploymentName = deploymentName;
  }

  public void setMeshKeeper(MeshKeeper mk) {
    this.mk = mk;
  }

  public MeshKeeper getMeshKeeper() {
    return mk;
  }

  public String getInstallBaseDirectory() {
    return installBaseDirectory;
  }

  public void setInstallBaseDirectory(String installBaseDirectory) {
    this.installBaseDirectory = installBaseDirectory;
  }
  
  public void setBackupBaseDirectory(String backupBaseDirectory) {
    this.backupBaseDirectory = backupBaseDirectory;
  }

  public String getBackupBaseDirectory(Component component) {
    return backupBaseDirectory;
  }
  
  public String getInstallationBase(Component component) {
    // TODO When we go distributed, we'll need to look up the target host's install dir:
    return installBaseDirectory;
  }

  public HostProperties getInstallationHostProperties(
      Component component) {
    return serverHost;
  }

  private void assertUnitialized() {
    if (state.isGreaterThan(STATE.UNITIALIZED)) {
      throw new IllegalStateException("Can't modify components after initialization");
    }
  }

  public String toString() {
    return deploymentName + "[" + state.name() + "]";
  }

  public void setSuppressBackups(boolean suppressBackups) {
    this.suppressBackups = suppressBackups;
  }

  public boolean isSuppressBackups() {
    return suppressBackups;
  }
  

}
