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
package org.fusesource.meshkeeper.distribution.provisioner;

/**
 * MeshProvisioner
 * <p>
 * Interface used for deploying a meshkeeper control server and agents.
 * 
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public interface Provisioner {

    public static final String MESHKEEPER_PROVISIONER_ID_PROPERTY = "meshkeeper.provisionerId";
    
    /**
     * MeshProvisioningException
     * <p>
     * Description:
     * </p>
     * 
     * @author cmacnaug
     * @version 1.0
     */
    public static class MeshProvisioningException extends Exception {
        private static final long serialVersionUID = 1L;

        public MeshProvisioningException(String reason) {
            super(reason);
        }

        public MeshProvisioningException(String reason, Throwable cause) {
            super(reason, cause);
        }
    }
    
    /**
     * 
     * @return The time allows to wait for each provisioned component 
     * to come online.
     */
    public long getProvisioningTimeout();

    /**
     * sets the time allows to wait for each provisioned component 
     * to come online.
     * @param provisioningTimeout the time allows to wait for each provisioned component 
     * to come online.
     */
    public void setProvisioningTimeout(long provisioningTimeout);
    
    /**
     * This can be used to specify a specific port on which the registry
     * service should listen. 
     * 
     * @param port The port on which the registry server should listen
     */
    public void setRegistryPort(int port);

    /**
     * Sets the deployment uri. This is implementation specific, and and may be
     * used to connect to a remote provisioning agent.
     * 
     * @param agentHosts
     *            The deployment uri.
     */
    public void setDeploymentUri(String uri);

    /**
     * Gets the deployment Uri.
     */
    public String getDeploymentUri();

    /**
     * Set the preferred hostname on which to deploy the MeshKeeper controller.
     * 
     * @param the
     *            preferred control agent hostname
     */
    public void setPreferredControlHost(String preferredControlHost);

    /**
     * @return the preferred hostname on which to deploy the MeshKeeper
     *         controller.
     */
    public String getPreferredControlHost();

    /**
     * Sets the requested set of hostnames on which to deploy a meshkeeper
     * agent.
     * 
     * @param agentHosts
     *            the requested set of hostnames on which to deploy a meshkeeper
     *            agent.
     */
    public void setRequestedAgentHosts(String[] agentHosts);

    /**
     * Sets the request set of hostnames on which to deploy a meshkeeper agent.
     * 
     * @param agentHosts
     *            the requested set of hostnames on which to deploy a meshkeeper
     *            agent
     */
    public String[] getRequestedAgentHosts();

    /**
     * Sets the maximum number of agents to deploy. If set to -1 no limit is
     * enforced and as many agents will be deployed as possible, unless hosts
     * are specified by {@link #setRequestedAgentHosts(String[])}
     * 
     * @param maxAgents
     *            Max agents to deploy.
     */
    public void setMaxAgents(int maxAgents);

    /**
     * Gets the maximum number of agents to deploy.
     * 
     * @param maxAgents
     */
    public int getMaxAgents();

    /**
     * Sets whether or not the agent should have exlusive access to the machine
     * where it is being deployed (if applicable to the deployment scheme).
     * 
     * @param val
     *            True if the agent should have exclusive ownership of the
     *            machine to which it is deployed
     */
    public void setAgentMachineOwnership(boolean machineOwnerShip);
    
    /**
     * @return Whether or not deployed agents will own their machines
     */
    public boolean getAgentMachineOwnership();

    /**
     * Find the registry connect uri which can be used to connect to the
     * provisionned meshkeeper controller.
     * 
     * @return the registry connect uri which can be used to connect to the
     *         provisionned meshkeeper controller
     * 
     * @throws MeshProvisioningException
     *             If there is an error determining the control uri.
     */
    public String findMeshRegistryUri() throws MeshProvisioningException;

    /**
     * Find the registry connect uri which can be used to connect to the
     * provisionned meshkeeper controller.
     * 
     * @return <code>true</code> if meshkeeper is deployed.
     * 
     * @throws MeshProvisioningException
     *             If there is an error determining the provisioning status
     */
    public boolean isDeployed() throws MeshProvisioningException;

    /**
     * Deploys a MeshKeeper controller and Agents.
     * 
     * @throws MeshProvisioningException
     *             If there is an error deploying meshkeeper.
     */
    public void deploy() throws MeshProvisioningException;

    /**
     * Undeploys meshkeeper.
     * 
     * @param force
     *            If true will undeploy meshkeeper even if it is in use.
     * 
     * @throws MeshProvisioningException
     *             If there is an error undeploying meshkeeper.
     */
    public void unDeploy(boolean force) throws MeshProvisioningException;

    /**
     * Undeploys and deploys meshkeeper.
     * 
     * @param force
     *            If true will undeploy meshkeeper even if it is in use.
     * 
     * @throws MeshProvisioningException
     *             If there is an error redeploying meshkeeper.
     */
    public void reDeploy(boolean force) throws MeshProvisioningException;

    /**
     * Gets a status summary of deployed MeshKeeper components.
     * 
     * @param buffer
     *            the buffer to dump the summary into.
     * 
     * @throws MeshProvisioningException
     *             If there is an error getting the status.
     */
    public StringBuffer getStatus(StringBuffer buffer) throws MeshProvisioningException;
}
