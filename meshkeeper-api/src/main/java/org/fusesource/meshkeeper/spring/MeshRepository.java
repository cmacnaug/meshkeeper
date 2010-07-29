package org.fusesource.meshkeeper.spring;

import java.io.File;

import org.fusesource.meshkeeper.AuthenticationInfo;
import org.springframework.beans.factory.InitializingBean;

public class MeshRepository implements org.fusesource.meshkeeper.MeshRepository, InitializingBean {

    private static final long serialVersionUID = 1L;
    
    private File baseDirectory;
    private String repositoryId;
    private String repositoryUri;
    private boolean isLocal;
    private AuthenticationInfo authenticationInfo;
    
    public void afterPropertiesSet()
    {
        if(isLocal) {
            baseDirectory = new File(repositoryUri);
        }
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getRepositoryUri() {
        return repositoryUri;
    }

    public void setRepositoryUri(String repositoryUri) {
        this.repositoryUri = repositoryUri;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean isLocal) {
        this.isLocal = isLocal;
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    public void setAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        this.authenticationInfo = authenticationInfo;
    }
}
