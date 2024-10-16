package io.github.wasabithumb.annolyze.reference;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Indicates a UTF-8 value retrieved from a class file which would require reflection to resolve.
 * Not to be confused with Java's {@link java.lang.ref.Reference}.
 * This is not an entry point.
 */
@ApiStatus.NonExtendable
public interface Reference<T> {

    /**
     * The name of this reference. The context of this name depends on the nature of the reference.
     */
    @NotNull String name();

    /**
     * Resolves the reference using the context class loader. Some subtypes may use memoization.
     */
    @NotNull T resolve() throws ReflectiveOperationException;

    /**
     * Resolves the reference with parameters conforming to {@link Class#forName(String, boolean, ClassLoader)}.
     * No subtypes may use memoization.
     */
    @NotNull T resolve(boolean initialize, @NotNull ClassLoader loader) throws ReflectiveOperationException;

}
