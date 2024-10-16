package io.github.wasabithumb.annolyze.file.except;

/**
 * Thrown when a class file has a missing or malformed header.
 */
public final class ClassFileMalformedHeaderException extends ClassFileReadException {

    public ClassFileMalformedHeaderException(String message) {
        super(message);
    }

    public ClassFileMalformedHeaderException(String message, Throwable cause) {
        super(message, cause);
    }

}
