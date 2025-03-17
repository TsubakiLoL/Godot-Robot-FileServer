package tsubaki.database.entity;

public class PluginConnectVersion {
    private String plugin_id;
    private String author_id;
    private String name;

    private String introduction;

    public String getPlugin_id() {
        return plugin_id;
    }

    public void setPlugin_id(String plugin_id) {
        this.plugin_id = plugin_id;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }



    private String version_id;

    private String path;





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
        return "PluginConnectVersion{" +
                "plugin_id='" + plugin_id + '\'' +
                ", author_id='" + author_id + '\'' +
                ", name='" + name + '\'' +
                ", introduction='" + introduction + '\'' +
                ", version_id='" + version_id + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
