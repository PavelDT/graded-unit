package backend;

import java.io.IOException;

/**
 * Represents the expected functions that the backup application should offer
 */
public interface BackupApp {

    /**
     * register new device
     * @param devicePath - path to the device being registered
     * @return String representing device id.
     * @throws IOException
     */
    String registerNew(String devicePath) throws IOException;

    /**
     * backup existing device
     * @param devicePath - path to the device being backed-up
     * @throws IOException
     */
    void backup(String devicePath) throws IOException;

    /**
     * sync existing device
     * @param devicePath - path to the device being synced
     * @throws IOException
     */
    void synchronise(String devicePath) throws IOException;

    /**
     * restore existing device
     * @param devicePath - path to the device being restored
     * @throws IOException
     */
    void restore(String devicePath) throws IOException;

    /**
     * restore an existing device from sync backup
     * @param devicePath - path to the device being sync-restored
     * @throws IOException
     */
    void syncRestore(String devicePath) throws IOException;
}
