package tsubaki.database.entity;

public class NodeSet {
    private String set_id;

    private String author_id;

    private String path;

    private String name;
    private String introduction;

    public String getSet_id() {
        return set_id;
    }

    public void setSet_id(String set_id) {
        this.set_id = set_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "NodeSet{" +
                "set_id='" + set_id + '\'' +
                ", author_id='" + author_id + '\'' +
                ", path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", introduction='" + introduction + '\'' +
                '}';
    }
}
