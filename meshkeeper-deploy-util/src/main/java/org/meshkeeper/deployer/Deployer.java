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
package org.meshkeeper.deployer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.fusesource.meshkeeper.MeshKeeper;
import org.fusesource.meshkeeper.MeshKeeperFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class Deployer implements InitializingBean, DisposableBean {

  private String homeDir = System.getProperty("user.dir");
  private static Log LOG = LogFactory.getLog(Deployer.class);
  private ClassPathXmlApplicationContext context;
  private String configuration = homeDir + "/configs/" + System.getProperty("deployment.config", "default.xml");
  private Deployment deployment;
  private static Deployer sandbox;
  private AtomicBoolean started = new AtomicBoolean(false);
  private AtomicBoolean destroyed = new AtomicBoolean(false);
  private static final CountDownLatch running = new CountDownLatch(1);
  
  //Discipline 
  public void afterPropertiesSet() throws Exception {
    //Make sure default properties file exists (users can store their properties
    //here:
    File myProps = new File(homeDir + "/deployment/my.properties");
    if(!myProps.exists()) {
      myProps.getParentFile().mkdirs();
      Properties props = new Properties();
      props.store(new FileOutputStream(myProps), "Place your deployment properties overrides here");
    }
    
    context = new ClassPathXmlApplicationContext(configuration);
    setDeployment((Deployment) context.getBean("deployment"));
    deployment.initialize();

    
    Deployer.sandbox = this;
  }

  public void destroy() throws Exception {
    shutdown();
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  public static final Deployer getSandbox() {
    try {
      if (sandbox == null) {
        Deployer sb = new Deployer();
        sb.afterPropertiesSet();
      }

      if (!sandbox.destroyed.get() && !sandbox.started.get() ) {
        sandbox.startup();
      }
    } catch (Exception e) {
      LOG.warn("Sandbox startup failed: " + e.getMessage(), e);
      throw new IllegalStateException("Sandbox startup failed", e);
    }

    return sandbox;
  }

  public void setDeployment(Deployment configuration) {
    this.deployment = configuration;
  }

  public Deployment getDeployment() {
    return deployment;
  }

  public void startup() throws Exception {
    if (started.compareAndSet(false, true)) {
      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

        public void run() {
          try {
            Deployer.getSandbox().shutdown();
          } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }

      }));
      deployment.launch();
    }
  }

  public void shutdown() throws Exception {
    destroyed.set(true);
    if (started.compareAndSet(true, false)) {
      deployment.shutdown();
      if(context != null) {
        context.destroy();
      }
      running.countDown();
    }
  }

  public static final void main(String[] args) throws Exception {
    final Deployer sb = new Deployer();
    if (args.length > 0) {
      sb.setConfiguration("com/savvion/webbpa/sandbox/" + args[0]);
    }

    sb.afterPropertiesSet();
    try
    {
      sb.startup();
    }
    catch(Exception e)
    {
      sb.destroy();
      throw e;
    }
    running.await();
  }
  
  public MeshKeeper createMeshKeeper() throws Exception {
    return MeshKeeperFactory.createMeshKeeper();
  }


}
