package tsubaki.database.entity;

public class Version {
    private String plugin_id;
    private String version;

    private String path;

    private String package_name;


    public String getPlugin_id() {
        return plugin_id;
    }

    public void setPlugin_id(String plugin_id) {
        this.plugin_id = plugin_id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion_id(String version_id) {
        this.version = version;
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
                ", version_id='" + version + '\'' +
                ", path='" + path + '\'' +
                '}';
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }
}
