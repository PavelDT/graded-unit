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
     * @throws IOException - If a file cannot be read / written
     */
    String registerNew(String devicePath) throws IOException;

    /**
     * backup existing device
     * @param devicePath - path to the device being backed-up
     * @throws IOException - If a file cannot be read / written
     */
    void backup(String devicePath) throws IOException;

    /**
     * sync existing device
     * @param devicePath - path to the device being synced
     * @throws IOException - If a file cannot be read / written
     */
    void synchronise(String devicePath) throws IOException;

    /**
     * restore existing device
     * @param devicePath - path to the device being restored
     * @throws IOException - If a file cannot be read / written
     */
    void restore(String devicePath) throws IOException;

    /**
     * restore an existing device from sync backup
     * @param devicePath - path to the device being sync-restored
     * @throws IOException - If a file cannot be read / written
     */
    void syncRestore(String devicePath) throws IOException;
}
