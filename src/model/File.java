package model;

import java.util.Map;

/**
 * Represents a file in the system
 */
public class File extends FileSystemItem {
    private String content;

    /**
     * Initialize a file
     * 
     * @param name    File name
     * @param content File content
     * @param parent  Parent folder
     */
    public File(String name, String content, FileSystemItem parent) {
        super(name, ItemType.FILE, parent);
        this.content = content != null ? content : "";
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        updateModifiedTime();
    }

    @Override
    public long getSize() {
        return content.getBytes().length;
    }

    @Override
    public Map<String, Object> getInfo() {
        Map<String, Object> info = super.getInfo();
        return info;
    }

    @Override
    public String toString() {
        return "  - " + name + " - " + getSize() + "B";
    }
}
