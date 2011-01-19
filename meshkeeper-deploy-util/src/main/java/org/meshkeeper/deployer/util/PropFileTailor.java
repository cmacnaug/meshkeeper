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

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class PropFileTailor {

  public static void tailor(File target, Properties props) throws IOException {
    tailor(target, props, true, true);
  }

  public static void tailor(File target, Properties props, boolean appendUnreplacedValues, boolean leaveOldVaulesCommented)
      throws IOException {
    TailoringUtil.propFileScanAndReplace(target, props, appendUnreplacedValues, leaveOldVaulesCommented);

    //
    //
    // Properties toAdd = new Properties(props);
    // File temp = new File(target.getCanonicalPath() + ".tailor");
    //
    // BufferedReader reader = new BufferedReader(new FileReader(target));
    // BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
    // while (true) {
    // String line = reader.readLine();
    // String tailored = line;
    // if (line == null) {
    // break;
    // }
    //
    // if (!isComment(line)) {
    // int eq = line.indexOf("=");
    // if (eq > 0) {
    // String prop = line.substring(0, eq).trim();
    // String override = toAdd.getProperty(prop);
    // if (override != null) {
    // if(leaveOldVaulesCommented) {
    // writer.write("#" + line);
    // writer.newLine();
    // }
    //
    // tailored = prop + "=" + override;
    // }
    // }
    // }
    //
    // writer.write(tailored);
    // writer.newLine();
    // }
    //
    // for (Entry<Object, Object> entry : toAdd.entrySet()) {
    // writer.write(entry.getKey() + "=" + entry.getValue());
    // writer.newLine();
    // }
    //
    // reader.close();
    // writer.flush();
    // writer.close();
    //
    // FileUtils.deleteQuietly(target);
    // FileUtils.moveFile(temp, target);
  }

  // private static boolean isComment(String line) {
  // if (line.startsWith("#") || line.startsWith(";")) {
  // return true;
  // }
  // return false;
  // }

  public static final void main(String[] args) throws IOException {
    String file = args[0];
    Properties props = new Properties();
    for (int i = 1; i + 1 < args.length; i += 2) {
      props.put(args[i], args[i + 1]);
    }
    
    boolean appendUnreplaced = Boolean.getBoolean("appendUnreplaced");

    PropFileTailor.tailor(new File(file), props, appendUnreplaced, false);
  }

}
