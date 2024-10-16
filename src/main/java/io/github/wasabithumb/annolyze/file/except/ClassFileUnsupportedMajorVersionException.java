package io.github.wasabithumb.annolyze.file.except;

/**
 * Thrown when the major version of the class file is too great for the current runtime or library.
 */
public final class ClassFileUnsupportedMajorVersionException extends ClassFileReadException {

    public ClassFileUnsupportedMajorVersionException(String message) {
        super(message);
    }

}
