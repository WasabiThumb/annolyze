package io.github.wasabithumb.annolyze.reference.type;

import io.github.wasabithumb.annolyze.reference.Reference;
import io.github.wasabithumb.annolyze.reference.type.boxed.ClassReference;
import io.github.wasabithumb.annolyze.reference.type.primitive.PrimitiveReference;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Refers to a type. Must be one of PrimitiveReference, ClassReference or ArrayTypeReference.
 */
@ApiStatus.NonExtendable
public interface TypeReference extends Reference<Class<?>>, Comparable<TypeReference> {

    /**
     * Returns a {@link TypeReference} that resolves to the provided class.
     * <ul>
     *     <li>
     *         <strong>When {@link Class#isPrimitive()} returns true</strong>
     *         <p>Calls {@link PrimitiveReference#getByClass(Class)}</p>
     *     </li>
     *     <li>
     *         <strong>When {@link Class#isArray()} returns true</strong>
     *         <p>Calls {@link ArrayTypeReference new ArrayTypeReference()}</p>
     *     </li>
     *     <li>
     *         <strong>Otherwise</strong>
     *         <p>Calls {@link ClassReference#of(Class)}</p>
     *     </li>
     * </ul>
     */
    static @NotNull TypeReference of(@NotNull Class<?> clazz) {
        try {
            if (clazz.isPrimitive()) return PrimitiveReference.getByClass(clazz);
            if (clazz.isArray()) return new ArrayTypeReference(of(clazz.componentType()));
            return ClassReference.of(clazz);
        } catch (IllegalArgumentException e) {
            throw new AssertionError("TypeReference#of should not throw IllegalArgumentException", e);
        }
    }

    /**
     * Parses the provided string in
     * <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3.2">internal JVM notation</a>,
     * returning a reference to the type that it encodes. The string {@code "Z"} can also be used to retrieve
     * {@link PrimitiveReference#VOID}.
     * The string provided may be moved using {@link CharSequence#toString() toString}, changes in the value are not
     * respected after this method is called.
     */
    static @NotNull TypeReference of(@NotNull CharSequence notation) throws IllegalArgumentException {
        final int len = notation.length();
        if (len == 0) throw new IllegalArgumentException("Cannot create TypeReference from empty notation string");

        final char firstChar = notation.charAt(0);
        if (firstChar == '[') return new ArrayTypeReference(of(notation.subSequence(1, notation.length())));

        if (len == 1) return PrimitiveReference.getByCharAssert(firstChar);
        return ClassReference.of(notation);
    }

    //

    /**
     * Provides the fully qualified name of the type that this refers to. For the
     * <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3.2">internal JVM representation</a>
     * of this type, use {@link #toString()}.
     * <ul>
     *     <li>
     *         <strong>PrimitiveReference</strong>
     *         <p>
     *             Provides the symbol used to declare a field of this type,
     *             for instance {@code int} and {@code long}.
     *         </p>
     *     </li>
     *     <li>
     *         <strong>ClassReference</strong>
     *         <p>
     *             Provides the fully qualified name as specified by {@link Class#getName()}.
     *         </p>
     *     </li>
     *     <li>
     *         <strong>ArrayTypeReference</strong>
     *         <p>
     *             Provides the name of the component type suffixed with {@code []}.
     *         </p>
     *     </li>
     * </ul>
     */
    @Override
    @NotNull String name();

    /**
     * Provides the simple name of the type that this refers to, as per {@link Class#getSimpleName()}.
     * This is identical to {@link #name()} except in the case of ClassReference, which will trim out
     * the package that the class is a member of.
     */
    @NotNull String simpleName();

    /**
     * Provides a reference to the type that may be an array member of the type referred to by this ArrayTypeReference.
     * @throws UnsupportedOperationException This reference does not refer to an array type.
     */
    @NotNull TypeReference componentType() throws UnsupportedOperationException;

    /**
     * Provides an ArrayTypeReference that refers to the {@link Class#arrayType() array type} of the type referred
     * to by this reference.
     */
    default @NotNull ArrayTypeReference arrayType() {
        return new ArrayTypeReference(this);
    }

    /**
     * @return True if this is a PrimitiveReference, meaning that it refers to a Java primitive type.
     */
    boolean isPrimitive();

    /**
     * @return True if this is a {@link #isPrimitive() primitive} reference and refers to the "void" pseudo-type.
     */
    default boolean isVoid() {
        return false;
    }

    /**
     * Alias for {@code !isPrimitive()}, and for proper implementations this should mean that either
     * {@link #isClass()} or {@link #isArray()} is true.
     */
    default boolean isComplex() {
        return !this.isPrimitive();
    }

    /**
     * @return True if this is an ArrayTypeReference, meaning that it has a {@link #componentType()}.
     */
    boolean isArray();

    /**
     * This does not return true for array types, see {@link #isArray()} and {@link #isComplex()}.
     * @return True if this is a ClassReference, meaning that it refers to a Java boxed type.
     */
    boolean isClass();

    /**
     * <p>
     *     For primitives, this method returns the primitive type with pure contract
     *     and will not throw. For arrays, this is an alias for {@code .componentType().resolve().arrayType()}.
     *     Otherwise, resolves this type as per {@link Class#forName(String)}.
     * </p>
     * <p>
     *     See {@link #resolveBoxed()} for a variant that always boxes primitive types; where this method would
     *     return {@link Integer#TYPE}, that returns {@code Integer.class}.
     * </p>
     * <p>
     *     Unlike {@link #resolve(boolean, ClassLoader)}, this method <strong>may be memoized</strong> using
     *     {@link ThreadLocal}.
     * </p>
     * @throws ClassNotFoundException A class required to resolve this type was not found.
     */
    @Override
    @NotNull Class<?> resolve() throws ClassNotFoundException;

    /**
     * <p>
     *     For primitives, this method returns the primitive type with pure contract
     *     and will not throw. For arrays, this is an alias for {@code .componentType().resolve(...).arrayType()}.
     *     Otherwise, resolves this type as per {@link Class#forName(String, boolean, ClassLoader)}.
     * </p>
     * <p>
     *     See {@link #resolveBoxed(boolean, ClassLoader)} for a variant that always boxes primitive types; where
     *     this method would return {@link Integer#TYPE}, that returns {@code Integer.class}.
     * </p>
     * <p>
     *     Unlike {@link #resolve()}, this method is <strong>never memoized</strong>.
     * </p>
     * @throws ClassNotFoundException A class required to resolve this type was not found.
     */
    @Override
    @NotNull Class<?> resolve(boolean initialize, @NotNull ClassLoader classLoader) throws ClassNotFoundException;

    /**
     * Similar to {@link #resolve()}, but will never return unboxed primitive types.
     * @throws ClassNotFoundException A class required to resolve this type was not found.
     */
    @NotNull Class<?> resolveBoxed() throws ClassNotFoundException;

    /**
     * Similar to {@link #resolve(boolean, ClassLoader)}, but will never return unboxed primitive types.
     * @throws ClassNotFoundException A class required to resolve this type was not found.
     */
    @NotNull Class<?> resolveBoxed(boolean initialize, @NotNull ClassLoader classLoader) throws ClassNotFoundException;

    /**
     * Provides a string containing the
     * <a href="https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3.2">internal JVM representation</a>
     * of this type. When the reference is constructed from a string, this may be the backing data.
     */
    @NotNull String toString();

    @Override
    default int compareTo(@NotNull TypeReference other) {
        if (this.isArray()) {
            if (other.isArray()) {
                return this.componentType().compareTo(other.componentType());
            } else {
                return 1;
            }
        } else if (other.isArray()) {
            return -1;
        }

        final boolean isClass = this.isClass();
        final boolean otherIsClass = other.isClass();
        if (isClass != otherIsClass) {
            return otherIsClass ? -1 : 1;
        }
        return this.toString().compareTo(other.toString());
    }

}
