package backend;

import java.io.File;
import java.util.List;

public class Device {


    private String path;
    private String id;

    private List<File> backedUp;
    private List<File> dirty;

    public List<File> getBackedUp() {
        return backedUp;
    }

    public List<File> getDirty() {
        return dirty;
    }

    // Constructor
    public Device(String path, String id) {
        this.path = path;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }
}
