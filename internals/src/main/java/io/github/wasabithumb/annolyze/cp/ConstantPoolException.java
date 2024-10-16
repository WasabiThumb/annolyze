package io.github.wasabithumb.annolyze.cp;

import org.jetbrains.annotations.ApiStatus;

/**
 * Thrown when an illegal access or store is made to the {@link ConstantPool constant pool}.
 */
@ApiStatus.Internal
public class ConstantPoolException extends RuntimeException {

    public ConstantPoolException(String message) {
        super(message);
    }

}
