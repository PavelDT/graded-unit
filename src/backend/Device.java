package backend;

public class Device {

    private String path;
    private String id;

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
