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
package org.meshkeeper.deployer.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.fusesource.meshkeeper.Expression;
import org.fusesource.meshkeeper.JavaLaunch;
import org.fusesource.meshkeeper.MeshKeeper;
import org.fusesource.meshkeeper.MeshProcess;
import static org.fusesource.meshkeeper.Expression.*;
import org.fusesource.meshkeeper.Expression.FileExpression;
import org.fusesource.meshkeeper.util.DefaultProcessListener;

public class AntRunner {

  private MeshKeeper meshKeeper;
  private FileExpression workingDir;
  private FileExpression buildFile;
  private HashMap<Expression, Expression> jvmArgs = new HashMap<Expression, Expression>();
  LinkedList<Expression> args = new LinkedList<Expression>();
  private String name = "AntRunner";
  private Expression jvm = string("java");

  public AntRunner() {

  }

  public int runAnt(String agentId, String... antArgs) throws Exception {
    JavaLaunch ant = meshKeeper.launcher().createJavaLaunch(
        org.apache.tools.ant.Main.class.getName());

    ant.setJvm(jvm);
    ant.setBootstrapClassLoaderFactory(meshKeeper.launcher().getBootstrapClassLoaderFactory()
        .getRegistryPath());
    for (Entry<Expression, Expression> arg : jvmArgs.entrySet()) {
      ant.addSystemProperty(arg.getKey(), arg.getValue());
    }
    ant.addArgs("-buildfile");
    ant.addArgs(buildFile);
    ant.addArgs(this.args);
    ant.addArgs(antArgs);
    ant.setWorkingDir(workingDir);

    DefaultProcessListener listener = new DefaultProcessListener(name);
    MeshProcess proc = meshKeeper.launcher().launchProcess(agentId, ant.toLaunchDescription(),
        listener);
    try {
      listener.waitForExit(10 * 60, TimeUnit.SECONDS);
      return listener.exitCode();
    } catch (Exception e) {
      proc.kill();
      throw e;
    }
  }

  public MeshKeeper getMeshKeeper() {
    return meshKeeper;
  }

  public void setMeshKeeper(MeshKeeper meshKeeper) {
    this.meshKeeper = meshKeeper;
  }

  public FileExpression getWorkingDir() {
    return workingDir;
  }

  public void setWorkingDir(FileExpression workingDir) {
    this.workingDir = workingDir;
  }

  public FileExpression getBuildFile() {
    return buildFile;
  }

  public void setBuildFile(FileExpression buildFile) {
    this.buildFile = buildFile;
  }

  public void addJVMArg(String key, String value) {
    jvmArgs.put(string(key), string(value));
  }

  public void addJVMArg(String key, Expression value) {
    jvmArgs.put(string(key), value);
  }

  public void addJVMArg(Expression key, Expression value) {
    jvmArgs.put(key, value);
  }

  public void addArgs(String... antArgs) {
    for (String arg : antArgs) {
      this.args.add(string(arg));
    }
  }
  
  public void addArgs(Expression ... antArgs) {
    for (Expression arg : antArgs) {
      this.args.add(arg);
    }
  }

  public void setName(String name) {
    this.name  = name;
  }

  public void setJvm(Expression jvm) {
    this.jvm = jvm;
  }

}
