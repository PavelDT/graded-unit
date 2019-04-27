package backend;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class BackupManager {

    private String user;

    // Constructor
    public BackupManager(String user){
        this.user = user;
    }

    /**
     * Returns backup location
     * @return
     */
    private String getBackupLocation() {
        return System.getProperty("user.home") + File.separator + "Desktop" +  File.separator + "backups" + File.separator + user;
    }

    /**
     * Creates a full backup of the device's files to backup location.
     * @param device The device which is being backed up
     */
    public void createBackup(Device device) throws IOException {

        Logger.addToLog(user, new Date() + " Started new full backup");

        // check to see if backup directory is created and ready to use
        backupCheck();
        // Finds all files in a specified directory recursively, requires java 8
        // source: https://stackoverflow.com/questions/2056221/recursively-list-files-in-java
        Files.walk(Paths.get(device.getPath()))
             .forEach(currentFile -> backFileUp(currentFile, device.getPath()));
        // for(File f: device.)

        Logger.addToLog(user, new Date() + " Full backup completed");
    }

    public void restore(String pathToDevice) throws IOException {
        restore(pathToDevice, findLatestSnapshot());
    }

    public void restore(String pathToDevice, Date snapshotDate) throws IOException {
        Logger.addToLog(user, new Date() + " Started restore");

        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        String latestSnapshotDir = getBackupLocation() + File.separator + formatter.format(snapshotDate);
        String restoreDir = pathToDevice + "restore" + File.separator + formatter.format(snapshotDate);
        // get all the files in latest snapshot
        // Finds all files in a specified directory recursively, requires java 8
        // source: https://stackoverflow.com/questions/2056221/recursively-list-files-in-java

        Files.walk(Paths.get(latestSnapshotDir))
                .forEach(currentFile -> restoreToDevice(currentFile, Paths.get(restoreDir + File.separator + currentFile.getFileName())));
        // for every file in the latest backup
        // check if restored folder exists
        // restore file to device in "restored" folder

        Logger.addToLog(user, new Date() + " Completed restore");
    }

    private void restoreToDevice(Path fromSnapshot, Path restoreFile) {

        // copy file to device path
        try {
            // try creating directory for backup location
            // IMPORTANT: this will not throw and exception if the directory already exists
            Files.createDirectories(restoreFile.getParent());

            if (!fromSnapshot.toFile().isDirectory()) {
                // Allow overwriting of a file as the snapshots are time
                // dependant, meaning one day can have one snapshot
                Files.copy(fromSnapshot, restoreFile, StandardCopyOption.REPLACE_EXISTING);
                // log copying of file
                Logger.addToLog(user, "Retored file " + fromSnapshot + " successfully to " + restoreFile);
            } else {
                System.out.println("Skipping directory. ");
            }
        } catch (IOException ex) {
            System.out.println("Failed to restore: " + fromSnapshot);
            Logger.addToLog(user, "Failed to restore: " + fromSnapshot);
            ex.printStackTrace();
        }

    }

    private Date findLatestSnapshot() {
        // list all files in backup location, exclude anything that isn't a backup directory
//        getBackupLocation()
        File file = new File(getBackupLocation());
        // lists all files for a given path
        // filters out anything that isn't a directory
        // based on: https://stackoverflow.com/questions/5125242/java-list-only-subdirectories-from-a-directory-not-files/5125258
        String[] directories = file.list((current, name) -> new File(current, name).isDirectory());

        List<Date> snapshotDates = new ArrayList<Date>();
        for (String currentDir : directories) {
            try {
                Date snapshotDate = new SimpleDateFormat("yyyy-MM-dd").parse(currentDir);
                snapshotDates.add(snapshotDate);
            } catch (ParseException ex) {
                System.out.println("Directory " + currentDir + " not part of backup tool");
                ex.printStackTrace();
            }
        }

        // sort the dates using descending order, latest snapshot will be first.
        Collections.sort(snapshotDates, Collections.reverseOrder());

        // make sure an IndexOutOfBounds exception isn't thrown
        if (snapshotDates.size() < 1) {
            System.out.println("No backups exist");
            return null;
        }

        // returns first date
        return snapshotDates.get(0);
    }

    public void Synchronise() {
        Logger.addToLog(user, new Date() + " Started file sync");
        Logger.addToLog(user, new Date() + " Completed file sync");
    }

    /**
     * Checks if backup directory exists, creates backup directory if not.
     * @throws IOException
     */
    public void backupCheck() throws IOException {
        File f = new File(getBackupLocation());
        if (!f.exists()){
            Files.createDirectories(f.toPath());
        }
    }

    /**'
     * Copies a file from one location to another, used as a lambda function
     * Has to handle exception as the function is used as part of a java stream.
     * @param toMove
     */
    private void backFileUp(Path toMove, String devicePath) {

        // if its a directory, do nothing
        if (!toMove.toFile().isDirectory()) {
            // date for today in the format of 2019-04-01
            Format formatter = new SimpleDateFormat("yyyy-MM-dd");
            String date = formatter.format(new Date());

            // have to strip the Drive letter from this.
            // Windows: C:\dir\file
            // todo - need to make this work on unix too
            // OSX: Device/
            // build the path of the file's backup location
            String newLocation = getBackupLocation() + File.separator + date + File.separator + toMove.toString().replace(devicePath, "");

            try {
                // try creating directory from source destination
                // IMPORTANT: this will not throw and exception if the directory already exists
                Files.createDirectories(Paths.get(newLocation).getParent());

                // Allow overwriting of a file as the snapshots are time
                // dependant, meaning one day can have one snapshot
                Files.copy(toMove, Paths.get(newLocation), StandardCopyOption.REPLACE_EXISTING);

                // log copying of file
                Logger.addToLog(user, new Date() + " Backed up file " + newLocation);
            } catch (IOException ex) {
                System.out.println("Failed to back file up: " + newLocation);
                ex.printStackTrace();
            }
        } else {
            System.out.println("Skipped backing-up directory");
        }
    }
}
