package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a folder in the system
 * Simulates struct dentry from Linux kernel
 */
public class Folder extends FileSystemItem {
    private List<FileSystemItem> children;

    /**
     * Initialize a folder
     * 
     * @param name   Folder name
     * @param parent Parent folder
     */
    public Folder(String name, FileSystemItem parent) {
        super(name, ItemType.FOLDER, parent);
        this.children = new ArrayList<>();
    }

    /**
     * Add a child item to this folder
     * Similar to d_subdirs in Linux kernel
     * 
     * @param item Item to add
     */
    public void addChild(FileSystemItem item) {
        item.setParent(this);
        children.add(item);
        updateModifiedTime();
    }

    /**
     * Remove a child item from this folder
     * 
     * @param item Item to remove
     */
    public void removeChild(FileSystemItem item) {
        if (children.remove(item)) {
            updateModifiedTime();
        }
    }

    /**
     * Get a child by name
     * 
     * @param name Name of the child
     * @return Child item or null
     */
    public FileSystemItem getChild(String name) {
        for (FileSystemItem child : children) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    public List<FileSystemItem> getChildren() {
        return new ArrayList<>(children);
    }

    @Override
    public long getSize() {
        long total = 0;
        for (FileSystemItem child : children) {
            total += child.getSize();
        }
        return total;
    }

    @Override
    public Map<String, Object> getInfo() {
        Map<String, Object> info = super.getInfo();
        info.put("items", children.size());
        return info;
    }

    @Override
    public String toString() {
        String string = new String("- " + name);
        for (FileSystemItem fileSystemItem : children) {
            string += "\n " + fileSystemItem.toString();
        }
        return string;
    }
}
