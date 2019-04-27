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

public class DeviceManager {

    private final String ID_FILE_NAME = "id-tag.txt";
    private String user;
    private BackupManager backupManager;

    // Constructor
    public DeviceManager(String user) {
        this.user = user;
        this.backupManager = new BackupManager(user);
    }

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
     * @throws IOException
     */
    // todo - list only external devices, dont list OS drive and dont list CD Drives / DVD drives etc.
    // todo - USE A MAP where k=path v=Device object
    public List<Device>scanForDevices() throws IOException{

        List<Device> devices = new ArrayList<Device>();

        FileSystemView fsv = FileSystemView.getFileSystemView();
        File[] devicePaths = File.listRoots();

        for (File devicePath : devicePaths) {
            // todo - remove System.out.println's, used for dbugging
            System.out.println("Drive Letter: " + devicePath);
            System.out.println("\tType: " + fsv.getSystemTypeDescription(devicePath));
            System.out.println("\tTotal space: " + devicePath.getTotalSpace());
            System.out.println("\tFree space: " + devicePath.getFreeSpace());
            System.out.println();

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

    public boolean displayDeviceStatus(String status){
        return false;
    }

    public void restore(String pathToDevice, String date) throws IOException {
        // todo... add the ability for a user to specify
        //         a date using the UI
        backupManager.restore(pathToDevice, null);
    }

    public void restore(String pathToDevice) throws IOException {
        // date and id
        backupManager.restore(pathToDevice);
    }

    public void backup(String path, String id) throws IOException{
        Device device = new Device(path, id);
        backupManager.createBackup(device);
    }

    public void synchronise(String id){

    }

    private String generateId(){
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid;
    }

    /**
     * Tries to read id of a device, if it reads id, device is register
     * otherwise the device is new and un-registered
     * @throws IOException - push the exception to caller function, shouldn't be handled here
     * @return Id of device
     */
    public String readId(String devicePath) throws IOException {
        Path path = Paths.get(devicePath + ID_FILE_NAME);
        // No id, this is a new device, return placeholder id.
        if (!path.toFile().exists()) {
            return "new";
        }

        // return device id
        return new String(Files.readAllBytes(path));
    }
}
