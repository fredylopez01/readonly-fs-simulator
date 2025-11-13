package model.exceptions;

/**
 * Exception raised when an item is not found
 * Similar to ENOENT (errno 2) in POSIX systems
 */
public class ItemNotFoundException extends FileSystemException {
    private String itemName;

    public ItemNotFoundException(String itemName) {
        super("Item '" + itemName + "' not found in file system");
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }
}
