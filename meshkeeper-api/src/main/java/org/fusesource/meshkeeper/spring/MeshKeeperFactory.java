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
package org.fusesource.meshkeeper.spring;

import java.io.File;
import java.util.List;

import org.fusesource.meshkeeper.MeshKeeper;
import org.fusesource.meshkeeper.MeshRepository;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * A spring FactoryBean to create MeshKeeper instances.
 * 
 * @author chirino
 */
public class MeshKeeperFactory extends org.fusesource.meshkeeper.MeshKeeperFactory implements FactoryBean, InitializingBean, DisposableBean {

    private MeshKeeper meshKeeper;
    private String registryUri;
    private File directory;
    private String centralRepositoryUri;
    private String provisionerUri;
    private List<MeshRepository> repositories;

    public void afterPropertiesSet() throws Exception {
        if (registryUri != null) {
            System.setProperty(MESHKEEPER_REGISTRY_PROPERTY, registryUri);
        }
        if (centralRepositoryUri != null) {
            System.setProperty(MESHKEEPER_CENTRAL_REPO_URI_PROPERTY, centralRepositoryUri);
        }
        if (directory != null) {
            System.setProperty(MESHKEEPER_BASE_PROPERTY, directory.getAbsolutePath());
        }
        if(provisionerUri != null) {
            System.setProperty(MESHKEEPER_PROVISIONER_PROPERTY, provisionerUri);
        }
        meshKeeper = super.createMeshKeeper();
        
        //Add additional repositories:
        if (repositories != null) {
            for (MeshRepository repo : repositories) {
                meshKeeper.repository().registerRepository(repo);
            }
        }
    }

    public void destroy() throws Exception {
        meshKeeper.destroy();
    }

    public Class<?> getObjectType() {
        return MeshKeeper.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public Object getObject() throws Exception {
        return meshKeeper;
    }

    public void setRegistryUri(String registry) {
        this.registryUri = registry;
    }

    public String getRegistryUri() {
        return registryUri;
    }

    public void setDirectory(File directory) {
        this.directory = directory;
    }

    public File getDirectory() {
        return directory;
    }

    public String getCentralRepositoryUri() {
        return centralRepositoryUri;
    }

    public void setCentralRepositoryUri(String centralRepositoryUri) {
        this.centralRepositoryUri = centralRepositoryUri;
    }

    public void setProvisionerUri(String provisionerUri) {
        this.provisionerUri = provisionerUri;
    }

    public String getProvisionerUri() {
        return provisionerUri;
    }

    public void setRepositories(List<MeshRepository> repositories) {
        this.repositories = repositories;
    }

}