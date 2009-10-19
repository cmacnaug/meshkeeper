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
package org.fusesource.meshkeeper.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.meshkeeper.Distributable;
import org.fusesource.meshkeeper.MeshKeeper;
import org.fusesource.meshkeeper.MeshProcess;
import org.fusesource.meshkeeper.MeshProcessListener;

import java.io.Serializable;
import java.io.ObjectStreamException;

/**
 * DefaultProcessListener
 * <p>
 * A default process listener. Applications can subclass this to customize
 * behavior
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class DefaultProcessListener implements MeshProcessListener, Serializable {

    Log LOG = LogFactory.getLog(DefaultProcessListener.class);
    protected String name = "";
    private Distributable proxy;

    public DefaultProcessListener(MeshKeeper meshKeeper) throws Exception {
        proxy = meshKeeper.remoting().export(this);
    }

    // When this object is serialized, we serialize the remote proxy.
    protected Object writeReplace() throws ObjectStreamException {
        return proxy;
    }

    public DefaultProcessListener(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.fusesource.meshkeeper.ProcessListener#onProcessError(java.lang.Throwable
     * )
     */
    public void onProcessError(Throwable thrown) {
        LOG.error(format("ERROR: " + thrown));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.meshkeeper.ProcessListener#onProcessExit(int)
     */
    public void onProcessExit(int exitCode) {
        LOG.info(format("exited with " + exitCode));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.fusesource.meshkeeper.ProcessListener#onProcessInfo(java.lang.String
     * )
     */
    public void onProcessInfo(String message) {
        LOG.info(format(message));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.meshkeeper.ProcessListener#onProcessOutput(int,
     * byte[])
     */
    public void onProcessOutput(int fd, byte[] output) {
        if (fd == MeshProcess.FD_STD_ERR) {
            LOG.error(format(new String(output)));
        } else {
            LOG.info(format(new String(output)));
        }
    }

    private String format(String s) {
        if (s.endsWith("\r\n")) {
            s = s.substring(0, s.length() - 2);
        } else if (s.endsWith("\n")) {
            s = s.substring(0, s.length() - 1);
        }

        if (name == null) {
            return s;
        } else {
            return name + ": " + s;
        }
    }

    public String toString() {
        return name;
    }

}
