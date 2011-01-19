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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class Graph<N> {
  
  HashMap<N, Node<N>> nodes = new HashMap<N,  Node<N>>();
  
  public Graph(Collection<N> nodes, EdgeFactory<N> edgeFactory) {
    
    for(N object : nodes) {
      if(this.nodes.put(object, new  Node<N>(object)) != null) {
        throw new IllegalArgumentException("Duplicate node provided " + object);
      }
    }
    
    for( Node<N> node : this.nodes.values()) {
      for(Edge<N> outgoing : edgeFactory.getOutgoingEdges(this, node.object)){
        node.out.add(outgoing);
        outgoing.to.in.add(outgoing);
      }
    }
  }
  
  public boolean isEmpty() {
    return nodes.isEmpty();
  }
  
  public void remove(N node) {
    Node<N> n = nodes.remove(node);
    if(n == null) {
      return;
    }
    
    for(Edge<N> in : n.in){
      in.from.out.remove(in);
    }
    
    for(Edge<N> out : n.out){
      out.to.in.remove(out);
    }
  }
  
  public void removeAll(Collection<N> toRemove) {
    for(N n : toRemove) {
      remove(n);
    }
  }
  
  public Graph<N> copy() {
    return new Graph<N>(this.nodes.keySet(), new EdgeFactory<N>() {
      
      @Override
      public Collection<Edge<N>> getOutgoingEdges(Graph<N> graph, N node) {
        Node<N> n = nodes.get(node);
        ArrayList<Edge<N>> ret = new ArrayList<Edge<N>>(n.out.size());
        for(Edge<N> e : n.out) {
          ret.add(createEdge(graph, e.from.object, e.to.object, e.label));
        }
        return ret;
      }
    });
  }
  
  /**
   * Gets a partially orderd set starting with this grapsh leaf nodes. 
   * 
   * @return a partially orderd set starting with this grapsh leaf nodes. 
   * @throws Exception
   */
  public List<N> getPartiallyOrderedSet() throws Exception {
    Graph<N> remaining = copy();
    
    ArrayList<N> directed = new ArrayList<N>();
    
    while(!remaining.isEmpty()) {
      Collection<N> leaves = remaining.getLeaves();
      if(leaves.isEmpty()) {
        throw new Exception("Cycle detected!");
      } else {
        directed.addAll(leaves);
        remaining.removeAll(leaves);
      }
    }
    
    return directed;
  }
  
  public Collection<N> getLeaves() {
    HashSet<N> leaves = new HashSet<N>();
    
    for( Node<N> n : nodes.values()) {
      if(n.out.isEmpty()) {
        leaves.add(n.object);
      }
    }
    
    return leaves;
  }
  
  public static abstract class EdgeFactory <N> {
    public abstract Collection<Edge<N>> getOutgoingEdges(Graph<N> graph, N node);
    
    protected final Edge<N> createEdge(Graph<N> graph, N from, N to, String label){
      Node<N> fromNode = graph.nodes.get(from);
      Node<N> toNode = graph.nodes.get(to);
      Edge<N> edge = new Edge<N>(toNode, fromNode, label);
      return edge;
    }
  }
  
  public static class Node<N> {
    
    final N object;
    final List<Edge<N>> in = new ArrayList<Edge<N>>();
    final List<Edge<N>> out = new ArrayList<Edge<N>>();
    
    private Node(N object) {
      this.object = object;
    }
  }


  public static class Edge<N> {
    final Node<N> to;
    final Node<N> from;
    final String label;
    
    private Edge(Node<N> to, Node<N> from, String label) {
      this.to = to;
      this.from = from;
      this.label = label;
    }
  }
  
  
   
  
}
