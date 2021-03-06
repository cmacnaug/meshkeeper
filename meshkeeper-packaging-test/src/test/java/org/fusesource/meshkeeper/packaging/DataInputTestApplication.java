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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.fusesource.meshkeeper.MeshEvent;
import org.fusesource.meshkeeper.MeshKeeper;
import org.fusesource.meshkeeper.MeshKeeperFactory;

/**
 * DataInputClass
 * <p>
 * Description:
 * </p>
 * 
 * @author cmacnaug
 * @version 1.0
 */
public class DataInputTestApplication {

    private String dataFile;

    public static void main(String[] args) {

        DataInputTestApplication app = new DataInputTestApplication();

        LinkedList<String> aList = new LinkedList<String>(Arrays.asList(args));
        while (!aList.isEmpty()) {
            try {
                String methodName = aList.removeFirst();
                if (methodName.startsWith("-")) {
                    methodName = methodName.substring(1);
                } else {
                    throw new IllegalArgumentException(methodName);
                }
                String arg = aList.removeFirst();

                Method m = DataInputTestApplication.class.getMethod("set" + methodName, new Class[] { String.class });
                m.invoke(app, new Object[] { arg });
            } catch (Throwable thrown) {
                thrown.printStackTrace();
                System.exit(-1);
            }
        }

        app.run();
    }

    public void setDataFile(String file) {
        dataFile = file;
    }

    public void run() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try {
            while (true) {
                String command = in.readLine();
                if (command == null) {
                    break;
                }
                StringTokenizer tok = new StringTokenizer(command, ":");
                command = tok.nextToken();

                if (command.equalsIgnoreCase("exit")) {
                    System.exit(Integer.parseInt(tok.nextToken().trim()));
                } else if (command.equalsIgnoreCase("echo")) {
                    System.out.println(tok.nextToken());
                } else if (command.equalsIgnoreCase("echo-data-file")) {
                    echoDataFile();
                } else if (command.equals("error")) {
                    System.err.println(tok.nextToken());
                } else if (command.equals("event")) {
                    sendEvent(tok.nextToken().trim());
                } else {
                    System.err.println("Unknown command: " + command);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendEvent(String topic) throws Exception {
        MeshKeeper mesh = null;
        try {
            mesh = MeshKeeperFactory.createMeshKeeper();
            mesh.eventing().sendEvent(new MeshEvent(), topic);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mesh != null) {
                mesh.destroy();
            }
        }
    }

    private void echoDataFile() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(new File(dataFile)));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            System.out.print(line);
            System.out.flush();
        }

    }
}