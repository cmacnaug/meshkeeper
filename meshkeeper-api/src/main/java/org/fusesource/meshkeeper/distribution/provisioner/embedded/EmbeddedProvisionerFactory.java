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

import org.fusesource.meshkeeper.distribution.provisioner.Provisioner;
import org.fusesource.meshkeeper.distribution.provisioner.ProvisionerFactory;

/**
 * EmbeddedProvisionerFactory
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class EmbeddedProvisionerFactory extends ProvisionerFactory {

    /*
     * (non-Javadoc)
     * 
     * @seeorg.fusesource.meshkeeper.distribution.registry.RegistryFactory#
     * createRegistry(java.lang.String)
     */
    @Override
    protected Provisioner createPlugin(String uri) throws Exception {
        EmbeddedProvisioner provisioner = new EmbeddedProvisioner();
        if (uri != null && uri.trim().length() > 0) {
            provisioner.setDeploymentUri(uri.trim());
        }
        
        return provisioner;
    }
}