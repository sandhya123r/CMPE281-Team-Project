package hello;

public class Database {

    private final String key;
    private final String value;

    public Database(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
