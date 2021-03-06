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
package org.fusesource.meshkeeper.packaging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;

import org.fusesource.meshkeeper.MavenTestSupport;
import org.fusesource.meshkeeper.MeshContainer;
import org.fusesource.meshkeeper.MeshKeeper;

/**
 * RemotingTest
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class RemotingTest extends TestCase {

    MeshKeeper meshKeeper;

    protected void setUp() throws Exception {
        meshKeeper = MavenTestSupport.createMeshKeeper(getClass().getSimpleName());
    }

    protected void tearDown() throws Exception {
        if (meshKeeper != null) {
            meshKeeper.destroy();
            meshKeeper = null;
        }
    }

    private String getAgent() throws InterruptedException, TimeoutException {
        meshKeeper.launcher().waitForAvailableAgents(5000);
        return meshKeeper.launcher().getAvailableAgents()[0].getAgentId();
    }

    public static class RemotingTestObject implements Serializable {
        private static final long serialVersionUID = 1L;
        public String foreignJarPath;

        public void triggerForeignException() throws Exception {
            Object foreignObject = null;
            Method foreignMethod = null;
            try {
                File f = new File(foreignJarPath);
                if (!f.exists()) {
                    throw new FileNotFoundException(f.getCanonicalPath());
                }
                System.out.println("Creating foreign classloader: " + f);
                URLClassLoader cl = new URLClassLoader(new URL[] { f.toURL() });
                Class<?> foreign = cl.loadClass("org.fusesource.meshkeeper.packaging.ForeignClass");
                foreignObject = foreign.newInstance();
                foreignMethod = foreign.getMethod("throwCustomForeignException");
            } catch (Exception e) {
                throw new RemotingTestException("Error loading foreign class", e);
            }

            //Should throw an exception:
            foreignMethod.invoke(foreignObject);

            //No exception thrown
            throw new Exception("Foreign Exception not thrown");

        }
        
        
    }

    public static class RemotingTestException extends Exception {
        private static final long serialVersionUID = 1L;
        RemotingTestException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public void testUnknownExceptionFails() throws Exception {
        //Test that the the failure message is clear when an exception is thrown to a caller that doesn't have it on their classpath:
        RemotingTestObject object = new RemotingTestObject();
        object.foreignJarPath = MavenTestSupport.getBaseDirectory() + File.separator + "testing-resources" + File.separator
                + "foreign.jar";

        MeshContainer container = meshKeeper.launcher().launchMeshContainer(getAgent());

        RemotingTestObject proxy = (RemotingTestObject) container.host("RemotingTestObject", object);

        try {
            proxy.triggerForeignException();
        } catch (RemotingTestException rte) {
            throw rte;
        } catch (Exception e) {
            System.out.println("Testing expected exception: ");
            e.printStackTrace();
            
            Throwable cause = e;
            while (cause != null) {
                if (cause instanceof ClassNotFoundException) {
                    break;
                }
                cause = cause.getCause();
            }

            if (cause == null) {
                throw new RemotingTestException("Caught exception doesn't have ClassNotFoundException as a cause", e);
            }
        }
    }

}
