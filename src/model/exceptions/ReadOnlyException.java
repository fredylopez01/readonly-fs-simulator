package model.exceptions;

/**
 * Exception raised when trying to modify file system in read-only mode
 * 
 * This simulates the behavior of real read-only file systems like:
 * - SquashFS: compressed read-only file system
 * - ISO 9660: CD-ROM file system format
 * - CRAMFS: compressed ROM file system
 * 
 * Similar to EROFS (errno 30) in POSIX systems
 */
public class ReadOnlyException extends FileSystemException {
    private String operation;

    public ReadOnlyException(String operation) {
        super(String.format(
                "Operation '%s' not allowed: File system is mounted as read-only. " +
                        "Similar to SquashFS or ISO 9660 behavior.",
                operation));
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }
}
