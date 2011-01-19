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

import static org.fusesource.meshkeeper.Expression.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.util.FileUtils;
import org.fusesource.meshkeeper.HostProperties;
import org.fusesource.meshkeeper.LaunchDescription;
import org.fusesource.meshkeeper.MeshArtifact;
import org.fusesource.meshkeeper.MeshKeeper;
import org.fusesource.meshkeeper.MeshProcess;
import org.fusesource.meshkeeper.util.DefaultProcessListener;

import org.meshkeeper.deployer.util.AntRunner;


public class BaseComponent implements Component {
  protected Log log = LogFactory.getLog(this.getClass());
  
  protected STATE state = STATE.UNITIALIZED;
  private static final AtomicInteger idGen = new AtomicInteger();
  private String id;
  private String name;
  private String installFolder;
  private String product;
  private String version;
  private Set<String> roles;
  
  private List<String> deploymentDependencies = Collections.emptyList();
  private List<String> runtimeDependencies = Collections.emptyList();
  
  private boolean deployable = true;
  private boolean installable = true;
  private boolean backupEnabled = true;
  private boolean configurable = true;
  private boolean launchable = true;
  
  protected String sourceRepositoryId;
  protected String sourcePath;
  protected HostProperties hostProperties;
  protected String installPath;
  
  public synchronized void setInstallPath(String installPath) {
    this.installPath = installPath;
  }

  protected String backupPath;
  protected String configPath;
  
  protected BaseDeployment deployment;
  protected MeshKeeper meshKeeper;
  
  private String executableExtension = null;
  private MeshProcess process;
  
  
  public BaseComponent() {
    
  }
  
  public LaunchDescription getLaunchDescription() {
   return null;
  }
  
  public void initialize(BaseDeployment theDeployment) {
    this.deployment = theDeployment;
    
    if(installFolder == null) {
      installFolder = id != null ? id : getProduct();
    }
    
    if(id == null) {
      id = getProduct() + idGen.incrementAndGet();
    }
    
    if(name == null) {
      name = id;
    }
    
    if(isDeployable() || isLaunchable()) {
      meshKeeper = deployment.getMeshKeeper();
      hostProperties = deployment.getInstallationHostProperties(this);
    }
    
    if(theDeployment.isSuppressBackups()) {
      backupEnabled = false;
    }
    
    String installBase = deployment.getInstallationBase(this);
    backupPath = deployment.getBackupBaseDirectory(this);
    if(installPath == null) {
      installPath = installBase + "/" + installFolder;
      backupPath = backupPath + "/" + installFolder;
    }
    if(roles == null) {
      roles = Collections.emptySet();
    }
    state = STATE.INITIALIZED;
    log.info("Initialized " + getName());
  }
  
  public boolean isDeployable() {
    return deployable;
  }
  

  public STATE getState() {
    return state;
  }

  public void setState(STATE state) {
    this.state = state;
  }

  public void deploy() throws DeploymentException {
    if(!state.isInitialized()) {
      throw new IllegalStateException("Can't deploy unitialized component: " + this);
    }
    
    if(!state.isDeployed()) {
      log.info("Deploying " + getName());
      
      //Copy the source locally:
      sourcePath = resolve();
      
      //Install and Configure it:
      configure();
      
      state = STATE.DEPLOYED;
    }
  }
  
  protected String resolve() throws DeploymentException {
    //TODO should run this on the target host
    //Copy down the installation source:
    MeshArtifact source = meshKeeper.repository().createArtifact();
    source.setRepositoryId(sourceRepositoryId);
    source.setRepositoryPath(product + "/" + version + "/");
    source.setType(MeshArtifact.DIRECTORY);


    log.info("Resolving " + source.getRepositoryPath() + " from " + source.getRepositoryId()
        + "...");

    try {
      source = meshKeeper.repository().resolveArtifact(source, "savvion-cache");
    } catch (Exception e) {
      throw new DeploymentException("Error resolving " + source.getRepositoryPath() + " from " + source.getRepositoryId(), e);
    }

    log.info("Resolved " + source.getRepositoryPath() + " to " + source.getLocalPath());
    
    return source.getLocalPath();
  }
  

  protected void configure() throws DeploymentException {
    //Install and Configure it:
    AntRunner configurator = new AntRunner();
    configurator.setName(getName() + " Configurator");
    configurator.setMeshKeeper(meshKeeper);
    if(isInstallable()) {
      configurator.addJVMArg("install.src", file(sourcePath));
    }
    
    if(isBackupEnabled()) {
      configurator.addJVMArg("backup.dir", file(backupPath));
    }
    configurator.addJVMArg("install.dir", file(installPath));

    configurator.setJvm(file(hostProperties.getSystemProperties().getProperty("java.home") + "/bin/java"));
    configurator.setWorkingDir(file(deployment.getConfigLocation(this)));
    configurator.setBuildFile(file(deployment.getConfigLocation(this) + "/config.xml"));
   
    //alfrescoConfig.addArgs("-verbose");
    if(isInstallable()) {
      configurator.addArgs("install");
    }
    
    if(isBackupEnabled()) {
      configurator.addArgs("backup");
    }
    
    if(isConfigurable()) {
      
      
      Properties configProps = getConfigurationProperties();
      File propFile = new File(deployment.getInstallationBase(this) + "/" + getInstallFolder() + "_config.properties");
      propFile.getParentFile().mkdirs();
      if(propFile.exists()) {
        FileUtils.delete(propFile);
      }
      try {
        FileOutputStream out = new FileOutputStream(propFile);
        configProps.store(out, getName() + " " + getVersionInformation());
        out.flush();
        out.close();
        //TODO This file should be distributed.
        configurator.addJVMArg("config.properties", file(propFile.getCanonicalPath()));
      }
      catch (IOException ioe) {
        throw new DeploymentException("Error writing configuration properties", ioe);
      }
      
      configurator.addArgs("configure");
    
      //Let subclasses do further config:
      addConfigurationProperties(configurator);
      
      try {
        if (configurator.runAnt(hostProperties.getAgentId()) != 0) {
          throw new DeploymentException("Failed to configure " + getName() + "!");
        }
      }
      catch (Exception e) {
        throw new DeploymentException("Failed to configure " + this, e);
      }
    }
  }
  
  /**
   * Subclasses that override this should add to the properties
   * returned here. 
   */
  public Properties getConfigurationProperties() {
    return new Properties();
  }
  
  /**
   * Subclasses can override this to augment the configurator
   */
  protected void addConfigurationProperties(AntRunner runner) {
  }

  
  public void undeploy() throws DeploymentException {
    shutdown();
    if(state.isDeployed() && isDeployable()) {
      log.info("Uninstall not implemented will remain deployed");
    }
    state = STATE.UNDEPLOYED;
  }
  
  public void launch () throws DeploymentException{
    if(!state.isDeployed()) {
      deploy();
    }
    
    if(isLaunchable() ) {
      if(process == null) {
        log.info("Launching " + getName());
        LaunchDescription ld = getLaunchDescription();
        process = launch(ld);
      }
      log.info("Launched " + getName());
      
    }
    state = STATE.STARTED;
  }
  
  protected MeshProcess launch(LaunchDescription ld) {
    
    if(ld != null) {
      log.info("Launching: " + getName());
      
      DefaultProcessListener processListener = new DefaultProcessListener(getName());
      
      try {
        process = meshKeeper.launcher().launchProcess(hostProperties.getAgentId(), ld, processListener);
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new DeploymentException(getName() + " failed to start (interrupted)!");
      } 
      catch (Exception e) {
        throw new DeploymentException(getName() + " failed to launch!", e);
      }
      log.info("Launched: " + getName());
    }
    
    return process;
  }
  
  public void shutdown () throws DeploymentException {
    if(state.isStarted() && isLaunchable() ) {
      log.info("Shutting down " + getName());
      try {
        if(process != null) {
          process.kill();
        }
      } catch (Exception e) {
        throw new DeploymentException("Error killing " + getName(), e);
      }
      process = null;
      state = STATE.DEPLOYED;
      log.info("Shutdown " + getName());
    }
  }
  
  public void restart() throws DeploymentException {
    shutdown();
    launch();
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getId() {
    return id;
  }

  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }

  public void setProduct(String product) {
    this.product = product;
  }

  public String getProduct() {
    return product;
  }

  public synchronized boolean isLaunchable() {
    return launchable && isDeployable();
  }

  public synchronized void setLaunchable(boolean launchable) {
    this.launchable = launchable;
  }

  public synchronized void setDeployable(boolean deployable) {
    this.deployable = deployable;
  }
  
  public synchronized void setConfigurable(boolean configurable) {
    this.configurable = configurable;
  }
  
  public boolean isConfigurable() {
    return configurable && isDeployable();
  }
  
  public synchronized void setBackupEnabled(boolean backupEnabled) {
    this.backupEnabled = backupEnabled;
  }
  
  public boolean isBackupEnabled() {
    return backupEnabled && isDeployable();
  }
  
  public synchronized void setInstallable(boolean installable) {
    this.installable = installable;
  }
  
  public boolean isInstallable() {
    return installable;
  }

  public void setVersion(String version) {
    this.version = version;
  }
  
  public String getVersion() {
    return version;
  }
  
  //Makes spring a little nicer to read:
  /**
   * Sets roles as a comma separated list
   * @param roles
   */
  public void setRolesList(String roles) {
    StringTokenizer tok = new StringTokenizer(roles, ",");
    this.roles = new HashSet<String>();
    while(tok.hasMoreTokens()) {
      this.roles.add(tok.nextToken().trim());
    }
  }
  
  public void setRoles(Set<String> roles) {
    this.roles = new HashSet<String>(roles);
  }
  
  public boolean matchesRole(ComponentRole role) {
    return this.roles.contains(role.getRoleName());
  }
  
  public boolean matchesRole(String roleName) {
    return this.roles.contains(roleName);
  }
  

  public Collection<String> getRoles() {
    return roles;
  }

  /**
   * The id of the repository where installation source resides. The installation
   * source will be downloaded locally and cached so that it can be reused. 
   * 
   * The source for this component is expected to be found at the repositoryroot/<name>/<version>
   * 
   * 
   * @param sourceRepositoryId The id of a configured sources repository. 
   */
  public void setSourceRepositoryId(String sourceRepositoryId) {
    this.sourceRepositoryId = sourceRepositoryId;
  }
  
  /**
   * Gets the path where the component was installed. 
   */
  public String getInstallPath() {
    return installPath;
  }

  /**
   * Gets the id of the repository where installation source resides. The installation
   * source will be downloaded locally and cached so that it can be reused. 
   * 
   * @return The id of a configured sources repository. 
   */
  public String getSourceRepositoryId() {
    return sourceRepositoryId;
  }

  public boolean isWindows() {
    return hostProperties.getOS().toLowerCase().contains("win");
  }
  
  protected void setExecutableExtension(String executableExtension) {
    this.executableExtension = executableExtension;
  }
  
  protected String getExecutableExtension() {
    if(executableExtension == null) {
      executableExtension = isWindows() ? ".exe" : "";
    }
    return executableExtension;
  }

  public HostProperties getHostProperties() {
    return hostProperties;
  }
    
  public String toString() {
    return name;
  }
  
  public void setDeploymentDependencies(String ... deploymentDependencies) {
    setDeploymentDependencies(Arrays.asList(deploymentDependencies));
  }
  
  public void addDeploymentDependencies(String ... deploymentDependencies) {
    if(this.deploymentDependencies == null || this.deploymentDependencies.isEmpty()) {
      setDeploymentDependencies(deploymentDependencies);
    } else {
      getDeploymentDependencies().addAll(Arrays.asList(deploymentDependencies));
    }
  }
  
  public void setDeploymentDependencies(List<String> deploymentDependencies) {
    this.deploymentDependencies = new ArrayList<String>();
    this.deploymentDependencies.addAll(deploymentDependencies);
  }
  
  public void setRuntimeDependencies(String ... runtimeDependencies) {
    setRuntimeDependencies(Arrays.asList(runtimeDependencies));
  }
  
  public void addRuntimeDependencies(String ... runtimeDependencies) {
    if(this.runtimeDependencies == null || this.runtimeDependencies.isEmpty()) {
      setRuntimeDependencies(runtimeDependencies);
    } else {
      getRuntimeDependencies().addAll(Arrays.asList(runtimeDependencies));
    }
  }
  
  public void setRuntimeDependencies(List<String> runtimeDependencies) {
    this.runtimeDependencies = new ArrayList<String>();
    this.runtimeDependencies.addAll(runtimeDependencies);
  }  
  
  public final List<String> getDeploymentDependencies() {
    return deploymentDependencies;
  }

  public final List<String> getRuntimeDependencies() {
    return runtimeDependencies;
  }

  public final String getVersionInformation() {
    return getProduct() + " " + getVersion();
  }

  public void setInstallFolder(String installFolder) {
    this.installFolder = installFolder;
  }

  public String getInstallFolder() {
    return installFolder;
  }


}
