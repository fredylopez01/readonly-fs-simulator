package model.exceptions;

/**
 * Exception raised for invalid operations
 * Similar to EINVAL (errno 22) in POSIX systems
 */
public class InvalidOperationException extends FileSystemException {
    public InvalidOperationException(String message) {
        super(message);
    }
}
