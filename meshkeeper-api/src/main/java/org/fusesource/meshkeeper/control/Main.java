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
package org.fusesource.meshkeeper.control;

import java.util.Arrays;
import java.util.LinkedList;

import org.fusesource.meshkeeper.MeshKeeperFactory;
import org.fusesource.meshkeeper.distribution.provisioner.Provisioner;

/**
 * Main
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class Main {

    public static String JMS_SWITCH = "--jms";
    public static String DIRECTORY_SWITCH = "--directory";
    public static String PROVISIONER_ID_SWITCH = "--provisionerId";
    public static String REPOSITORY_SWITCH = "--repository";
    public static String REGISTRY_SWITCH = "--registry";
    public static String START_EMBEDDED_AGENT = "--startLaunchAgent";
    
    private static final void showUsage() {
        System.out.println("Usage:");
        System.out.println("Args:");
        System.out.println("  -h, --help                  -- this message");
        System.out.println("  [--jms <uri>]               -- specifies listening address for jms.");
        System.out.println("  [--registry <uri>]          -- specifies listening address for the regsitry.");
        System.out.println("  [--directory <directory>]   -- specifies data directory used by control server.");
        System.out.println("  [--repository <uri>]        -- specifies a uri to a centralized repository.");
    }

    @SuppressWarnings("serial")
    static class UsageException extends Exception {
        UsageException(String message) {
            super(message);
        }
    }

    /*
     * public static void main()
     * 
     * Defines the entry point into this app.
     */
    public static void main(String[] args) {
        String jv = System.getProperty("java.version").substring(0, 3);
        if (jv.compareTo("1.5") < 0) {
            System.err.println("The Control Server requires jdk 1.5 or higher to run, the current java version is " + System.getProperty("java.version"));
            System.exit(-1);
            return;
        }

        if (System.getProperty("meshkeeper.application") == null) {
            System.setProperty("meshkeeper.application", Main.class.getName());
        }

        boolean startEmbeddedAgent = false;
        String jms = ControlServer.DEFAULT_JMS_URI;
        String registry = ControlServer.DEFAULT_REGISTRY_URI;
        String repository = null;
        String directory = MeshKeeperFactory.getDefaultServerDirectory().getPath();
        LinkedList<String> alist = new LinkedList<String>(Arrays.asList(args));

        try {
            while (!alist.isEmpty()) {
                String arg = alist.removeFirst();
                if (arg.equals("--help") || arg.equals("-h")) {
                    showUsage();
                    return;
                } else if (arg.equals(JMS_SWITCH)) {
                    assertHasAdditionalArg(alist, "Expected uri after " + JMS_SWITCH);
                    jms = alist.removeFirst();
                } else if (arg.equals(DIRECTORY_SWITCH)) {
                    assertHasAdditionalArg(alist, "Directory expected after " + DIRECTORY_SWITCH);
                    directory = alist.removeFirst();
                } else if (arg.equals(REGISTRY_SWITCH)) {
                    String message = "Expected uri after " + REGISTRY_SWITCH;
                    assertHasAdditionalArg(alist, message);
                    registry = alist.removeFirst();
                } else if (arg.equals(REPOSITORY_SWITCH)) {
                    assertHasAdditionalArg(alist, "Expected url after " + REPOSITORY_SWITCH);
                    repository = alist.removeFirst();
                } else if (arg.equals(PROVISIONER_ID_SWITCH)) {
                    assertHasAdditionalArg(alist, "Expected provisionerId after " + PROVISIONER_ID_SWITCH);
                    System.setProperty(Provisioner.MESHKEEPER_PROVISIONER_ID_PROPERTY, alist.removeFirst());
                } else if (arg.equals(START_EMBEDDED_AGENT)) {
                    startEmbeddedAgent = true;
                } else {
                    throw new UsageException("Unexpected argument: " + arg);
                }
            }

        } catch (UsageException e) {
            System.out.println("Invalid usage: " + e.getMessage());
            System.out.println();
            showUsage();
            System.exit(-1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-2);
        }

        try {
            ControlServer server = new ControlServer();
            server.setDirectory(directory);
            server.setJmsUri(jms);
            server.setRepositoryUri(repository);
            server.setRegistryUri(registry);
            server.setStartEmbeddedAgent(startEmbeddedAgent);
            server.start();
            server.join();
            System.out.println("Control Server Exited");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-3);
        }
        System.exit(0);
    }

    private static void assertHasAdditionalArg(LinkedList<String> alist, String message) throws Exception {
        if (alist.isEmpty()) {
            throw new UsageException(message);
        }
    }

}
