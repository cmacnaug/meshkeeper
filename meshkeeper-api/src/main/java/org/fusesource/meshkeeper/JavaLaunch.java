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
package org.fusesource.meshkeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Properties;

import static org.fusesource.meshkeeper.Expression.*;

/**
 * JavaLaunch
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class JavaLaunch {

    private Expression jvm = string("java");
    Expression classpath;
    FileExpression workingDir;
    Expression mainClass;
    ArrayList<Expression> jvmArgs = new ArrayList<Expression>();
    ArrayList<Expression> args = new ArrayList<Expression>();
    ArrayList<Expression> systemProperties = new ArrayList<Expression>();
    String bootStrapClassLoaderFactoryPath;

    public Expression getJvm() {
        return jvm;
    }

    public void setJvm(String jvm) {
        this.jvm = string(jvm);
    }

    public void setJvm(Expression jvm) {
        this.jvm = jvm;
    }

    public List<Expression> getJvmArgs() {
        return jvmArgs;
    }

    public JavaLaunch addJvmArgs(String... args) {
        return addJvmArgs(string(args));
    }

    public JavaLaunch addJvmArgs(Expression... args) {
        return addJvmArgs(Arrays.asList(args));
    }

    public JavaLaunch addJvmArgs(List<Expression> args) {
        this.jvmArgs.addAll(args);
        return this;
    }

    public JavaLaunch addSystemProperty(String key, String value) {
        systemProperties.add(string("-D" + key + "=" + value));
        return this;
    }

    public JavaLaunch addSystemProperty(Expression key, Expression value) {
        systemProperties.add(append(string("-D"), key, string("="), value));
        return this;
    }

    public JavaLaunch propagateSystemProperties(Properties sourceProps, String... names) {
        for (String name : names) {
            if (sourceProps.getProperty(name) != null) {
                addSystemProperty(name, sourceProps.getProperty(name));
            }
        }
        return this;
    }

    public Expression getClasspath() {
        return classpath;
    }

    public void setBootstrapClassLoaderFactory(String bootStrapClassLoaderFactoryPath) {
        this.bootStrapClassLoaderFactoryPath = bootStrapClassLoaderFactoryPath;
    }

    public void setClasspath(Expression classpath) {
        this.classpath = classpath;
    }

    public void setClasspath(FileExpression... classpath) {
        this.classpath = path(classpath);
    }

    public void setClasspath(String... classpath) {
        this.classpath = path(file(classpath));
    }

    public FileExpression getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = file(workingDir);
    }

    public void setWorkingDir(FileExpression workingDir) {
        this.workingDir = workingDir;
    }

    public Expression getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = string(mainClass);
    }

    public void setMainClass(Expression mainClass) {
        this.mainClass = mainClass;
    }

    public ArrayList<Expression> args() {
        return args;
    }

    public JavaLaunch addArgs(String... args) {
        return addArgs(string(args));
    }

    public JavaLaunch addArgs(Expression... args) {
        return addArgs(Arrays.asList(args));
    }

    public JavaLaunch addArgs(List<Expression> args) {
        this.args.addAll(args);
        return this;
    }

    public LaunchDescription toLaunchDescription() {
        LaunchDescription ld = new LaunchDescription();
        ld.setWorkingDirectory(workingDir);
        ld.add(jvm);
        ld.add(jvmArgs);
        ld.add(systemProperties);
        if (classpath != null || bootStrapClassLoaderFactoryPath != null) {
            ld.add(string("-cp"));
            Expression launchClasspath = null;
            if (bootStrapClassLoaderFactoryPath != null) {
                ld.addPreLaunchTask(new LaunchDescription.BootstrapClassPathTask(bootStrapClassLoaderFactoryPath));
                launchClasspath = file(property(LaunchDescription.BootstrapClassPathTask.BOOTSTRAP_CP_PROPERTY, string("")));
            }
            if (classpath != null) {
                if (launchClasspath == null) {
                    launchClasspath = classpath;
                } else {
                    launchClasspath = path(file(launchClasspath), file(classpath));
                }
            }
            ld.add(launchClasspath);
        }
        
        ld.add(mainClass);
        ld.add(args);
        return ld;
    }
}
