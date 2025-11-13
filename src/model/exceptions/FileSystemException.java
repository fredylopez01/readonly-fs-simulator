package model.exceptions;

/**
 * Base exception for file system errors
 */
public class FileSystemException extends Exception {
    public FileSystemException(String message) {
        super(message);
    }

    public FileSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}