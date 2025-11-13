package presenter;

import model.*;
import model.exceptions.*;
import view.MainView;

import java.util.*;

/**
 * Presenter in the MVP pattern
 */
public class MainPresenter {
    private MainView view;
    private FileSystem model;
    private Map<String, FileSystemItem> itemMap;

    public MainPresenter(MainView view, FileSystem model) {
        this.view = view;
        this.model = model;
        this.itemMap = new HashMap<>();

        connectViewEvents();

        refreshAll();
    }

    public void run() {
        this.view.setVisible(true);
    }

    /**
     * Connect view events to presenter methods
     */
    private void connectViewEvents() {
        view.setOnCreateFile(this::handleCreateFile);
        view.setOnCreateFolder(this::handleCreateFolder);
        view.setOnDeleteItem(this::handleDeleteItem);
        view.setOnModifyFile(this::handleModifyFile);
        view.setOnRenameItem(this::handleRenameItem);
        view.setOnToggleReadOnly(this::handleToggleReadOnly);
        view.setOnRefresh(this::handleRefresh);
        view.setOnClearLog(this::handleClearLog);
    }

    /**
     * Refresh all view components
     */
    private void refreshAll() {
        updateFileTree();
        updateLog();
        updateStatistics();
        updateModeIndicator();
        view.updateStatus("Ready");
    }

    /**
     * Update the file tree display
     */
    private void updateFileTree() {
        itemMap.clear();

        Folder root = model.getRoot();
        Map<String, Object> rootData = root.getInfo();

        List<Map<String, Object>> items = new ArrayList<>();
        buildTreeItems(root, items);

        view.updateFileTree(rootData, items);
    }

    /**
     * Recursively build tree items for display
     */
    private void buildTreeItems(FileSystemItem item, List<Map<String, Object>> items) {
        String itemId = generateItemId(item);
        itemMap.put(itemId, item);

        Map<String, Object> info = item.getInfo();
        info.put("id", itemId);
        items.add(info);

        if (item instanceof Folder) {
            Folder folder = (Folder) item;
            for (FileSystemItem child : folder.getChildren()) {
                buildTreeItems(child, items);
            }
        }
    }

    /**
     * Generate a unique ID for tree item
     */
    private String generateItemId(FileSystemItem item) {
        return item.getPath();
    }

    /**
     * Update the log display
     */
    private void updateLog() {
        List<String> entries = model.getLogEntries();
        // Show last 100 entries
        int size = entries.size();
        List<String> recentEntries = (size > 100)
                ? entries.subList(size - 100, size)
                : entries;
        view.updateLog(recentEntries);
    }

    /**
     * Update statistics display
     */
    private void updateStatistics() {
        Map<String, Object> stats = model.getStatistics();
        view.updateStatistics(stats);
    }

    /**
     * Update read-only mode indicator
     */
    private void updateModeIndicator() {
        view.updateModeIndicator(model.isReadOnlyMode());
    }

    /**
     * Get the currently selected item
     */
    private FileSystemItem getSelectedItem() throws ItemNotFoundException {
        String selectedPath = view.getSelectedItemPath();
        if (selectedPath == null) {
            throw new ItemNotFoundException("No item selected");
        }

        // Extract actual path from label (format: "name [type - size]")
        String path = selectedPath.split(" \\[")[0];

        for (FileSystemItem item : itemMap.values()) {
            if (item.getName().equals(path) || item.getPath().endsWith("/" + path)) {
                return item;
            }
        }

        throw new ItemNotFoundException("Selected item not found: " + selectedPath);
    }

    /**
     * Handle create file request
     */
    private void handleCreateFile() {
        try {
            // Get file name from user
            String name = view.askString("Create File", "Enter file name:", "");
            if (name == null || name.trim().isEmpty()) {
                return;
            }

            // Get content from user
            String content = view.askText("File Content",
                    "Enter file content (optional):", "");
            if (content == null) {
                content = "";
            }

            // Determine parent folder
            Folder parent = model.getRoot();
            try {
                FileSystemItem selected = getSelectedItem();
                if (selected instanceof Folder) {
                    parent = (Folder) selected;
                } else if (selected.getParent() != null) {
                    parent = (Folder) selected.getParent();
                }
            } catch (ItemNotFoundException e) {
                // Use root if no selection
            }

            // Create file
            model.createFile(name, content, parent);

            // Update view
            refreshAll();
            view.updateStatus("File '" + name + "' created successfully");
            view.showInfo("Success", "File '" + name + "' created successfully");

        } catch (ReadOnlyException e) {
            view.showError("Read-Only Mode", e.getMessage());
            view.updateStatus("Operation blocked: Read-only mode active");

        } catch (ItemAlreadyExistsException e) {
            view.showError("Item Exists", e.getMessage());
            view.updateStatus("Error: " + e.getMessage());

        } catch (Exception e) {
            view.showError("Error", "Failed to create file: " + e.getMessage());
            view.updateStatus("Error: " + e.getMessage());
        }
    }

    /**
     * Handle create folder request
     */
    private void handleCreateFolder() {
        try {
            // Get folder name from user
            String name = view.askString("Create Folder", "Enter folder name:", "");
            if (name == null || name.trim().isEmpty()) {
                return;
            }

            // Determine parent folder
            Folder parent = model.getRoot();
            try {
                FileSystemItem selected = getSelectedItem();
                if (selected instanceof Folder) {
                    parent = (Folder) selected;
                } else if (selected.getParent() != null) {
                    parent = (Folder) selected.getParent();
                }
            } catch (ItemNotFoundException e) {
                // Use root if no selection
            }

            // Create folder
            model.createFolder(name, parent);

            // Update view
            refreshAll();
            view.updateStatus("Folder '" + name + "' created successfully");
            view.showInfo("Success", "Folder '" + name + "' created successfully");

        } catch (ReadOnlyException e) {
            view.showError("Read-Only Mode", e.getMessage());
            view.updateStatus("Operation blocked: Read-only mode active");

        } catch (ItemAlreadyExistsException e) {
            view.showError("Item Exists", e.getMessage());
            view.updateStatus("Error: " + e.getMessage());

        } catch (Exception e) {
            view.showError("Error", "Failed to create folder: " + e.getMessage());
            view.updateStatus("Error: " + e.getMessage());
        }
    }

    /**
     * Handle delete item request
     */
    private void handleDeleteItem() {
        try {
            // Get selected item
            FileSystemItem item = getSelectedItem();

            // Confirm deletion
            if (!view.confirm("Confirm Delete",
                    "Are you sure you want to delete '" + item.getName() + "'?")) {
                return;
            }

            // Delete item
            String itemName = item.getName();
            model.deleteItem(item);

            // Update view
            refreshAll();
            view.updateStatus("'" + itemName + "' deleted successfully");

        } catch (ReadOnlyException e) {
            view.showError("Read-Only Mode", e.getMessage());
            view.updateStatus("Operation blocked: Read-only mode active");

        } catch (ItemNotFoundException e) {
            view.showError("No Selection", e.getMessage());
            view.updateStatus("Please select an item to delete");

        } catch (InvalidOperationException e) {
            view.showError("Invalid Operation", e.getMessage());
            view.updateStatus("Error: " + e.getMessage());

        } catch (Exception e) {
            view.showError("Error", "Failed to delete item: " + e.getMessage());
            view.updateStatus("Error: " + e.getMessage());
        }
    }

    /**
     * Handle modify file request
     */
    private void handleModifyFile() {
        try {
            // Get selected item
            FileSystemItem item = getSelectedItem();

            // Check if it's a file
            if (!(item instanceof File)) {
                view.showError("Invalid Selection", "Please select a file to modify");
                return;
            }

            File file = (File) item;

            // Get new content from user
            String newContent = view.askText("Modify File",
                    "Edit content of '" + file.getName() + "':",
                    file.getContent());

            if (newContent == null) {
                return;
            }

            // Modify file
            model.modifyFile(file, newContent);

            // Update view
            refreshAll();
            view.updateStatus("File '" + file.getName() + "' modified successfully");
            view.showInfo("Success", "File '" + file.getName() + "' modified successfully");

        } catch (ReadOnlyException e) {
            view.showError("Read-Only Mode", e.getMessage());
            view.updateStatus("Operation blocked: Read-only mode active");

        } catch (ItemNotFoundException e) {
            view.showError("No Selection", e.getMessage());
            view.updateStatus("Please select a file to modify");

        } catch (Exception e) {
            view.showError("Error", "Failed to modify file: " + e.getMessage());
            view.updateStatus("Error: " + e.getMessage());
        }
    }

    /**
     * Handle rename item request
     */
    private void handleRenameItem() {
        try {
            // Get selected item
            FileSystemItem item = getSelectedItem();

            // Get new name from user
            String newName = view.askString("Rename Item",
                    "Enter new name for '" + item.getName() + "':",
                    item.getName());

            if (newName == null || newName.trim().isEmpty() || newName.equals(item.getName())) {
                return;
            }

            // Rename item
            String oldName = item.getName();
            model.renameItem(item, newName);

            // Update view
            refreshAll();
            view.updateStatus("Renamed '" + oldName + "' to '" + newName + "'");
            view.showInfo("Success", "Renamed '" + oldName + "' to '" + newName + "'");

        } catch (ReadOnlyException e) {
            view.showError("Read-Only Mode", e.getMessage());
            view.updateStatus("Operation blocked: Read-only mode active");

        } catch (ItemNotFoundException e) {
            view.showError("No Selection", e.getMessage());
            view.updateStatus("Please select an item to rename");

        } catch (ItemAlreadyExistsException e) {
            view.showError("Name Conflict", e.getMessage());
            view.updateStatus("Error: " + e.getMessage());

        } catch (Exception e) {
            view.showError("Error", "Failed to rename item: " + e.getMessage());
            view.updateStatus("Error: " + e.getMessage());
        }
    }

    /**
     * Handle read-only mode toggle
     */
    private void handleToggleReadOnly(boolean enabled) {
        try {
            model.setReadOnlyMode(enabled);
            updateModeIndicator();
            updateLog();

            String modeStr = enabled ? "READ-ONLY" : "READ-WRITE";
            view.updateStatus("File system mode changed to " + modeStr);

            if (enabled) {
                view.showInfo("Read-Only Mode Activated",
                        "File system is now in read-only mode.\n\n" +
                                "All modification operations (create, delete, modify, rename) " +
                                "are blocked.\n\n" +
                                "This simulates the behavior of SquashFS, ISO 9660, " +
                                "and CRAMFS file systems.");
            }

        } catch (Exception e) {
            view.showError("Error", "Failed to toggle read-only mode: " + e.getMessage());
            view.updateStatus("Error: " + e.getMessage());
        }
    }

    /**
     * Handle refresh request
     */
    private void handleRefresh() {
        try {
            refreshAll();
            view.updateStatus("View refreshed");
        } catch (Exception e) {
            view.showError("Error", "Failed to refresh: " + e.getMessage());
            view.updateStatus("Error: " + e.getMessage());
        }
    }

    /**
     * Handle clear log request
     */
    private void handleClearLog() {
        try {
            if (view.confirm("Clear Log",
                    "Are you sure you want to clear the operation log?")) {
                model.clearLog();
                updateLog();
                view.updateStatus("Log cleared");
            }
        } catch (Exception e) {
            view.showError("Error", "Failed to clear log: " + e.getMessage());
            view.updateStatus("Error: " + e.getMessage());
        }
    }
}