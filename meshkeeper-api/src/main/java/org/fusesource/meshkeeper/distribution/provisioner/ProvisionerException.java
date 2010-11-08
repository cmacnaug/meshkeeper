package org.fusesource.meshkeeper.distribution.provisioner;

import java.io.IOException;

public class ProvisionerException extends IOException {

    private static final long serialVersionUID = 1230894179622311918L;

    public ProvisionerException(String reason, Throwable cause) {
        super(reason);
        super.initCause(cause);
    }

    public ProvisionerException(String string) {
        super(string);
    }

}
