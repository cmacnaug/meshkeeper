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

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.meshkeeper.MeshProcess;
import org.fusesource.meshkeeper.MeshProcessListener;

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
public class DefaultProcessListener implements MeshProcessListener {

    Log LOG = LogFactory.getLog(DefaultProcessListener.class);
    protected String name = "";

    protected LinkedList<MeshProcessListener> delegates;
    protected boolean prefixEachLine = true;
    
    protected final CountDownLatch exitLatch = new CountDownLatch(1);
    protected final AtomicInteger exitCode = new AtomicInteger();

    public DefaultProcessListener(String name) {
        this.name = name;
    }

    /**
     * Adds a delegate listener which will be called by this listener when
     * output is received.
     * 
     * @param listener
     *            The delegate listener:
     */
    public synchronized void addDelegate(MeshProcessListener listener) {
        if (delegates == null) {
            delegates = new LinkedList<MeshProcessListener>();
        }

        if (!delegates.contains(listener)) {
            delegates.add(listener);
        }
    }

    /**
     * Removes a delegate listener from this listener.
     * 
     * @param listener
     *            The delegate listener:
     * 
     * @return true if the listener was in the list
     */
    public synchronized boolean removeDelegate(MeshProcessListener listener) {
        if (delegates == null) {
            return false;
        }

        return delegates.remove(listener);
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
        if (delegates != null) {
            for (MeshProcessListener listener : delegates) {
                listener.onProcessError(thrown);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.meshkeeper.ProcessListener#onProcessExit(int)
     */
    public void onProcessExit(int exitCode) {
        this.exitCode.set(exitCode);
        try {
            LOG.info(format("exited with " + exitCode));
            if (delegates != null) {
                for (MeshProcessListener listener : delegates) {
                    listener.onProcessExit(exitCode);
                }
            }
        } finally {
            exitLatch.countDown();
        }
    }
    
    public int waitForExit() throws InterruptedException {
        exitLatch.await();
        return exitCode.get();
    }

    /**
     * Tests if the process has reported an exit status yet. 
     * 
     * @return True if if the process hasn't reported an exit status yet. 
     */
    public boolean isRunning() {
        return exitLatch.getCount() > 0;
    }
    
    public int waitForExit(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        if( exitLatch.await(timeout, unit) )
            return exitCode.get();
        throw new TimeoutException();
    }

    /**
     * 
     * @return 
     *              The exit code of the remote process
     * @throws IllegalStateException
     *              If the 
     */
    public int exitCode() throws IllegalStateException {
        if( exitLatch.getCount()==0 )
            return exitCode.get();
        throw new IllegalStateException();
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
        if (delegates != null) {
            for (MeshProcessListener listener : delegates) {
                listener.onProcessInfo(message);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.meshkeeper.ProcessListener#onProcessOutput(int,
     * byte[])
     */
    public void onProcessOutput(int fd, byte[] output) {
        if (prefixEachLine) {
            BufferedReader reader = new BufferedReader(new StringReader(new String(output)));

            try {
                String line = reader.readLine();
                while (line != null) {
                    if (fd == MeshProcess.FD_STD_ERR) {
                        LOG.error(prefix(line));
                    } else {
                        LOG.info(prefix(line));
                    }
                    line = reader.readLine();
                    ;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (fd == MeshProcess.FD_STD_ERR) {
                LOG.error(format(new String(output)));
            } else {
                LOG.info(format(new String(output)));
            }
        }

        if (delegates != null) {
            for (MeshProcessListener listener : delegates) {
                listener.onProcessOutput(fd, output);
            }
        }
    }

    private String format(String s) {
        if (s.endsWith("\r\n")) {
            s = s.substring(0, s.length() - 2);
        } else if (s.endsWith("\n")) {
            s = s.substring(0, s.length() - 1);
        }
        return prefix(s);
    }

    private String prefix(String s) {
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
