package org.fusesource.cloudlaunch.util;

import java.util.List;
import java.net.URL;
import java.io.IOException;
import java.io.Serializable;

import org.fusesource.cloudlaunch.distribution.Distributable;

/**
 * @author chirino
*/
public interface IClassLoaderServer extends Distributable {
    public static class PathElement implements Serializable {
        URL url;
        long jarFileChecksum;
        long jarFileSize;
    }

    IClassLoaderServer getParent() throws Exception;
    List<PathElement> getPathElements() throws Exception;
    byte[] download(URL url) throws IOException;
    byte[] findResource(String name) throws Exception;

}