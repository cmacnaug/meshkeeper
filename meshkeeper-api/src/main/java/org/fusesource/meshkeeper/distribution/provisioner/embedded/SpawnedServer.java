package org.fusesource.meshkeeper.distribution.provisioner.embedded;

import static org.fusesource.meshkeeper.Expression.file;
import static org.fusesource.meshkeeper.control.ControlServer.ControlEvent.SHUTDOWN;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.URL;
import java.util.LinkedList;
import java.util.Properties;

import org.fusesource.meshkeeper.JavaLaunch;
import org.fusesource.meshkeeper.LaunchDescription;
import org.fusesource.meshkeeper.MeshEvent;
import org.fusesource.meshkeeper.MeshKeeper;
import org.fusesource.meshkeeper.MeshKeeperFactory;
import org.fusesource.meshkeeper.control.ControlServer;
import org.fusesource.meshkeeper.control.Main;
import org.fusesource.meshkeeper.distribution.PluginClassLoader;
import org.fusesource.meshkeeper.distribution.PluginResolver;
import org.fusesource.meshkeeper.distribution.provisioner.Provisioner.MeshProvisioningException;
import org.fusesource.meshkeeper.util.internal.ProcessSupport;

//import static Expression.file;
public class SpawnedServer implements LocalServer{

    private long provisioningTimeout = 10000;
    private boolean started = false;
    private boolean createWindow = true;
    private boolean pauseWindow = true;
    private String registryUri;
    private File serverDirectory;
    private int registryPort = 0;


    public synchronized void start() throws MeshProvisioningException  {
        if(isDeployed()) {
            return;
        }
        
        if(!serverDirectory.exists()) {
            try {
                serverDirectory = serverDirectory.getCanonicalFile();
                serverDirectory.mkdirs();
                if(!serverDirectory.exists()) {
                    throw new FileNotFoundException(serverDirectory.getPath());
                }
            }
            catch (Exception e) {
                throw new MeshProvisioningException("Unable to create server directory: " + serverDirectory, e);
            }
        }

        JavaLaunch jl = new JavaLaunch();
        jl.setWorkingDir(file(serverDirectory.getPath()));
        String jvm = System.getProperty("java.home") + "/bin/java";
        if(ProcessSupport.isWindows()) {
            jl.setJvm(file(jvm + ".exe"));
        }
        else {
            jl.setJvm(file(jvm));
        }
        
        //Resolve the meshkeeper classpath:
        try {
            PluginResolver resolver = PluginClassLoader.getDefaultPluginLoader().getPluginResolver();
            jl.setClasspath(resolver.resolveClassPath(PluginResolver.PROJECT_GROUP_ID + ":" + PluginResolver.PROJECT_ARTIFACT_ID +":" + PluginClassLoader.getDefaultPluginVersion()));
        }
        catch (Exception e) {
            throw new MeshProvisioningException("Unable to resolve meshkeeper classpath:" + e.getMessage(), e);
        }
       
        String log4jConf=System.getProperty("log4j.configuration");
        if(log4jConf == null) {
            URL u = this.getClass().getClassLoader().getResource("meshkeeperlog4j.properties");
            if(u == null) {
                u = this.getClass().getClassLoader().getResource("log4j.properties");
            }
            if(u != null) {
                log4jConf=u.toString();
            }
        }
        if(log4jConf != null) {
            jl.addSystemProperty("log4j.configuration", log4jConf);
            //jl.addSystemProperty("log4j.debug", "true");
        }
        
        
        jl.setMainClass(Main.class.getName());
        jl.addArgs(Main.DIRECTORY_SWITCH).addArgs(file(serverDirectory.toString()));
        jl.addArgs(Main.REGISTRY_SWITCH, "zk:tcp://0.0.0.0:" + registryPort);
        jl.addArgs(Main.START_EMBEDDED_AGENT);
        
        
        LaunchDescription ld = jl.toLaunchDescription();
        //ld.propagateSystemProperties(System.getProperties(), LaunchAgent.PROPAGATED_SYSTEM_PROPERTIES);
        
        LinkedList<String> cmdList = new LinkedList<String>(ld.evaluate(new Properties()));
        
        if(createWindow) {
            //Look for command to launch in window:
            if(!ProcessSupport.isWindows()) {
                cmdList.addLast("&");
            }
        }
        
        String [] cmdArray = cmdList.toArray(new String[]{});
        String command = "";
        try {
            command = ld.evaluateCommandLine(System.getProperties());
            
            //If it's windows dump the command to script into a script:
            if(createWindow && ProcessSupport.isWindows()) {
                File batFile = new File(serverDirectory, "meshkeeper.bat");
                if(batFile.exists()) {
                    batFile.delete();
                }
                batFile.createNewFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(batFile));
                writer.write("@echo off\r\n");
                writer.write("TITLE MeshKeeper\r\n");
                writer.write(command + "\r\n");
                if(pauseWindow) {
                    writer.write("pause\r\n");
                }
                cmdArray = new String[] {"START", batFile.getCanonicalPath()};
                command = "START " + batFile.getCanonicalPath();
                writer.flush();
                writer.close();
            }
                    
            
            System.out.println("Launching command: " + command);
            
            Process proc = Runtime.getRuntime().exec(cmdArray, null, new File(ld.getWorkingDirectory().evaluate()));
   
            if(createWindow) {
                proc.waitFor();
            }
            
            long timeout = System.currentTimeMillis() + provisioningTimeout;
            while(!isDeployed() && System.currentTimeMillis() < timeout) {
                Thread.sleep(100);
            }
            
            if(!isDeployed()) {
                throw new MeshProvisioningException("Timed out spawning meshkeeper server");
            }
            
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            if(isDeployed()) {
                throw new MeshProvisioningException("Interrupted spawning meshkeeper control server");
            }
        }
        catch (Exception ioe) {
            throw new MeshProvisioningException("Unable to spawn meshkeeper control server with command line of " + command, ioe);
        }
        
        
        
    }

    public synchronized void stop() throws MeshProvisioningException {
        if(isDeployed()) {
            try {
                sendShutdownSignal(getRegistryUri());
            }
            finally {
                started = false;
                registryUri = null;
            }
        }
    }
    
    
    
    public String getRegistryUri() throws MeshProvisioningException {
        if(registryUri != null) { 
            return registryUri;
        }
        
        try {
            Properties p = getFileProps();
            registryUri = p.getProperty(MeshKeeperFactory.MESHKEEPER_REGISTRY_PROPERTY);
            if (registryUri != null) {
                return registryUri;
            }
            else {
                throw new Error(MeshKeeperFactory.MESHKEEPER_REGISTRY_PROPERTY + " was not found in server properties file");
            }

        }
        catch (Throwable thrown) {
            throw new MeshProvisioningException("Error locating registry uri", thrown);
        }
    }
    
    private Properties getFileProps() throws Exception {
        if(serverDirectory == null) {
            throw new MeshProvisioningException("serverDirectory must be specified!");
        }
        
        File propFile =  propFile();

        if (propFile.exists()) {
            Properties props = new Properties();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(propFile);
                props.load(fis);
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
            return props;
        } else {
            throw new FileNotFoundException(propFile.getPath());
        }
    }
    
    private File propFile() {
        return new File(serverDirectory, ControlServer.CONTROLLER_PROP_FILE_NAME);
    }
    
    public void setServerDirectory(File ServerDirectory) {
        this.serverDirectory = ServerDirectory;
    }    
    
    public void setRegistryPort(int registryPort) {
        this.registryPort = registryPort;
    }

    public long getProvisioningTimeout() {
        return provisioningTimeout;
    }

    public void setProvisioningTimeout(long provisioningTimeout) {
        this.provisioningTimeout = provisioningTimeout;
    }

    public boolean isCreateWindow() {
        return createWindow;
    }

    public void setCreateWindow(boolean createWindow) {
        this.createWindow = createWindow;
    }

    public boolean isStarted() {
        if(started) {
            return true;
        } else {
            return isDeployed();
        }
        
    }
    
    private boolean isDeployed () {
        if(started) {
            return true;
        }
        
        MeshKeeper mesh = null;
        try {
            //See if we can connect:
            mesh = MeshKeeperFactory.createMeshKeeper(getRegistryUri());
            started = true;
        } catch (Exception e) {
            registryUri = null;
            started = false;
        } finally {
            if (mesh != null) {
                try {
                    mesh.destroy();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return started;
    }

    static final void sendShutdownSignal(String registryUri) throws MeshProvisioningException {
        MeshKeeper mesh = null;
        try {
            mesh = MeshKeeperFactory.createMeshKeeper(registryUri);
            mesh.eventing().sendEvent(new MeshEvent(SHUTDOWN.ordinal(), EmbeddedServer.class.getSimpleName(), null), ControlServer.CONTROL_TOPIC);

            File f = new File(MeshKeeperFactory.getDefaultServerDirectory(), ControlServer.CONTROLLER_PROP_FILE_NAME);
            long timeout = System.currentTimeMillis() + 5000;
            while (System.currentTimeMillis() < timeout && f.exists()) {
                Thread.sleep(500);
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new MeshProvisioningException("interrupted", ie);
        } catch (Exception e) {

        }
    }

    public void setPauseWindow(boolean pauseWindow) {
        this.pauseWindow = pauseWindow;
        
    }
    
   
}
