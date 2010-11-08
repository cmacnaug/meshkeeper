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
package org.fusesource.meshkeeper.distribution.provisioner.embedded;

import java.io.File;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.meshkeeper.distribution.provisioner.Provisioner;

public class SpawnProvisionerFactory extends EmbeddedProvisionerFactory {
    
    private static Log LOG = LogFactory.getLog(SpawnProvisionerFactory.class);
    
    /*
     * (non-Javadoc)
     * 
     * @seeorg.fusesource.meshkeeper.distribution.registry.RegistryFactory#
     * createRegistry(java.lang.String)
     */
    @Override
    protected Provisioner createPlugin(String uri) throws Exception {
        System.out.println(this.getClass().getName() + " createPlugin " + uri);
        
        EmbeddedProvisioner provisioner = new EmbeddedProvisioner();
        LOG.info("Creating spawn provisioner from " + uri);
        uri = super.applyQueryParameters(provisioner, new URI(uri)).toString().trim();
        
        //Treat the remaining portion if any as a the deployment uri (which be a uri
        //specifying the control server directory)
        if (uri.length() > 0) {
            if (uri != null && uri.trim().length() > 0) {
                provisioner.setDeploymentUri(new File(new URI(uri)).toString());
            }
        }
        //Make sure we're spawning:
        provisioner.setSpawn(true);
        
        LOG.info("Created spawn provisioner, control server directory is: " + provisioner.getControlServerDirectory());

        return provisioner;
    }
}
