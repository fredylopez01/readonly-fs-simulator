package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for file system items
 */
public abstract class FileSystemItem {
    protected String name;
    protected ItemType itemType;
    protected FileSystemItem parent;
    protected LocalDateTime createdAt;
    protected LocalDateTime modifiedAt;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Initialize a file system item
     * 
     * @param name     Name of the item
     * @param itemType Type of item (FILE or FOLDER)
     * @param parent   Parent folder reference
     */
    public FileSystemItem(String name, ItemType itemType, FileSystemItem parent) {
        this.name = name;
        this.itemType = itemType;
        this.parent = parent;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }

    /**
     * Get the full path of this item
     * Similar to d_path() in fs/dcache.c of Linux kernel
     */
    public String getPath() {
        if (parent == null) {
            return "/" + name;
        }
        String parentPath = parent.getPath();
        if ("/".equals(parentPath)) {
            return "/" + name;
        }
        return parentPath + "/" + name;
    }

    /**
     * Get item information as map
     */
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", name);
        info.put("type", itemType.getValue());
        info.put("size", getSize());
        info.put("path", getPath());
        info.put("created", createdAt.format(DATE_FORMATTER));
        info.put("modified", modifiedAt.format(DATE_FORMATTER));
        return info;
    }

    /**
     * Get size of the item in bytes
     */
    public abstract long getSize();

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.modifiedAt = LocalDateTime.now();
    }

    public ItemType getItemType() {
        return itemType;
    }

    public FileSystemItem getParent() {
        return parent;
    }

    public void setParent(FileSystemItem parent) {
        this.parent = parent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getModifiedAt() {
        return modifiedAt;
    }

    protected void updateModifiedTime() {
        this.modifiedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return itemType.getValue() + ": " + getPath();
    }
}