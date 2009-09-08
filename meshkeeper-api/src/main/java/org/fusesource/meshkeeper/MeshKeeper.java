/**************************************************************************************
 * Copyright (C) 2009 Progress Software, Inc. All rights reserved.                    *
 * http://fusesource.com                                                              *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the AGPL license      *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package org.fusesource.meshkeeper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

import org.fusesource.meshkeeper.classloader.ClassLoaderServer;



/**
 * Distributor
 * <p>
 * A distributor provides access to meshkeeper distribution services.
 * </p>
 * @author cmacnaug
 * @version 1.0
 */
public interface MeshKeeper {

    public interface DistributionRef<D extends Distributable>
    {
        public String getRegistryPath();
        public D getProxy();
        public D getTarget();
    }
    
    /** 
     * Eventing
     * <p>
     * Description:
     * </p>
     * @author cmacnaug
     * @version 1.0
     */
    public interface Eventing {

        /**
         * Sends an event on the given topic.
         */
        public void sendEvent(Event event, String topic) throws Exception;

        /**
         * Opens a listener on the given event topic.
         *
         * @param listener The listener
         * @param topic The topic
         * @throws Exception If there is an error opening the listener
         */
        public void openEventListener(EventListener listener, String topic) throws Exception;

        /**
         * Stops listening to events on the given topic.
         *
         * @param listener The listener The listener
         * @param topic The topic
         * @throws Exception If there is an error closing the listener
         */
        public void closeEventListener(EventListener listener, String topic) throws Exception;
    }

    
    /** 
     * Launcher
     * <p>
     * Description:
     * </p>
     * @author cmacnaug
     * @version 1.0
     */
    public interface Launcher {

        /**
         * Requests the specified number of tcp ports from the specified process
         * launcher.
         * 
         * @param agentName
         *            The name of the process launcher
         * @param count
         *            The number of ports.
         * @return The reserved ports
         * @throws Exception
         *             If there is an error reserving the requested number of ports.
         */
        public List<Integer> reserveTcpPorts(String agentName, int count) throws Exception;
        
        /**
         * Releases previously reserved ports at the launcher.
         */
        public void releasePorts(String agentName, Collection<Integer> ports) throws Exception; 
        
        /**
         * Releases all ports that have been reserved on the specified launcher.
         */
        public void releaseAllPorts(String agentName) throws Exception;

        /**
         * Waits for 
         * @param timeout
         * @throws InterruptedException
         * @throws TimeoutException
         */
        public void waitForAvailableAgents(long timeout) throws InterruptedException, TimeoutException;

        /**
         * Gets available the properties of available agents. 
         * @return A list of available agents. 
         */
        public HostProperties[] getAvailableAgents();
        
        /**
         * Attempts to bind the given agent. Once an agent is bound it is exclusively
         * available to this mesh keeper until it is released or this keeper is closed. 
         * 
         * @param agentName The agent to bind. 
         * @throws Exception If there is an error binding the agent.
         */
        public void bindAgent(String agentName) throws Exception;

        /**
         * Releases a previously bound agent. 
         * @param agentName The agent name
         * @throws Exception If there is an error binding the agent. 
         */
        public void releaseAgent(String agentName) throws Exception;

        /**
         * Release all agents bound by this launcher. 
         * @throws Exception If there is an error releasing agents. 
         */
        public void releaseAllAgents() throws Exception;

        /**
         * Launches a process on the specified agent. 
         * @param agentId The agent
         * @param launch The launch description. 
         * @param listener A listener for the process's output. 
         * @return The launched process. 
         * @throws Exception If there is an error launching the process. 
         */
        public MeshProcess launchProcess(String agentId, final LaunchDescription launch, ProcessListener listener) throws Exception;

        /**
         * Prints a line to the given process' standard input. 
         * @param process The process. 
         * @param line The line to print. 
         */
        public void println(MeshProcess process, String line);

        /**
         * Creates a remote executor on the given agent. This will create 
         * a new jvm instance on the given agent which can be used to execute 
         * runnables. 
         * 
         * @param agentId The agent id. 
         * @return The executor
         * @throws Exception If there is an error creating the remote executor. 
         */
        public Executor createRemoteExecutor(String agentId) throws Exception;

        /**
         * Launches the given Runnable in a new jvm instance at the specified agent.
         * The Runnable must also implement {@link Serializable} as it will be serialized
         * when sent to the agent for execution. 
         * 
         * @param agentId The agent id 
         * @param runnable The runnable to execute
         * @param listener The listener for output from the launched process. 
         * @return The process in which the runnable is executed. 
         * @throws Exception If there is an error executing the runnable
         */
        public MeshProcess launch(String agentId, Runnable runnable, ProcessListener listener) throws Exception;
        
        /**
         * Gets the {@link ClassLoaderServer} used for remote executables. If the {@link ClassLoaderServer}
         * has not been set by the application, then one will be created. 
         * @return The ClassLoaderServer
         * @throws Exception If there is an error getting the
         */
        public ClassLoaderServer getClassLoaderServer() throws Exception;

        /**
         * Sets the {@link ClassLoaderServer} used for remote executables. 
         */
        public void setClassLoaderServer(ClassLoaderServer classLoaderServer);

        /**
         * @return The amount of time to allow for binding an agent. 
         */
        public long getBindTimeout();

        /**
         * Sets the bind timeout for binding agents
         * @param bindTimeout
         */
        public void setBindTimeout(long bindTimeout);

        /**
         * @return The amount of time allowed for launching a remote proceess. 
         */
        public long getLaunchTimeout();

        /**
         * The amount of time allowed for launching a remote proceess. 
         */
        public void setLaunchTimeout(long launchTimeout);

        /**
         * @return The amount of time allowed for killing a remote proceess. 
         */
        public long getKillTimeout();

        /**
         * The amount of time allowed for killing a remote proceess. 
         */
        public void setKillTimeout(long killTimeout);

    }
    
    /** 
     * Registry
     * <p>
     * Description:
     * </p>
     * @author cmacnaug
     * @version 1.0
     */
    public interface Registry {

        /**
         * Adds an object to the registry at the given path. If sequential is
         * true then the object will be added at the given location with a unique
         * name. Otherwise the object will be added at the location given by path.
         *
         * @param path The path to add to.
         * @param sequential When true a unique child node is created at the given path
         * @param o The object to add.
         * @return The path at which the element was added.
         * @throws Exception If there is an error adding the node.
         */
        public String addRegistryObject(String path, boolean sequential, Serializable o) throws Exception;

        /**
         * Gets the data at the specified node as an object.
         * @param <T> The type of the object expected.
         * @param path The path of the object.
         * @return The object at the given node.
         * @throws Exception If the object couldn't be retrieved.
         */
        public <T> T getRegistryObject(String path) throws Exception;

        /**
         * Gets the data at the specified node.
         * @param path The path of the data.
         * @return The data at the given node.
         * @throws Exception If the object couldn't be retrieved.
         */
        public byte [] getRegistryData(String path) throws Exception;

        /**
         * Removes a node from the registry.
         *
         * @param path The path to remove.
         * @param recursive If true then any children will also be removed.
         * @throws Exception If the path couldn't be removed.
         */
        public void removeRegistryData(String path, boolean recursive) throws Exception;


        /**
         * Adds data to the registry at the given path. If sequential is
         * true then the data will be added at the given location with a unique
         * name. Otherwise the data will be added at the location given by path.
         *
         * @param path The path to add to.
         * @param sequential When true a unique child node is created at the given path
         * @param data The data. If null then a 0 byte array will be stored in the registry
         * @return The path at which the element was added.
         * @throws Exception If there is an error adding the node.
         */
        public String addRegistryData(String path, boolean sequential, byte[] data) throws Exception ;


        /**
         * Adds a listener for changes in a path's child elements.
         * @param path
         * @param watcher
         */
        public void addRegistryWatcher(String path, RegistryWatcher watcher) throws Exception;

        /**
         * Removes a previously registered
         * @param path The path on which the listener was listening.
         * @param watcher The watcher
         */
        public void removeRegistryWatcher(String path, RegistryWatcher watcher) throws Exception;

        /**
         * Convenience method that waits for a minimum number of objects to be registered at the given
         * registry path.
         *
         * @param <T>
         * @param path The path
         * @param min The minimum number of objects to wait for.
         * @param timeout The maximum amount of time to wait.
         * @return The objects that were registered.
         * @throws Exception
         */
        public <T> Collection<T> waitForRegistrations(String path, int min, long timeout) throws TimeoutException, Exception;
    }
    
    public interface Remoting {

        /**
         * Exports a {@link Distributable} object returning an RMI proxy to the Distibutable object. The proxy
         * can then be passed to other applications in the mesh to use via RMI. It is best practice
         * to unexport the object when it is no longer used.
         *
         * The exported object
         *
         * @param <T>
         * @param obj
         * @return
         * @throws Exception
         */
        public <T extends Distributable> T export(T obj) throws Exception;

        /**
         * Unexports a previously exported object.
         *
         * @param obj The object that had previously been exported.
         * @throws Exception If there is an error unexporting the object.
         */
        public void unexport(Distributable obj) throws Exception;
        
    }
    
    /** 
     * Repository
     * <p>
     * Description:
     * </p>
     * @author cmacnaug
     * @version 1.0
     */
    public interface Repository {

        /**
         * Factory method for creating a resource.
         * @return An empty resource.
         */
        public Resource createResource();

        /**
         * Called to locate the given resource.
         * @param resource The resource to locate.
         * @throws Exception If there is an error locating the resource.
         */
        public void resolveResource(Resource resource) throws Exception;

        /**
         * @param resource
         * @param data
         * @throws IOException
         */
        public void deployFile(Resource resource, byte[] data) throws Exception;

        /**
         *
         * @param resource
         * @param d
         * @throws Exception
         */
        public void deployDirectory(Resource resource, File d) throws Exception;

        /**
         * @return The path to the local resource directory.
         */
        public File getLocalRepoDirectory();

        /**
         *
         * @throws IOException
         */
        public void purgeLocalRepo() throws IOException;
    }

    /**
     * Gets a uri which can be used to connect to a meshkeeper server
     * @return
     */
    public String getDistributorUri();

    /**
     * Starts distributor services.
     * @throws Exception
     */
    public void start() throws Exception;

    /**
     * Closes the distributor cleaning up all distributed references.
     */
    public void destroy() throws Exception;

    /**
     * This is a convenience method to register and export a Distributable object. This is equivalent
     * to calling:
     * <code>
     * <br>{@link #export(Distributable)};
     * <br>{@link #addRegistryObject(String, boolean, Serializable)};
     * </code>
     *
     * It is best practice to call {@link #undistribute(Distributable)} once the object is no longer needed.
     *
     *
     * @param path The path at which to register the exported object.
     * @param sequential Whether the registry path should be registered as a unique node at the given path.
     * @param distributable The {@link Distributable} object.
     * @return a {@link DistributionRef} to the distributed object.
     */
    public <T extends Distributable> DistributionRef<T> distribute(String path, boolean sequential, T distributable) throws Exception;

    /**
     * Called to undistribute a previously distributed object. This is equivalent to calling
     * <code>
     * <br>{@link #unexport(Distributable)};
     * <br>{@link #removeRegistryObject(String, boolean, Serializable)};
     * </code>
     * @param distributable The object that previously distributed.
     */
    public void undistribute(Distributable distributable) throws Exception;

    /**
     * Gets the Mesh Registy support interface. Registry support provides a location accessible
     * to all participants in the Mesh where objects and data can be stored, discovered, and
     * retrieved. 
     * 
     * @return The registry. 
     */
    public Registry registry();
    
    /**
     * Gets the Remoting support interface. Remoting support allows rmi like export of objects
     * in the mesh. 
     * 
     * @return The remoting interface. 
     */
    public Remoting remoting();
    
    /**
     * Gets the Eventing support interface. Eventing support allows users to create and listen
     * for events in the Mesh. 
     * 
     * @return The Eventing interface. 
     */
    public Eventing eventing();
    
    /**
     * Gets the Repository support interface. Repository support allows the sharing of artifacts
     * amongst mesh participants. Unlike Registry support, Repository support is intended for large
     * artifacts or directory structures, and also allows referencing resources that are outside the
     * mesh for example in a remote webdav server. 
     * 
     * @return Repository Support. 
     */
    public Repository repository();
    
    /**
     * Gets the launcher support interface. Launcher support allows launching of remote processes
     * and Runnables on a Mesh agent. 
     * 
     * @return The Launcher support interface
     */
    public Launcher launcher();
}