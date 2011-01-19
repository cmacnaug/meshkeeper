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

import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.meshkeeper.deployer.util.ClassUtils;
import org.meshkeeper.deployer.util.ClassUtils.ClassFilter;

/**
 * This class assists in the discovery of {@link Component}s. It
 * will search for any class implementing the {@link Component} interface
 * under the specified package names. It is useful in Spring for example:
 * <br>
 * <pre>
 * {@code
 *  <bean id="componentFactory" class="org.meshkeeper.deployer.ComponentFactory">
 *   <property name="componentPackages">
 *     <list>
 *       <value>com.example.mycomponents</value>
 *       <value>com.example.morecomponents</value>
 *     </list> 
 *   </property>
 *  </bean>
 *  
 *  <!--Create beans through the factory using spring property replacement e.g.:-->
 *  <bean id="database" class="org.meshkeeper.deployer.Component" factory-bean="componentFactory" factory-method="create">
 *     <constructor-arg index="0" value="$database.provider"/>
 *     <constructor-arg index="1" value="$database.version"/>
 *  </bean>
 * }
 * </pre>
 * When it finds a component it registers it under the component {@link Class}'s simple
 * name unless the class is annotated with {@link ProductName} in which case that
 * name will be used instead.
 * 
 * @author cmacnaug
 */
public class ComponentFactory {
  private static final Log LOG = LogFactory.getLog(ComponentFactory.class);
  private final HashMap<String, Class<?>> DISCOVERED = new HashMap<String, Class<?>>();

  public void setComponentPackages(List<String> packageNames) throws DeploymentException{
    
    try {
      for(String packageName : packageNames) {
        for (Class<?> discovered : ClassUtils.findClasses(packageName,
            new ClassFilter() {

              public boolean match(Class<?> clazz) {
                return clazz != null && !clazz.isInterface()
                    && Component.class.isAssignableFrom(clazz);
              }
  
            })) {
          
          ProductName pn = discovered.getAnnotation(ProductName.class);
          String productName = pn == null ? discovered.getSimpleName() : pn.value();
          DISCOVERED.put(productName.toUpperCase(), discovered);
          LOG.debug("Discovered component class: " + discovered.getCanonicalName());
        }
      }
    } catch (Exception e) {
      LOG.error("Error discovery service classes", e);
    }
  }

  public Component create(String product, String version) {
    BaseComponent component = null;

    if (DISCOVERED.containsKey(product.toUpperCase())) {
      Class<?> componentClass = DISCOVERED.get(product.toUpperCase());
      try {
        component = (BaseComponent) componentClass.newInstance();
      } catch (Throwable e) {
        LOG.error("Error creating service class for " + product + " " + version, e);
      }
    }

    if (component == null) {
      component = new BaseComponent();
    }
    
    component.setProduct(product);
    component.setVersion(version);
    return component;
  }
}
