package io.github.wasabithumb.annolyze.file.except;

/**
 * Thrown when a field successfully read by the reader has an invalid value. For instance, this can throw when
 * {@code constant_pool_count} is equal to 0.
 */
public final class ClassFileInvalidDataException extends ClassFileReadException {

    public ClassFileInvalidDataException(String message) {
        super(message);
    }

    public ClassFileInvalidDataException(String message, Throwable cause) {
        super(message, cause);
    }

}
