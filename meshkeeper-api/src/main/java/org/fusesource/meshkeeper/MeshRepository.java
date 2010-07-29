package org.fusesource.meshkeeper;

import java.io.File;
import java.io.Serializable;

/**
 * Holds information about a repository for use with resolving {@link MeshArtifact}s
 * from a {@link MeshKeeper} #{@link Repository}. {@link MeshRepository} are created
 * via 
 * @author cmacnaug
 */
public interface MeshRepository extends Serializable{

    /**
     * Returns the authentication info for the repository.
     * @return
     */
    public AuthenticationInfo getAuthenticationInfo();

    /** 
     * @return The repository connect uri. 
     */
    public String getRepositoryUri();
    
    /**
     * Gets the identifier for the repository.
     * @return
     */
    public String getRepositoryId();
    
    /**
     * @return whether or not this is a local repository.
     */
    public boolean isLocal();
    
    /**
     * If this is a local repository this will return the base directory
     * of the local repository. 
     * @return The base directory of the repo or null if not local.
     */
    public File getBaseDirectory();
    
}
