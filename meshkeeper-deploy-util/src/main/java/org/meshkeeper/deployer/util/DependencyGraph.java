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
package org.meshkeeper.deployer.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.meshkeeper.deployer.Component;

public class DependencyGraph extends Graph<Component> {

  public DependencyGraph(Collection<Component> nodes, DependencyMapper resolver) {
    super(nodes, new EdgeFactoryAdapter(resolver));
  }


  private static class EdgeFactoryAdapter extends EdgeFactory<Component> {
    private final DependencyMapper mapper;
    
    EdgeFactoryAdapter(DependencyMapper mapper) {
      super();
      this.mapper = mapper;
      
    }
    
    @Override
    public final Collection<Edge<Component>> getOutgoingEdges(Graph<Component> target, Component node) {
      ArrayList<Edge<Component>> rc = new ArrayList<Edge<Component>>();
      Set<Dependency> dependencies = mapper.getDependencies(node);
      
      for(Dependency dependency : dependencies) {
        rc.add(createEdge(target, node, dependency.dependency, dependency.label));
      }  
      
      return rc;
    }
  }
  
  public static interface DependencyMapper {
    public Set<Dependency> getDependencies(Component component);
  }
  
  public static class Dependency {
    private final Component dependency;
    private final  String label;
    
    public Dependency(Component dependency, String label) {
      this.dependency = dependency;
      this.label = label;
    }
    
  }
  
  
  
}
