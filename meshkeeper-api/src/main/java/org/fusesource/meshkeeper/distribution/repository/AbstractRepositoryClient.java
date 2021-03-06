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
package org.fusesource.meshkeeper.distribution.repository;

import java.io.File;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.fusesource.meshkeeper.AuthenticationInfo;
import org.fusesource.meshkeeper.MeshArtifact;
import org.fusesource.meshkeeper.MeshRepository;
import org.fusesource.meshkeeper.distribution.AbstractPluginClient;

/**
 * AbstractRepositoryClient
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public abstract class AbstractRepositoryClient extends AbstractPluginClient implements RepositoryClient {

    protected final String REPOSITORY_REGISTRY_PATH = "meshkeeper/repositories/";
    protected final HashMap<String, MeshRepository> repositories = new HashMap<String, MeshRepository>();

    /**
     * Creates a repository.
     * 
     * @param id
     * @param repositoryUri
     * @param isLocal
     *            indicates whether or not this repository is on the local
     *            machine's file system.
     * @param authenticationInfo
     *            authentication info for the repository.
     * @return
     * @throws URISyntaxException
     */
    public MeshRepository createRepository(String id, String repositoryUri, boolean isLocal, AuthenticationInfo authenticationInfo) throws URISyntaxException {
        return new RepositoryImpl(id, repositoryUri, isLocal, authenticationInfo);
    }

    /**
     * Adds a repository location, and registers it in the registry.
     * 
     * @param repository
     *            The repository to add.
     * @throws Exception
     * @throws Exception
     *             If there is an error adding the repository.
     */
    public synchronized void registerRepository(MeshRepository repository) throws Exception {
        repositories.put(repository.getRepositoryId(), repository);
        if (!repository.isLocal()) {
            getMeshKeeper().registry().addRegistryObject(REPOSITORY_REGISTRY_PATH + repository.getRepositoryId(), false, repository);
        }
    }

    /**
     * Removes a repository location, and removes it from the registry.
     * 
     * @param repositoryId
     *            The id of the repository.
     * @throws Exception
     *             If there is an error adding the repository.
     */
    public synchronized void unregisterRepository(String repositoryId) throws Exception {
        getMeshKeeper().registry().removeRegistryData(REPOSITORY_REGISTRY_PATH + repositoryId, false);
    }

    /**
     * Looks up a repository by it's id.
     * 
     * @param repositoryId
     *            The repository.
     * @return The repository or null if none is configured.
     * @throws Exception
     *             If there is an error looking up the repository
     */
    protected MeshRepository getRepository(String repositoryId) throws Exception {
        MeshRepository rc = repositories.get(repositoryId);
        if (rc == null) {
            rc = getMeshKeeper().registry().getRegistryObject(REPOSITORY_REGISTRY_PATH + repositoryId);
            if (rc != null) {
                repositories.put(rc.getRepositoryId(), rc);
            }
        }
        return rc;
    }

    public final MeshArtifact resolveArtifact(MeshArtifact artifact) throws Exception {
        return resolveArtifact(artifact, LOCAL_REPOSITORY_ID);
    }

    public final MeshArtifact resolveArtifact(MeshArtifact artifact, String repositoryId) throws Exception {
        MeshArtifact toResolve = createArtifact();
        toResolve.setRepositoryId(artifact.getRepositoryId());
        toResolve.setRepositoryPath(artifact.getRepositoryPath());
        toResolve.setType(MeshArtifact.FILE);

        toResolve = resolveOsMap(artifact, repositoryId);

        downloadArtifact(toResolve, repositoryId);
        artifact.setLocalPath(toResolve.getLocalPath());
        return artifact;
    }

    private MeshArtifact resolveOsMap(MeshArtifact resource, String repositoryId) throws Exception {
        String sourceDir = resource.getRepositoryPath();
        if (resource.getType() == MeshArtifact.DIRECTORY) {
            if (!sourceDir.endsWith("/")) {
                sourceDir = sourceDir + "/";
            }
        } else {
            sourceDir = new File(resource.getRepositoryPath()).getParent();
        }

        MeshArtifact osMap = createArtifact();
        osMap.setRepositoryId(resource.getRepositoryId());
        osMap.setRepositoryPath(sourceDir + "os.map");
        osMap.setType(MeshArtifact.FILE);

        try {
            osMap = downloadArtifact(osMap, repositoryId);
        } catch (Exception e) {
            // No os map or couldn't find one, just return the original
            // artifact.
            return resource;
        }

        String mapped = OsMapper.map(sourceDir, new File(osMap.getLocalPath()));
        if (mapped != null) {
            resource.setRepositoryPath(sourceDir + mapped);
        }

        return resource;
    }

    /**
     * Subclasses should implement this method to download an artifact to the
     * given localRepositoryId.
     * 
     * @param resource
     *            The resource to resolve
     * @param localRepositoryId
     *            The target repository id.
     * @throws Exception
     *             if there is an error downloading the artifact.
     * @return The resolved artifact
     */
    protected abstract MeshArtifact downloadArtifact(MeshArtifact resource, String localRepositoryId) throws Exception;

}
