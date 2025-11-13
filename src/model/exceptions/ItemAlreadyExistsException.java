package model.exceptions;

/**
 * Exception raised when trying to create an item that already exists
 * Similar to EEXIST (errno 17) in POSIX systems
 */
public class ItemAlreadyExistsException extends FileSystemException {
    private String itemName;

    public ItemAlreadyExistsException(String itemName) {
        super("Item '" + itemName + "' already exists");
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }
}
