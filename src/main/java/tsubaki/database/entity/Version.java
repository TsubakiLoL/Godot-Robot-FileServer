package tsubaki.database.entity;

public class Version {
    private String plugin_id;
    private String version_id;

    private String path;



    public String getPlugin_id() {
        return plugin_id;
    }

    public void setPlugin_id(String plugin_id) {
        this.plugin_id = plugin_id;
    }

    public String getVersion_id() {
        return version_id;
    }

    public void setVersion_id(String version_id) {
        this.version_id = version_id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    @Override
    public String toString() {
        return "Version{" +
                "plugin_id='" + plugin_id + '\'' +
                ", version_id='" + version_id + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
