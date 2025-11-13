package model;

/**
 * Types of items in the file system
 */
public enum ItemType {
    FILE("file"),
    FOLDER("folder");

    private final String value;

    ItemType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}