package org.fusesource.meshkeeper.distribution.provisioner;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;

import org.fusesource.meshkeeper.util.internal.Os;


/**
 * Instantiates and runs a provisioner. Useful for utilities like
 * Ant that want to spawn MeshKeeper in the background. For Maven
 * The maven module's provisioning mojo should be used instead.
 * 
 * @author cmacnaug
 */
public class Main {


    @SuppressWarnings("serial")
    static class UsageException extends Exception {
        UsageException(String message) {
            super(message);
        }
    }
    
    private static final void showUsage() {
        System.out.println("Usage:");
        System.out.println("Args:");
        System.out.println("  -h, --help                    -- this message");
        System.out.println("  -u --uri                      -- the provisioner uri.");
        System.out.println("  [-a --action deploy|undeploy] -- specifies the provisioning action to take.");
        System.out.println("");
        System.out.println("Example:");
        System.out.println("-a undeploy -u spawn:file://c:/meshkeeper/server?leaveRunning=true&amp;createWindow=true");
        
    }
    
    public static final void main(String [] args) {
        String jv = System.getProperty("java.version").substring(0, 3);
        if (jv.compareTo("1.5") < 0) {
            System.err.println("The Control Server requires jdk 1.5 or higher to run, the current java version is " + System.getProperty("java.version"));
            System.exit(-1);
            return;
        }

        String action = "deploy";
        String uri = null;
        
        LinkedList<String> alist = new LinkedList<String>(Arrays.asList(args));
        try {
            while (!alist.isEmpty()) {
                String arg = alist.removeFirst();
                if (arg.equals("--help") || arg.equals("-h")) {
                    showUsage();
                    return;
                } else if (arg.equals("-a") || arg.equals("--action")) {
                    assertHasAdditionalArg(alist, "action required");
                    action = alist.removeFirst();
                } else if (arg.equals("-u") || arg.equals("--uri")) {
                    assertHasAdditionalArg(alist, "uri required");
                    uri = alist.removeFirst();
                } else {
                    throw new UsageException("Illegal Argument: " + arg);
                }
            }
            
            if(uri == null) {
                throw new UsageException("uri cannot be null");
            }
            
            //Replace backslashes with forward slashes, usefule from ant
            //on windows where it can be tricky to get absolute paths with 
            //forward slash file separators
            if(Os.isFamily(Os.FAMILY_WINDOWS) && System.getProperty("suppress.uri.cleanup") == null) {
              if(uri.startsWith("spawn") || uri.startsWith("embedded")) {
                uri = uri.replace('\\', '/');
              }
            }
            
            ProvisionerFactory pf = new ProvisionerFactory();
            Provisioner provisioner = pf.create(uri);
            
            //Look for the action:
            Method m = null;
            try {
              m = provisioner.getClass().getDeclaredMethod(action, new Class [] {});
            }catch (NoSuchMethodException nsme) {
              //See if there is one that matches case insensitively:
              for(Method candidate : provisioner.getClass().getDeclaredMethods()) {
                if(candidate.getParameterTypes().length > 0) {
                  continue;
                }
                
                if(!candidate.isAccessible()) {
                  continue;
                }
                
                if(m.getDeclaringClass().getPackage().getName().startsWith("java")) {
                  continue;
                }
                
                if(candidate.getName().toLowerCase().equals(action.toLowerCase())) {
                  m = candidate;
                  break;
                }
              }
              
              if(m == null) {
                nsme.fillInStackTrace();
                throw nsme;
              }
            }
            
            Object o = m.invoke(provisioner, new Object[]{});
            if(o != null) {
                System.out.println(o);
            }
           
            System.out.println("Provisioned at " + provisioner.findMeshRegistryUri());
        }
        catch (Exception e) {
            showUsage();
            e.printStackTrace();
            throw new RuntimeException(e);
        }   
    }
    


    private static void assertHasAdditionalArg(LinkedList<String> alist, String message) throws Exception {
        if (alist.isEmpty()) {
            throw new UsageException(message);
        }
    }
}
