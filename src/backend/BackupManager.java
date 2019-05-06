package backend;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Internal implementation of how to backup / sync / restore files
 * Interacts with the file system extensively
 */
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
     * Returns sync location
     * @return
     */
    private String getSyncLocation() {
        return System.getProperty("user.home") + File.separator + "Desktop" +  File.separator + "syncs" + File.separator + user;
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

        Logger.addToLog(user, new Date() + " Full backup completed");
    }

    /**
     * Restores latest backup to specified device
     * @param pathToDevice - what device to restore to
     * @throws IOException
     */
    public void restore(String pathToDevice) throws IOException {
        restore(pathToDevice, findLatestSnapshot());
    }

    /**
     * Restores selected backup to spefied device
     * @param pathToDevice - what device to restore to
     * @param snapshotDate - what backup to restore
     * @throws IOException
     */
    public void restore(String pathToDevice, Date snapshotDate) throws IOException {
        Logger.addToLog(user, new Date() + " Started restore");

        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        String latestSnapshotDir = getBackupLocation() + File.separator + formatter.format(snapshotDate);
        String restoreDir = pathToDevice + File.separator + "restore" + File.separator + formatter.format(snapshotDate);
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

    /**
     * Restores a synced backup to specified device
     * @param device - what device to sync-restore to
     * @throws IOException
     */
    public void syncRestore(Device device) throws IOException {
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        String restoreDir = device.getPath() + File.separator + "sync" + File.separator + formatter.format(new Date());
        Files.walk(Paths.get(getSyncLocation()))
                .forEach(currentFile -> restoreToDevice(currentFile, Paths.get(restoreDir + File.separator + currentFile.getFileName())));

        // log success
        Logger.addToLog(user, new Date() + " Completed sync-based restore");
    }

    /**
     * Utility funtion used during restore to move files from restore folder to device
     * @param fromSnapshot - path to backup to restore
     * @param restoreFile - what file to restore to, aka destination file
     */
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

    /**
     * Finds latest snapshot backed up for a specific user.
     * @return Date of latest backup
     */
    private Date findLatestSnapshot() {
        // list all files in backup location, exclude anything that isn't a backup directory
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

    /**
     * Syncs dirty files (files previously not synced) to sync folder.
     * @param device - Device object representing device to sync from
     * @throws IOException
     */
    public void synchronise(Device device) throws IOException {
        Logger.addToLog(user, new Date() + " Started file sync");

        // location of the sync folder used for backed up files via synchronisation
        String syncLocation = getSyncLocation();
        // create sync dir if it doesn't exist
        File syncDir = new File(syncLocation);
        if (!syncDir.exists()) {
            syncDir.mkdirs();
        }

        // list all files on device,
        // 1st filter is for extensions, no extensions are filtered
        // 2nd filter is for whether to list recursively, subfolders need to be backedup so yes to recursive
        Collection<File> currentFiles = FileUtils.listFiles(new File(device.getPath()), TrueFileFilter.TRUE, TrueFileFilter.TRUE);


        // list all the files in the sync
        Collection<File> synchedFiles = FileUtils.listFiles(new File(syncLocation), TrueFileFilter.TRUE, TrueFileFilter.TRUE);

        // loop over current files and see if they match synchedFiles
        for (File f : currentFiles) {
            System.out.println("file: " + f);

            File syncFile = new File(syncLocation + File.separator + f.getName());

            // check if the sync file's size matches the size of current file from the device
            // necessary to ensure that an updated version of the file isn't removed
            // can get unlucky, this should check something more accurate like described below:
            // https://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
            boolean fileSizesMatch = f.getTotalSpace() == syncFile.getTotalSpace();
            if (synchedFiles.contains(f) && fileSizesMatch) {
                // any files that are synced can be removed from current files as they don't need to be re-synced
                currentFiles.remove(f);
            } else {
                // otherwise if the sync doesn't contain the file it should remove it from disk
                boolean deleted = false;
                // if the file isn't a directory remove it
                // otherwise if the directory is empty, delete it
                if (!syncFile.isDirectory()) {
                    deleted = syncFile.delete();
                } else if (syncFile.listFiles().length == 0) {
                    deleted = syncFile.delete();
                }

                // this is a ternary operator, allows to condense an if-else statement
                // source: https://stackoverflow.com/a/16876728
                // most likely wont reach due to exception, but still nice to have
                System.out.println(deleted ? "Deleted successfully" : "Failed to delete");
            }
        }

        // anything that remains needs to be synced
        for (File f : currentFiles) {
            // copy current file to the sync location, keep the file name the same
            FileUtils.copyFile(f, new File(syncLocation + File.separator + f.getName()));
        }

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

    /**
     * Copies a file from one location to another, used as a lambda function
     * Has to handle exception as the function is used as part of a java stream.
     * @param toMove - File to be backed up
     * @param devicePath - path of device to backup
     */
    private void backFileUp(Path toMove, String devicePath) {

        // if its a directory, do nothing
        if (!toMove.toFile().isDirectory()) {
            // date for today in the format of 2019-04-01
            Format formatter = new SimpleDateFormat("yyyy-MM-dd");
            String date = formatter.format(new Date());

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
