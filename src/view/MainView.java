package view;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Main application view using Java Swing
 */
public class MainView extends JFrame {
    // Components
    private JCheckBox readOnlyCheckBox;
    private JLabel modeLabel;
    private JLabel statsLabel;
    private JTree fileTree;
    private DefaultTreeModel treeModel;
    private JTextArea logTextArea;
    private JLabel statusLabel;

    // Callbacks (will be set by presenter)
    private Runnable onCreateFile;
    private Runnable onCreateFolder;
    private Runnable onDeleteItem;
    private Runnable onModifyFile;
    private Runnable onRenameItem;
    private Consumer<Boolean> onToggleReadOnly;
    private Runnable onRefresh;
    private Runnable onClearLog;

    /**
     * Initialize the main window and widgets
     */
    public MainView() {
        super("Read-Only File System Simulator");
        setupFrame();
        createComponents();
        layoutComponents();
    }

    /**
     * Setup main frame properties
     */
    private void setupFrame() {
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * Create all UI components
     */
    private void createComponents() {
        // Create components
        readOnlyCheckBox = new JCheckBox("Read-Only Mode (Block all modifications)");
        readOnlyCheckBox.addActionListener(e -> handleReadOnlyToggle());

        modeLabel = new JLabel("● READ-WRITE MODE");
        modeLabel.setFont(new Font("Arial", Font.BOLD, 12));

        statsLabel = new JLabel("Items: 0 | Files: 0 | Folders: 0 | Size: 0 B");

        // File tree
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Loading...");
        treeModel = new DefaultTreeModel(root);
        fileTree = new JTree(treeModel);
        fileTree.setRootVisible(true);

        // Log text area
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Courier New", Font.PLAIN, 10));

        // Status label
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
    }

    /**
     * Layout all components
     */
    private void layoutComponents() {
        setLayout(new BorderLayout(5, 5));

        // Top panel - Control panel
        JPanel topPanel = createControlPanel();
        add(topPanel, BorderLayout.NORTH);

        // Center - Split pane with file tree and log
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(createFilePanel());
        splitPane.setRightComponent(createLogPanel());
        splitPane.setDividerLocation(600);
        add(splitPane, BorderLayout.CENTER);

        // Bottom - Status bar
        add(statusLabel, BorderLayout.SOUTH);

        // Menu bar
        setJMenuBar(createMenuBar());
    }

    /**
     * Create control panel with mode toggle and statistics
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new BorderLayout(1, 1));
        panel.setBorder(BorderFactory.createTitledBorder("Control Panel"));

        // Left side - checkbox and mode label
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(readOnlyCheckBox);
        leftPanel.add(modeLabel);

        // Right side - statistics
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(statsLabel);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    /**
     * Create file tree panel with operation buttons
     */
    private JPanel createFilePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("File System"));

        // Tree with scroll pane
        JScrollPane treeScroll = new JScrollPane(fileTree);
        panel.add(treeScroll, BorderLayout.CENTER);

        // Operation buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(createButton("➕ Create File", e -> handleCreateFile()));
        buttonPanel.add(createButton("📁 Create Folder", e -> handleCreateFolder()));
        buttonPanel.add(createButton("✏️ Modify", e -> handleModify()));
        buttonPanel.add(createButton("🖊️ Rename", e -> handleRename()));
        buttonPanel.add(createButton("🗑️ Delete", e -> handleDelete()));
        buttonPanel.add(createButton("🔄 Refresh", e -> handleRefresh()));

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Create log panel with controls
     */
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Operation Log"));

        // Log text area with scroll
        JScrollPane logScroll = new JScrollPane(logTextArea);
        panel.add(logScroll, BorderLayout.CENTER);

        // Log control buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(createButton("Clear Log", e -> handleClearLog()));

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Create menu bar
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(createMenuItem("Refresh", e -> handleRefresh()));
        fileMenu.addSeparator();
        fileMenu.add(createMenuItem("Exit", e -> System.exit(0)));
        menuBar.add(fileMenu);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(createMenuItem("About", e -> showAbout()));
        menuBar.add(helpMenu);

        return menuBar;
    }

    /**
     * Create a button with action listener
     */
    private JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    /**
     * Create a menu item with action listener
     */
    private JMenuItem createMenuItem(String text, ActionListener listener) {
        JMenuItem item = new JMenuItem(text);
        item.addActionListener(listener);
        return item;
    }

    // Event handlers
    private void handleReadOnlyToggle() {
        if (onToggleReadOnly != null) {
            onToggleReadOnly.accept(readOnlyCheckBox.isSelected());
        }
    }

    private void handleCreateFile() {
        if (onCreateFile != null)
            onCreateFile.run();
    }

    private void handleCreateFolder() {
        if (onCreateFolder != null)
            onCreateFolder.run();
    }

    private void handleDelete() {
        if (onDeleteItem != null)
            onDeleteItem.run();
    }

    private void handleModify() {
        if (onModifyFile != null)
            onModifyFile.run();
    }

    private void handleRename() {
        if (onRenameItem != null)
            onRenameItem.run();
    }

    private void handleRefresh() {
        if (onRefresh != null)
            onRefresh.run();
    }

    private void handleClearLog() {
        if (onClearLog != null)
            onClearLog.run();
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "Read-Only File System Simulator\n\n" +
                        "Simulates SquashFS, ISO 9660, and CRAMFS behavior\n" +
                        "Built with Java and Swing\n" +
                        "MVP Architecture Pattern\n\n" +
                        "© 2025",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Public methods for presenter to update view

    /**
     * Update the read-only mode indicator
     */
    public void updateModeIndicator(boolean isReadOnly) {
        if (isReadOnly) {
            modeLabel.setText("🔒 READ-ONLY MODE");
            modeLabel.setForeground(Color.RED);
        } else {
            modeLabel.setText("● READ-WRITE MODE");
            modeLabel.setForeground(new Color(10, 180, 0));
        }
    }

    /**
     * Update the file tree display
     */
    public void updateFileTree(Map<String, Object> rootData, List<Map<String, Object>> items) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(
                rootData.get("name") + " [" + rootData.get("type") + "]");

        // Build tree structure (presenter will provide structured data)
        for (Map<String, Object> item : items) {
            addTreeNode(root, item);
        }

        treeModel.setRoot(root);
        expandAllNodes(fileTree, 0, fileTree.getRowCount());
    }

    /**
     * Add a tree node (helper for building tree)
     */
    private void addTreeNode(DefaultMutableTreeNode parent, Map<String, Object> itemData) {
        String label = String.format("%s [%s - %s]",
                itemData.get("name"),
                itemData.get("type"),
                itemData.get("size"));
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(label);
        parent.add(node);
    }

    /**
     * Expand all tree nodes
     */
    private void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }
        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }

    /**
     * Update the log display
     */
    public void updateLog(List<String> entries) {
        logTextArea.setText("");
        for (String entry : entries) {
            logTextArea.append(entry + "\n");
        }
        // Auto-scroll to bottom
        logTextArea.setCaretPosition(logTextArea.getDocument().getLength());
    }

    /**
     * Update statistics display
     */
    public void updateStatistics(Map<String, Object> stats) {
        statsLabel.setText(String.format(
                "Items: %d | Files: %d | Folders: %d | Size: %s",
                stats.get("total_items"),
                stats.get("files"),
                stats.get("folders"),
                formatSize((Long) stats.get("total_size"))));
    }

    /**
     * Format size in bytes to human-readable format
     */
    private String formatSize(long sizeBytes) {
        String[] units = { "B", "KB", "MB", "GB", "TB" };
        double size = sizeBytes;
        int unitIndex = 0;

        while (size >= 1024.0 && unitIndex < units.length - 1) {
            size /= 1024.0;
            unitIndex++;
        }

        return String.format("%.1f %s", size, units[unitIndex]);
    }

    /**
     * Update status bar message
     */
    public void updateStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * Get the currently selected item path from tree
     */
    public String getSelectedItemPath() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
        if (node == null) {
            return null;
        }
        return node.getUserObject().toString();
    }

    /**
     * Show error message dialog
     */
    public void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show info message dialog
     */
    public void showInfo(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Ask user for string input
     */
    public String askString(String title, String prompt, String initialValue) {
        return (String) JOptionPane.showInputDialog(
                this, prompt, title,
                JOptionPane.PLAIN_MESSAGE, null, null, initialValue);
    }

    /**
     * Ask user for multiline text input
     */
    public String askText(String title, String prompt, String initialValue) {
        JTextArea textArea = new JTextArea(initialValue, 15, 50);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);

        int result = JOptionPane.showConfirmDialog(
                this, scrollPane, title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            return textArea.getText();
        }
        return null;
    }

    /**
     * Show confirmation dialog
     */
    public boolean confirm(String title, String message) {
        int result = JOptionPane.showConfirmDialog(
                this, message, title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    // Setters for callbacks
    public void setOnCreateFile(Runnable callback) {
        this.onCreateFile = callback;
    }

    public void setOnCreateFolder(Runnable callback) {
        this.onCreateFolder = callback;
    }

    public void setOnDeleteItem(Runnable callback) {
        this.onDeleteItem = callback;
    }

    public void setOnModifyFile(Runnable callback) {
        this.onModifyFile = callback;
    }

    public void setOnRenameItem(Runnable callback) {
        this.onRenameItem = callback;
    }

    public void setOnToggleReadOnly(Consumer<Boolean> callback) {
        this.onToggleReadOnly = callback;
    }

    public void setOnRefresh(Runnable callback) {
        this.onRefresh = callback;
    }

    public void setOnClearLog(Runnable callback) {
        this.onClearLog = callback;
    }
}
