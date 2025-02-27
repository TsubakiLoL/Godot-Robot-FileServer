package tsubaki.database.entity;

public class Plugin {
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

    @Override
    public String toString() {
        return "Plugin{" +
                "plugin_id='" + plugin_id + '\'' +
                ", author_id='" + author_id + '\'' +
                ", name='" + name + '\'' +
                ", introduction='" + introduction + '\'' +
                '}';
    }
}
