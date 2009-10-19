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
package org.fusesource.meshkeeper.launcher;

import java.util.Collection;
import java.util.List;

import org.fusesource.meshkeeper.Distributable;
import org.fusesource.meshkeeper.HostProperties;
import org.fusesource.meshkeeper.LaunchDescription;
import org.fusesource.meshkeeper.MeshProcess;
import org.fusesource.meshkeeper.MeshProcessListener;
import org.fusesource.meshkeeper.classloader.Marshalled;

/** 
 * ProcessLauncher
 * <p>
 * Description:
 * </p>
 * @author cmacnaug
 * @version 1.0
 */
public interface LaunchAgentService extends Distributable {

    /**
     * Specifies the registry prefix where IRemoteProcess launchers
     * will be published for discovery. 
     */
    public static final String REGISTRY_PATH = "/launchers";
    
    public void bind(String owner) throws Exception;

    public void unbind(String owner) throws Exception;
    
    /**
     * Request that the agent reserve the specified number of ports. 
     * 
     * @param count The number of porst
     * @return A list of free ports on the agent. 
     * @throws Exception If the specified number of ports could not be reserved. 
     */
    public List<Integer> reserveTcpPorts(int count) throws Exception;
    
    /**
     * Release a list of previously reserved ports.
     * @param ports The list of ports
     */
    public void releaseTcpPorts(Collection<Integer> ports);

    public MeshProcess launch(LaunchDescription launchDescription, MeshProcessListener handler) throws Exception;


    /**
     * Executes a runnable task in a new JVM. 
     *
     * @param runnable
     * @param handler
     * @return
     * @throws Exception
     */
    public MeshProcess launch(Marshalled<Runnable> runnable, MeshProcessListener handler) throws Exception;
    
    public HostProperties getHostProperties() throws Exception;
}
