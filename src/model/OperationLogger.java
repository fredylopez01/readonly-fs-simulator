package model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Logs all operations performed on the file system
 */
public class OperationLogger {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private String logFile;
    private List<String> entries;

    /**
     * Initialize the logger
     * 
     * @param logFile Path to the log file
     */
    public OperationLogger(String logFile) {
        this.logFile = logFile;
        this.entries = new ArrayList<>();
        initializeLogFile();
    }

    /**
     * Create or clear the log file with header
     */
    private void initializeLogFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {
            writer.write("=".repeat(80) + "\n");
            writer.write("READ-ONLY FILE SYSTEM SIMULATOR - OPERATION LOG\n");
            writer.write("=".repeat(80) + "\n");
            writer.write("Session started: " + LocalDateTime.now().format(TIMESTAMP_FORMATTER) + "\n");
            writer.write("=".repeat(80) + "\n\n");
        } catch (IOException e) {
            System.err.println("Warning: Could not initialize log file: " + e.getMessage());
        }
    }

    /**
     * Log an operation
     * 
     * @param operationType Type of operation (CREATE, DELETE, MODIFY, etc.)
     * @param description   Detailed description of the operation
     */
    public void logOperation(String operationType, String description) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String logEntry = String.format("[%s] [%s] %s", timestamp, operationType, description);

        // Add to memory
        entries.add(logEntry);

        // Write to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(logEntry + "\n");
        } catch (IOException e) {
            System.err.println("Warning: Could not write to log file: " + e.getMessage());
        }
    }

    /**
     * Get all log entries
     * 
     * @return Unmodifiable list of log entries
     */
    public List<String> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Get recent log entries
     * 
     * @param count Number of recent entries to return
     * @return List of recent log entries
     */
    public List<String> getRecentEntries(int count) {
        int size = entries.size();
        if (count >= size) {
            return getEntries();
        }
        return Collections.unmodifiableList(entries.subList(size - count, size));
    }

    /**
     * Clear all log entries (memory only, file is preserved)
     */
    public void clear() {
        entries.clear();
    }

    /**
     * Save current log entries to a file
     * 
     * @param filename Output filename
     * @return true if successful, false otherwise
     */
    public boolean saveToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("=".repeat(80) + "\n");
            writer.write("READ-ONLY FILE SYSTEM SIMULATOR - OPERATION LOG\n");
            writer.write("=".repeat(80) + "\n");
            writer.write("Generated: " + LocalDateTime.now().format(TIMESTAMP_FORMATTER) + "\n");
            writer.write("Total entries: " + entries.size() + "\n");
            writer.write("=".repeat(80) + "\n\n");

            for (String entry : entries) {
                writer.write(entry + "\n");
            }

            return true;
        } catch (IOException e) {
            System.err.println("Error saving log file: " + e.getMessage());
            return false;
        }
    }
}
