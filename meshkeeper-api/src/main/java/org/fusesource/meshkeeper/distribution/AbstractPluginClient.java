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
package org.fusesource.meshkeeper.distribution;

import org.fusesource.meshkeeper.MeshKeeper;

/**
 * AbstractPluginClient
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public abstract class AbstractPluginClient implements PluginClient {

    protected ClassLoader userClassLoader = null;
    protected MeshKeeper meshKeeper = null;

    /**
     * Sets the meshkeeper instance that created the plugin client
     * 
     * @param meshKeeper
     */
    public void setMeshKeeper(MeshKeeper meshKeeper) {
        this.meshKeeper = meshKeeper;
    }

    /**
     * @return the meshkeeper instance that created the plugin client
     */
    public MeshKeeper getMeshKeeper() {
        return meshKeeper;
    }

    /**
     * Sets the user class loader. Setting the user class loader assists
     * meshkeeper in resolving user's serialized objects.
     * 
     * @param classLoader
     *            The user classloader.
     */
    public void setUserClassLoader(ClassLoader classLoader) {
        userClassLoader = classLoader;
    }

    /**
     * Gets the user class loader. Setting the user class loader assists
     * meshkeeper in resolving user's serialized objects.
     * 
     * @return The user classloader.
     */
    public ClassLoader getUserClassLoader() {
        return userClassLoader;
    }

}
