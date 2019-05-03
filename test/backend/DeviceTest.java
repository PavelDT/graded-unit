package backend;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

// enforce order of testing
// A device has to be registered before it can be backed up etc.
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DeviceTest {
    private static final String username = "a64c7652-68f0-11e9-a923-1681be663d3e" ;
    private static final String devicePath = System.getProperty("user.home") + File.separator + "Desktop" +  File.separator + "unitTest" + File.separator;
    private final DeviceManager dm = new DeviceManager(username);

    @BeforeClass
    public static void prepare() {
        // create a simulated device path
        File dFile = new File(devicePath);
        assertTrue(dFile.mkdirs());
    }

    @AfterClass
    public static void cleanup() throws IOException {
        // delete simulated device path
        File dFile = new File(devicePath);
        FileUtils.deleteDirectory(dFile);
        assertFalse(dFile.exists());
    }


    @Test
    public void test1RegisterNew() throws IOException {
        String id = dm.registerNew(devicePath);
        // test uuid length, should always be the same
        assertEquals(id.length(), "6af7a7cb75cd4c4a8df1e01bac96c963".length());
    }


    @Test
    public void test2ReadId() {
        String id = dm.readId(devicePath);
        assertEquals(id.length(), "6af7a7cb75cd4c4a8df1e01bac96c963".length());
        // as dashes are removed manually, here they're added back
        // source: https://stackoverflow.com/questions/18986712/creating-a-uuid-from-a-string-with-no-dashes#answer-18987428
        String idWithDashes = id.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
        UUID res = UUID.fromString(idWithDashes);
        // checks that a valid UUID object was created from the string
        assertEquals(idWithDashes, res.toString());
    }

    @Test
    public void testScanForDevice() throws IOException {
        List<Device> devices = dm.scanForDevices();
        System.out.println(devices);
        // check there was more than 0 devices found
        assertTrue(devices.size() > 0);
        // check device exists as a directory
        for (Device d : devices) {
            File f = new File(d.getPath());
            assertTrue(f.exists());
            assertTrue(f.isDirectory());
        }
    }

    @Test
    public void testRestore() throws IOException {
        dm.restore(devicePath);
        File backupRestoreDir = new File(devicePath + File.separator + "restore" + File.separator);
        // check that the restored directory exists, is a directory and contains files
        assertTrue(backupRestoreDir.exists());
        assertTrue(backupRestoreDir.isDirectory());
        assertTrue(backupRestoreDir.listFiles().length > 0);
    }

    // test that no exception is thrown
    @Test
    public void test3Backup() throws IOException {
        dm.backup(devicePath);
    }

    // test that no exception is thrown
    @Test
    public void test4Synchronise() throws IOException {
        dm.synchronise(devicePath);
    }

    @Test
    public void test5SyncRestore() throws IOException {
        dm.syncRestore(devicePath);
        File syncRestoreDir = new File(devicePath + File.separator + "sync" + File.separator);
        // check that the restored directory exists, is a directory and contains files
        assertTrue(syncRestoreDir.exists());
        assertTrue(syncRestoreDir.isDirectory());
        assertTrue(syncRestoreDir.listFiles().length > 0);
    }

}
