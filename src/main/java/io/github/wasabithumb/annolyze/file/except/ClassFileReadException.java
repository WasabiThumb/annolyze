package io.github.wasabithumb.annolyze.file.except;

import java.io.IOException;

/**
 * The supertype of errors thrown due to an issue while reading a class file.
 * This should not throw due to generic IO errors, rather the data given by the class file is unprocessable
 * for some reason.
 */
public abstract sealed class ClassFileReadException extends IOException permits
        ClassFileMalformedHeaderException,
        ClassFileIncompleteDataException,
        ClassFileUnsupportedMajorVersionException,
        ClassFileInvalidDataException
{

    public ClassFileReadException(String message) {
        super(message);
    }

    public ClassFileReadException(String message, Throwable cause) {
        super(message, cause);
    }

}
