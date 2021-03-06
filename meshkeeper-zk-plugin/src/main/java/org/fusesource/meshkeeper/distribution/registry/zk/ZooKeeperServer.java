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
package org.fusesource.meshkeeper.distribution.registry.zk;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.server.NIOServerCnxn;
import org.apache.zookeeper.server.persistence.FileTxnLog;
import org.apache.zookeeper.server.persistence.FileTxnSnapLog;
import org.fusesource.meshkeeper.control.ControlService;
import org.fusesource.meshkeeper.distribution.registry.RegistryClient;

/**
 * @author chirino
 */
public class ZooKeeperServer implements ControlService {

  Log log = LogFactory.getLog(this.getClass());

  private int port = 2181;
  private String userid = "guest";
  private String password = "";
  private String directory = "zookeeper-server-data";
  private boolean purge;
  private String serviceUri;
  int tick = 2000;

  public int getTick() {
    return tick;
  }

  public void setTick(int tick) {
    this.tick = tick;
  }

  private NIOServerCnxn.Factory serverFactory;

  public void start() throws Exception {

    File file = new File(directory);

    boolean doPurge = purge;

    // NOTE: this doesn't always work, since zk hangs on to file locks
    // It aslo can cause ZK to fail with CRC exceptions when running in
    // embedded mode:
    //              
    // if (doPurge && file.exists()) {
    // log.debug("Attempting to delete zk data");
    // try {
    // FileSupport.recursiveDelete(file.getCanonicalPath());
    // doPurge = false;
    // file.mkdirs();
    // } catch (Exception e) {
    // log.debug("Error purging store (likely a benign zoo-keeper bug)", e);
    // }
    // }

    // Reduces startup time, and doesn't waste space:
    int preallocateSize = 1024;
    System.setProperty("zookeeper.preAllocSize", "" + preallocateSize);
    FileTxnLog.setPreallocSize(preallocateSize);
    log.debug("Preallocate Size: " + preallocateSize);

    org.apache.zookeeper.server.ZooKeeperServer zkServer = new org.apache.zookeeper.server.ZooKeeperServer();
    FileTxnSnapLog ftxn = new FileTxnSnapLog(file, file);

    zkServer.setTxnLogFactory(ftxn);
    zkServer.setTickTime(tick);
    // serverFactory = new NIOServerCnxn.Factory(port);
    serverFactory = new NIOServerCnxn.Factory(new InetSocketAddress(port));
    serverFactory.startup(zkServer);

    // InetAddress address = serverFactory.getLocalAddress().getAddress();
    String actualHost = InetAddress.getLocalHost().getCanonicalHostName();
    serviceUri = "zk:tcp://" + actualHost + ":" + zkServer.getClientPort();

    if (doPurge) {

      if (log.isDebugEnabled()) {
        log.debug("Purging registry");
      }
      RegistryClient zk = null;
      try {
        zk = new ZooKeeperFactory().createPlugin(getServiceUri());
        zk.removeRegistryData("/", true);
      } catch (Exception e) {
        log.warn("Error purging registry", e);
      } finally {
        zk.destroy();
      }
    }
  }

  public void destroy() throws Exception {
    if (serverFactory != null) {
      serverFactory.shutdown();
      org.apache.zookeeper.server.ZooKeeperServer zkServer = serverFactory.getZooKeeperServer();
      if (zkServer != null) {
        if (zkServer.isRunning()) {
          zkServer.shutdown();
        }
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("Destroyed");
    }
  }

  public String getUserid() {
    return userid;
  }

  public void setUserid(String userid) {
    this.userid = userid;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getDirectory() {
    return directory;
  }

  public void setDirectory(String directory) {
    this.directory = directory;
  }

  public boolean isPurge() {
    return purge;
  }

  public void setPurge(boolean purge) {
    this.purge = purge;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getName() {
    return toString();
  }

  public String toString() {
    return "Zoo Keeper Registry Server";
  }

  public String getServiceUri() {
    return serviceUri;
  }

}
