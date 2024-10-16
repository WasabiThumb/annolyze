package io.github.wasabithumb.annolyze.file.except;

/**
 * Thrown when the reader expects a field of size N due to the specification, however reaches the end of the stream
 * before that can happen.
 */
public final class ClassFileIncompleteDataException extends ClassFileReadException {

    public ClassFileIncompleteDataException(String message) {
        super(message);
    }

    public ClassFileIncompleteDataException(String message, Throwable cause) {
        super(message, cause);
    }

}
