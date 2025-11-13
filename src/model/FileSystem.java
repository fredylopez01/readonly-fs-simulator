package model;

import model.exceptions.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main file system model that manages files and folders
 */
public class FileSystem {
    private Folder root;
    private Folder currentFolder;
    private boolean readOnlyMode;
    private OperationLogger logger;

    /**
     * Initialize the file system with root folder
     */
    public FileSystem() {
        this.root = new Folder("root", null);
        this.currentFolder = root;
        this.readOnlyMode = false;
        this.logger = new OperationLogger("acciones.log");

        // Log initialization
        logger.logOperation("SYSTEM", "File system initialized in read-write mode");

        // Create initial demo structure
        createDemoStructure();
    }

    /**
     * Create initial demo files and folders
     */
    private void createDemoStructure() {
        // Create demo folders
        Folder docsFolder = new Folder("documents", root);
        Folder imagesFolder = new Folder("images", root);
        Folder configFolder = new Folder("config", root);

        root.addChild(docsFolder);
        root.addChild(imagesFolder);
        root.addChild(configFolder);

        // Create demo files
        File readme = new File("readme.txt", "Welcome to Read-Only File System Simulator", docsFolder);
        docsFolder.addChild(readme);

        File configFile = new File("settings.conf", "mode=read-write\nversion=1.0", configFolder);
        configFolder.addChild(configFile);

        logger.logOperation("SYSTEM", "Demo structure created");
    }

    /**
     * Check if file system is in read-only mode
     */
    public boolean isReadOnlyMode() {
        return readOnlyMode;
    }

    /**
     * Enable or disable read-only mode
     * 
     * @param enabled True to enable read-only mode, False to disable
     */
    public void setReadOnlyMode(boolean enabled) {
        boolean oldMode = this.readOnlyMode;
        this.readOnlyMode = enabled;

        String modeStr = enabled ? "READ-ONLY" : "READ-WRITE";
        String oldModeStr = oldMode ? "READ-ONLY" : "READ-WRITE";

        logger.logOperation("MODE_CHANGE",
                String.format("File system changed from %s to %s", oldModeStr, modeStr));
    }

    /**
     * Check if write operations are allowed
     * Similar to may_write() in Linux kernel
     * 
     * @param operation Name of the operation being attempted
     * @throws ReadOnlyException If file system is in read-only mode
     */
    private void checkWritePermission(String operation) throws ReadOnlyException {
        if (readOnlyMode) {
            logger.logOperation("ERROR",
                    String.format("Attempted '%s' in read-only mode - BLOCKED", operation));
            throw new ReadOnlyException(operation);
        }
    }

    /**
     * Create a new file
     * 
     * @param name    File name
     * @param content Initial file content
     * @param parent  Parent folder (uses current folder if null)
     * @return Created file object
     * @throws ReadOnlyException          If in read-only mode
     * @throws ItemAlreadyExistsException If file already exists
     */
    public File createFile(String name, String content, Folder parent)
            throws ReadOnlyException, ItemAlreadyExistsException {
        checkWritePermission("create_file");

        Folder targetFolder = (parent != null) ? parent : (Folder) currentFolder;

        // Check if item already exists
        if (targetFolder.getChild(name) != null) {
            throw new ItemAlreadyExistsException(name);
        }

        // Create file
        File newFile = new File(name, content, targetFolder);
        targetFolder.addChild(newFile);

        logger.logOperation("CREATE_FILE",
                String.format("Created file: %s (%d bytes)", newFile.getPath(), newFile.getSize()));

        return newFile;
    }

    /**
     * Create a new folder
     * 
     * @param name   Folder name
     * @param parent Parent folder (uses current folder if null)
     * @return Created folder object
     * @throws ReadOnlyException          If in read-only mode
     * @throws ItemAlreadyExistsException If folder already exists
     */
    public Folder createFolder(String name, Folder parent)
            throws ReadOnlyException, ItemAlreadyExistsException {
        checkWritePermission("create_folder");

        Folder targetFolder = (parent != null) ? parent : (Folder) currentFolder;

        // Check if item already exists
        if (targetFolder.getChild(name) != null) {
            throw new ItemAlreadyExistsException(name);
        }

        // Create folder
        Folder newFolder = new Folder(name, targetFolder);
        targetFolder.addChild(newFolder);

        logger.logOperation("CREATE_FOLDER",
                String.format("Created folder: %s", newFolder.getPath()));

        return newFolder;
    }

    /**
     * Delete a file or folder
     * 
     * @param item Item to delete
     * @throws ReadOnlyException         If in read-only mode
     * @throws InvalidOperationException If trying to delete root
     */
    public void deleteItem(FileSystemItem item)
            throws ReadOnlyException, InvalidOperationException {
        checkWritePermission("delete_item");

        if (item == root) {
            throw new InvalidOperationException("Cannot delete root folder");
        }

        if (item.getParent() != null) {
            String itemPath = item.getPath();
            ((Folder) item.getParent()).removeChild(item);

            logger.logOperation("DELETE",
                    String.format("Deleted %s: %s", item.getItemType().getValue(), itemPath));
        }
    }

    /**
     * Modify file content
     * 
     * @param file       File to modify
     * @param newContent New content
     * @throws ReadOnlyException If in read-only mode
     */
    public void modifyFile(File file, String newContent) throws ReadOnlyException {
        checkWritePermission("modify_file");

        long oldSize = file.getSize();
        file.setContent(newContent);
        long newSize = file.getSize();

        logger.logOperation("MODIFY_FILE",
                String.format("Modified file: %s (size: %d -> %d bytes)",
                        file.getPath(), oldSize, newSize));
    }

    /**
     * Rename a file or folder
     * 
     * @param item    Item to rename
     * @param newName New name
     * @throws ReadOnlyException          If in read-only mode
     * @throws ItemAlreadyExistsException If new name already exists
     */
    public void renameItem(FileSystemItem item, String newName)
            throws ReadOnlyException, ItemAlreadyExistsException {
        checkWritePermission("rename_item");

        if (item.getParent() != null) {
            Folder parent = (Folder) item.getParent();
            if (parent.getChild(newName) != null) {
                throw new ItemAlreadyExistsException(newName);
            }
        }

        String oldPath = item.getPath();
        item.setName(newName);

        logger.logOperation("RENAME",
                String.format("Renamed %s: %s -> %s",
                        item.getItemType().getValue(), oldPath, item.getPath()));
    }

    /**
     * Get all items in a folder recursively
     * 
     * @param folder Folder to list (uses root if null)
     * @return List of all items
     */
    public List<FileSystemItem> getAllItems(Folder folder) {
        Folder targetFolder = (folder != null) ? folder : root;
        List<FileSystemItem> items = new ArrayList<>();

        for (FileSystemItem child : targetFolder.getChildren()) {
            items.add(child);
            if (child instanceof Folder) {
                items.addAll(getAllItems((Folder) child));
            }
        }

        return items;
    }

    /**
     * Get all log entries
     */
    public List<String> getLogEntries() {
        return logger.getEntries();
    }

    /**
     * Clear the operation log
     */
    public void clearLog() {
        logger.clear();
        logger.logOperation("SYSTEM", "Log cleared");
    }

    /**
     * Get file system statistics
     * 
     * @return Map with statistics
     */
    public Map<String, Object> getStatistics() {
        List<FileSystemItem> allItems = getAllItems(null);
        long fileCount = allItems.stream()
                .filter(item -> item instanceof File)
                .count();
        long folderCount = allItems.stream()
                .filter(item -> item instanceof Folder)
                .count();
        long totalSize = allItems.stream()
                .filter(item -> item instanceof File)
                .mapToLong(FileSystemItem::getSize)
                .sum();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total_items", allItems.size());
        stats.put("files", fileCount);
        stats.put("folders", folderCount);
        stats.put("total_size", totalSize);
        stats.put("read_only", readOnlyMode);

        return stats;
    }

    /**
     * Get root folder
     */
    public Folder getRoot() {
        return root;
    }

    /**
     * Get current folder
     */
    public Folder getCurrentFolder() {
        return currentFolder;
    }
}