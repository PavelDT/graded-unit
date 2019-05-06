package backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.filechooser.FileSystemView;

/**
 * Device manager's role is to coordinates interaction between UI and backend implemented in BackupManager
 */
public class DeviceManager implements BackupApp {

    // private instance variables
    private final String ID_FILE_NAME = "id-tag.txt";
    private String user;
    private BackupManager backupManager;

    // Constructor
    public DeviceManager(String user) {
        this.user = user;
        this.backupManager = new BackupManager(user);
    }

    /**
     * Registeres a new device
     * @param devicePath - path to the device being registered
     * @return String - representing UUID id of the device
     * @throws IOException - If a file cannot be read / written
     */
    public String registerNew(String devicePath) throws IOException{
        // make a file
        File device = new File(devicePath + ID_FILE_NAME);
        // make sure we're not overwriting a new device
        if (device.exists())
            throw new FileAlreadyExistsException("Device is already registered");

        device.createNewFile();
        // generate the id and write it to the file
        String id = generateId();
        Files.write(device.toPath(), id.getBytes());

        return id;
    }

    /**
     * Finds all currently connected device
     * @return List of attached Devices
     */
    public List<Device> scanForDevices() {

        List<Device> devices = new ArrayList<Device>();

        FileSystemView fsv = FileSystemView.getFileSystemView();
        File[] devicePaths = null;
        // different OS' represent devices differently
        if (OSUtility.isWindows()) {
            devicePaths = File.listRoots();
        }
        else if (OSUtility.isMac()) {
            devicePaths = new File("/Volumes").listFiles();
        }

        for (File devicePath : devicePaths) {
            if (devicePath.getTotalSpace() > 0) {
                String path = devicePath.toString();
                // determine id by using readId function
                String id = readId(path);
                // add device to list
                devices.add(new Device(path, id));
            } else {
                System.out.println("Device " + devicePath + " skipped as its not storage.");
            }
        }

        return devices;
    }

    /**
     * Restore a specified backup to the selected device
     * @param pathToDevice - path to the device being registered
     * @param date - date for backup to restore
     * @throws IOException - If a file cannot be read / written
     */
    public void restore(String pathToDevice, String date) throws IOException {
        // todo... add the ability for a user to specify
        //         a date using the UI
        backupManager.restore(pathToDevice, null);
    }

    /**
     * Restore latest backup to the selected device
     * @param pathToDevice - path to the device being registered
     * @throws IOException - If a file cannot be read / written
     */
    public void restore(String pathToDevice) throws IOException {
        // date and id
        backupManager.restore(pathToDevice);
    }

    /**
     * Sync-restore latest backup to the selected device
     * @param devicePath - path to the device being registered
     * @throws IOException - If a file cannot be read / written
     */
    public void syncRestore(String devicePath) throws IOException {
        Device device = new Device(devicePath, readId(devicePath));
        backupManager.syncRestore(device);
    }

    /**
     * Full backup of all device data for selected device
     * @param devicePath - path to the device being registered
     * @throws IOException - If a file cannot be read / written
     */
    public void backup(String devicePath) throws IOException {
        Device device = new Device(devicePath, readId(devicePath));
        backupManager.createBackup(device);
    }

    /**
     * Syncs dirty files (files previously not synced) to sync folder.
     * @param devicePath - path to the device being synced
     * @throws IOException - If a file cannot be read / written
     */
    public void synchronise(String devicePath) throws IOException {
        backupManager.synchronise(new Device(devicePath, readId(devicePath)));
    }

    /**
     * Generates UUID based id as a string
     * @return String representing id
     */
    private String generateId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid;
    }

    /**
     * Tries to read id of a device, if it reads id, device is register
     * otherwise the device is new and un-registered
     * @param devicePath - path to device
     * @return Id of device
     */
    public String readId(String devicePath) {
        Path path = Paths.get(devicePath + ID_FILE_NAME);
        // No id, this is a new device, return placeholder id.
        if (!path.toFile().exists()) {
            return "new";
        }

        // return device id
        try {
            return new String(Files.readAllBytes(path));
        } catch (IOException ex) {
            ex.printStackTrace();
            return "error";
        }
    }
}
