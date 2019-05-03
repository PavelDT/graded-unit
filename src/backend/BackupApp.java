package backend;

import java.io.IOException;

/**
 * Represents the expected functions that the backup application should offer
 */
public interface BackupApp {

    // register new device
    String registerNew(String devicePath) throws IOException;

    // backup existing device
    void backup(String devicePath) throws IOException;

    // sync existing device
    void synchronise(String devicePath) throws IOException;

    // restore existing device
    void restore(String devicePath) throws IOException;

    // restore an existing device from sync backup
    void syncRestore(String devicePath) throws IOException;
}
