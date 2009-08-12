/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.cloudlaunch.distribution;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.cloudlaunch.distribution.Distributor.DistributionRef;

/**
 * Exporter
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class Exporter {

    Log log = LogFactory.getLog(this.getClass());

    Distributor distributor;
    private Distributable source;
    private String path;
    private DistributionRef<Distributable> ref;

    private boolean sequential = true;

    public void export() throws Exception {
        if (ref == null) {
            if (this.path == null) {
                ref = distributor.export(source);
                log.trace("Exported:" + source);
            } else {
                ref = distributor.register(source, path, true);
                log.trace("Registered as: " + ref.getPath() + " implementing: " + Arrays.asList(ref.getStub().getClass().getInterfaces()));
            }
        }
    }

    public void destroy() throws Exception {
        if (ref != null) {
            distributor.unregister(source);
        }
    }

    public void setDistributor(Distributor distributor) {
        this.distributor = distributor;
    }

    public Distributor getRegistry() {
        return distributor;
    }

    public Distributable getSource() {
        return source;
    }

    public void setSource(Distributable source) {
        this.source = source;
    }

    public Distributable getStub() {
        if (ref != null) {
            return ref.getStub();
        }
        return null;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSequential(boolean sequential) {
        this.sequential = sequential;
    }

    public boolean getSequential() {
        return this.sequential;
    }

}