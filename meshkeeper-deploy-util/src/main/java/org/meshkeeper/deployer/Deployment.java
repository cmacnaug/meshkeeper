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

import java.util.List;

public interface Deployment {

  /**
   * Sets the location at which component configuration is located. 
   * @param location The configuration location.
   */
  public void setConfigLocation(String location);
  
  
  /**
   * Deploys the components. All defined components
   * will be resolved, installed and configured on a suitable host. 
   * 
   * @throws Exception
   */
  public void initialize() throws Exception;
  
  /**
   * Deploys the components. All defined components
   * will be resolved, installed and configured on a suitable host. 
   * 
   * @throws Exception
   */
  public void deploy() throws Exception;
  
  /**
   * Undeploys all previously deployed components. 
   * 
   * @throws Exception
   */
  public void unDeploy() throws Exception;
  
  /**
   * Launches all deployment components. 
   * 
   * @throws Exception
   */
  public void launch() throws Exception;
  
  public void shutdown() throws Exception;
  
  public String getDeploymentName();
  
  public String getConfigLocation(Component component);
  
  public <T extends Component> List<T> getExpectedComponentsByRole(ComponentRole role);
  
  public <T extends Component> T getExpectedComponentByRole(ComponentRole role);
  
  public <T extends Component> T getComponentByRole(ComponentRole role);
  
  public <T extends Component> List<T> getComponentsByRole(ComponentRole role);
  
  public <T extends Component> T getExpectedComponentByRoleName(String role);
  
  public <T extends Component> T getComponentByRoleName(String role);
  
  public <T extends Component> List<T> getComponentsByRoleName(String role);
}
