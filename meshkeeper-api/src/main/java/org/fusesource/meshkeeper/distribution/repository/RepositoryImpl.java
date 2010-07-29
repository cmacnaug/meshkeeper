package org.fusesource.meshkeeper.distribution.repository;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.fusesource.meshkeeper.AuthenticationInfo;
import org.fusesource.meshkeeper.MeshRepository;

public class RepositoryImpl implements MeshRepository {

    private static final long serialVersionUID = 1L;
    private final String id;
    private final String repositoryUri;
    private final boolean isLocal;
    private final AuthenticationInfo authenticationInfo;
    private transient File baseDir = null;
    
    RepositoryImpl(String id, String repositoryUri, boolean isLocal, AuthenticationInfo authenticationInfo) throws URISyntaxException {
        this.id = id;
        this.repositoryUri = repositoryUri;
        this.authenticationInfo = authenticationInfo;
        this.isLocal = isLocal;
        if(isLocal) {
            baseDir = new File(new URI(repositoryUri).getSchemeSpecificPart());
        }
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    public String getRepositoryId() {
        return id;
    }

    public String getRepositoryUri() {
        return repositoryUri;
    }
    
    public boolean isLocal()
    {
        return isLocal;
    }

    public File getBaseDirectory() {
        return baseDir;
    }

}
