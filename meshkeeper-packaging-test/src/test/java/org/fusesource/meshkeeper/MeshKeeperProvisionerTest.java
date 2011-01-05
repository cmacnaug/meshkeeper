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
package org.fusesource.meshkeeper;

import java.io.File;

import org.fusesource.meshkeeper.control.ControlServer;
import org.fusesource.meshkeeper.distribution.provisioner.Main;
import org.fusesource.meshkeeper.distribution.provisioner.Provisioner;
import org.fusesource.meshkeeper.distribution.provisioner.ProvisionerFactory;
import org.fusesource.meshkeeper.distribution.provisioner.Provisioner.MeshProvisioningException;
import org.fusesource.meshkeeper.util.internal.FileSupport;

import junit.framework.TestCase;

/**
 * MeshKeeperProvisionerTest
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class MeshKeeperProvisionerTest extends TestCase {

    public void testEmbeddedProvisionerInstantiation() throws Exception {
        Provisioner provisioner = new ProvisionerFactory().create("embedded");
        assertNotNull(provisioner);
    }

    public void testEmbeddedControlServerDiscoverable() throws Exception {
        File dataDir = MavenTestSupport.getDataDirectory(this.getClass().getSimpleName());
        ControlServer server = MeshKeeperFactory.createControlServer(ControlServer.DEFAULT_REGISTRY_URI, dataDir.getCanonicalFile());
        
        Provisioner provisioner = new ProvisionerFactory().create("embedded");
        provisioner.setDeploymentUri(dataDir.getPath());
        try {
            assertEquals(server.getRegistryUri(), provisioner.findMeshRegistryUri());
        } finally {
            //Destroy Server we should no longer be able to find it:
            server.destroy();
        }

        MeshProvisioningException expected = null;
        try {
            provisioner.findMeshRegistryUri();
        } catch (MeshProvisioningException e) {
            expected = e;
        }

        assertNotNull(expected);
    }
    
    
    public void testSpawnedControlServerDiscoverable() throws Exception {
      File dataDir = MavenTestSupport.getDataDirectory(this.getClass().getSimpleName());
      dataDir = new File(dataDir + File.separator + "spawnTest");
      FileSupport.recursiveDelete(dataDir);
      
      Provisioner provisioner = new ProvisionerFactory().create("spawn:" + dataDir.toURI());
      
      
      try {
        provisioner.deploy();
        assertTrue("Provisioner did non deploy", provisioner.isDeployed());
        
        MeshKeeper mesh = MeshKeeperFactory.createMeshKeeper(provisioner.findMeshRegistryUri());
        mesh.destroy();
        
      } finally {
          //Destroy Server we should no longer be able to find it:
        provisioner.unDeploy(true);
      }

      MeshProvisioningException expected = null;
      try {
          provisioner.findMeshRegistryUri();
      } catch (MeshProvisioningException e) {
          expected = e;
      }

      assertNotNull(expected);
  }
    
    public void testSpawnedControlServerDiscoverableWithFileSpec() throws Exception {
      File dataDir = MavenTestSupport.getDataDirectory(this.getClass().getSimpleName());
      dataDir = new File(dataDir + File.separator + "spawnTest");
      FileSupport.recursiveDelete(dataDir);
      
      //As long as foward slashes are used shouldn't need to specify a uri:
      Provisioner provisioner = new ProvisionerFactory().create("spawn:" + dataDir.getCanonicalFile().toString().replace("\\", "/") + "?createWindow=false");
      
      try {
        provisioner.deploy();
        assertTrue("Provisioner did non deploy", provisioner.isDeployed());
        
        MeshKeeper mesh = MeshKeeperFactory.createMeshKeeper(provisioner.findMeshRegistryUri());
        mesh.destroy();
        
      } finally {
          //Destroy Server we should no longer be able to find it:
        provisioner.unDeploy(true);
      }

      MeshProvisioningException expected = null;
      try {
          provisioner.findMeshRegistryUri();
      } catch (MeshProvisioningException e) {
          expected = e;
      }

      assertNotNull(expected);
  }

  public void testProvisionerMain() throws Exception {
    File dataDir = MavenTestSupport.getDataDirectory(this.getClass().getSimpleName());
    dataDir = new File(dataDir + File.separator + "testProvisionerMain");
    FileSupport.recursiveDelete(dataDir);
    
    String uri = "spawn:" + dataDir.getCanonicalFile().toString().replace("\\", "/") + "?createWindow=true";
    
    Main.main(new String [] {"-a", "deploy", "-u", uri});
    Provisioner provisioner = new ProvisionerFactory().create( uri);
    try {
      //Create a provisioner to find the 
      MeshKeeper mesh = MeshKeeperFactory.createMeshKeeper(provisioner.findMeshRegistryUri());
      mesh.destroy();
      
      //Make sure we don't double deploy:
      Main.main(new String [] {"-a", "deploy", "-u", uri});
    }
    finally {
      Main.main(new String [] {"-a", "undeploy", "-u", uri});
    }
    
    MeshProvisioningException expected = null;
    try {
        provisioner.findMeshRegistryUri();
    } catch (MeshProvisioningException e) {
        expected = e;
    }

    assertNotNull(expected);
    
    
  }
    
    // public void testCloudmixProvisionerInstantiation() throws Exception {
    //     Provisioner provisioner = new ProvisionerFactory().create("cloudmix:http://localhost:8181");
    //     assertNotNull(provisioner);
    //     assertEquals("http://localhost:8181", provisioner.getDeploymentUri());
    // }
}
