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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Untar;
import org.apache.tools.ant.taskdefs.Untar.UntarCompressionMethod;

public class ZipUtil {

  private static final Log LOG = LogFactory.getLog(ZipUtil.class);

  public static final void unzipTo(File compressed, File targetDir) throws ZipException,
      IOException {
    if (compressed.getName().endsWith(".tar.gz") || compressed.getName().endsWith(".tar")) {
      untarFile(compressed, targetDir);
    } else {
      unzipFile(compressed, targetDir);
    }
  }

  public static final void untarFile(File tar, File targetDir) {
    Untar untar = new Untar() {
      @Override
      public void log(java.lang.String msg) {
        log(msg, Project.MSG_INFO);
      }

      @Override
      public void log(java.lang.String msg, int level) {
        switch (level) {
          case Project.MSG_DEBUG: {
            if (LOG.isDebugEnabled()) {
              LOG.debug(msg);
            }
            break;
          }
          case Project.MSG_ERR: {
            LOG.error(msg);
            break;
          }
          case Project.MSG_INFO: {
            if (LOG.isInfoEnabled()) {
              LOG.info(msg);
            }
            break;
          }
          case Project.MSG_VERBOSE: {
            if (LOG.isTraceEnabled()) {
              LOG.trace(msg);
            }
            break;
          }
          case Project.MSG_WARN: {
            if (LOG.isWarnEnabled()) {
              LOG.warn(msg);
            }
            break;
          }
          default: {
            if (LOG.isInfoEnabled()) {
              LOG.info(msg);
            }
          }
        }

      }
    };

    if (tar.getName().endsWith(".gz")) {
      UntarCompressionMethod c = new UntarCompressionMethod();
      c.setValue("gzip");
      untar.setCompression(c);
    }

    untar.setSrc(tar);
    untar.setDest(targetDir);
    untar.execute();
  }

  public static final void unzipFile(File zip, File targetDir) throws ZipException, IOException {
    Enumeration<? extends ZipEntry> entries;
    ZipFile zipFile;

    zipFile = new ZipFile(zip);

    entries = zipFile.entries();

    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();

      if (entry.isDirectory()) {
        // Assume directories are stored parents first then children.
        // This is not robust, just for demonstration purposes.
        (new File(targetDir, entry.getName())).mkdirs();
        continue;
      }

      copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(
          new File(targetDir, entry.getName()))));
    }

    zipFile.close();

  }

  public static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int len;

    while ((len = in.read(buffer)) >= 0)
      out.write(buffer, 0, len);

    in.close();
    out.close();
  }

}
