package io.github.wasabithumb.annolyze.reference.type.boxed;

import io.github.wasabithumb.annolyze.reference.type.TypeReference;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@ApiStatus.NonExtendable
public sealed interface ClassReference extends TypeReference permits AbstractClassReference {

    /**
     * Wraps a {@link Class} into a {@link ClassReference}. This reference will not require resolution when no
     * {@link ClassLoader} is passed into it.
     * This does not handle array types and should not be used for primitive types. For that, see
     * {@link TypeReference#of(Class)}.
     * @throws IllegalArgumentException The provided class is an array type ({@link Class#isArray()} returns true).
     */
    static @NotNull ClassReference of(@NotNull Class<?> value) throws IllegalArgumentException {
        return new DirectClassReference(value);
    }

    /**
     * Returns a ClassReference backed by the provided string, in JVM descriptor notation. For example,
     * {@code of("Ljava/lang/Object;")} would provide a reference that resolves to {@code java.lang.Object.class}.
     * This does not handle array types and should not be used for primitive types. For that, see
     * {@link TypeReference#of(CharSequence)}.
     * @throws IllegalArgumentException The notation is invalid.
     */
    static @NotNull ClassReference of(@NotNull CharSequence notation) throws IllegalArgumentException {
        return new NotationClassReference(notation);
    }

    //

    /**
     * @throws UnsupportedOperationException Always thrown, a ClassReference may not have a component type.
     */
    @Contract("-> fail")
    @Override
    default @NotNull TypeReference componentType() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot get componentType() of ClassReference");
    }

    /**
     * Always {@code false}.
     */
    @Contract("-> false")
    @Override
    default boolean isPrimitive() {
        return false;
    }

    /**
     * Always {@code false}.
     */
    @Contract("-> false")
    @Override
    default boolean isArray() {
        return false;
    }

    /**
     */
    @Contract("-> true")
    @Override
    default boolean isClass() {
        return true;
    }

    /**
     * Alias for {@link #resolve()}.
     */
    @Override
    default @NotNull Class<?> resolveBoxed() throws ClassNotFoundException {
        return this.resolve();
    }

    /**
     * Alias for {@link #resolve(boolean, ClassLoader)}.
     */
    @Override
    default @NotNull Class<?> resolveBoxed(boolean initialize, @NotNull ClassLoader classLoader) throws ClassNotFoundException {
        return this.resolve(initialize, classLoader);
    }

}
