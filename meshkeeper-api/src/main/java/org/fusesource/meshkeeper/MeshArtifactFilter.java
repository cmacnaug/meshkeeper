package org.fusesource.meshkeeper;

public interface MeshArtifactFilter {

  public boolean include(MeshArtifact artifact);
}
