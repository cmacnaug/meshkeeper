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
package org.fusesource.meshkeeper.distribution.event.vm;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

import org.fusesource.meshkeeper.MeshEvent;
import org.fusesource.meshkeeper.MeshEventListener;
import org.fusesource.meshkeeper.distribution.event.AbstractEventClient;
import org.fusesource.meshkeeper.distribution.event.EventClient;

/**
 * VMEventClient
 * <p>
 * Description: An in VM implementation of an EventClient
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class VMEventClient extends AbstractEventClient implements EventClient {

    private static final VMEventServer server = new VMEventServer();
    private final HashMap<String, HashSet<MeshEventListener>> eventListeners = new HashMap<String, HashSet<MeshEventListener>>();

    private boolean closed = false;

    public void start() {
        //No-Op
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.fusesource.meshkeeper.distribution.event.EventClient#close()
     */
    public void destroy() throws Exception {
        synchronized (this) {
            closed = true;
        }

        for (Map.Entry<String, HashSet<MeshEventListener>> entry : eventListeners.entrySet()) {
            for (MeshEventListener l : entry.getValue()) {
                server.closeEventListener(l, entry.getKey());
            }
        }
        eventListeners.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.fusesource.meshkeeper.distribution.event.EventClient#closeEventListener
     * (org.fusesource.meshkeeper.distribution.event.EventListener,
     * java.lang.String)
     */
    public void closeEventListener(MeshEventListener listener, String topic) throws Exception {
        boolean removed = false;
        synchronized (this) {
            checkClosed();
            HashSet<MeshEventListener> listeners = eventListeners.get(topic);
            if (listeners != null) {
                if (listeners.remove(listener)) {
                    removed = true;
                }
                if (listeners.isEmpty()) {
                    eventListeners.remove(topic);
                }
            }
        }
        if (removed) {
            server.closeEventListener(listener, topic);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.fusesource.meshkeeper.distribution.event.EventClient#openEventListener
     * (org.fusesource.meshkeeper.distribution.event.EventListener,
     * java.lang.String)
     */
    public void openEventListener(MeshEventListener listener, String topic) throws Exception {
        boolean added = false;
        synchronized (this) {
            checkClosed();
            HashSet<MeshEventListener> listeners = eventListeners.get(topic);
            if (listeners == null) {
                listeners = new HashSet<MeshEventListener>(1);
                eventListeners.put(topic, listeners);
            }
            if (listeners.add(listener)) {
                added = true;
            }
        }

        if (added) {
            server.openEventListener(listener, topic);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.fusesource.meshkeeper.distribution.event.EventClient#sendEvent(org
     * .fusesource.meshkeeper.distribution.event.Event, java.lang.String)
     */
    public void sendEvent(final MeshEvent event, String topic) throws Exception {
        checkClosed();
        server.sendEvent(event, topic);
    }

    private synchronized void checkClosed() {
        if (closed) {
            throw new IllegalStateException("closed");
        }
    }

}
