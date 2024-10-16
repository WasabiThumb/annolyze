package io.github.wasabithumb.annolyze.reference.type.primitive;

import io.github.wasabithumb.annolyze.reference.type.TypeReference;
import io.github.wasabithumb.annolyze.reference.type.boxed.ClassReference;
import org.jetbrains.annotations.*;

@ApiStatus.NonExtendable
public sealed interface PrimitiveReference extends TypeReference permits PrimitiveReferenceImpl {

    PrimitiveReference BYTE = new PrimitiveReferenceImpl(
            'B',
            "byte",
            Byte.TYPE,
            Byte.class
    );

    PrimitiveReference CHAR = new PrimitiveReferenceImpl(
            'C',
            "char",
            Character.TYPE,
            Character.class
    );

    PrimitiveReference DOUBLE = new PrimitiveReferenceImpl(
            'D',
            "double",
            Double.TYPE,
            Double.class
    );

    PrimitiveReference FLOAT = new PrimitiveReferenceImpl(
            'F',
            "float",
            Float.TYPE,
            Float.class
    );

    PrimitiveReference INT = new PrimitiveReferenceImpl(
            'I',
            "int",
            Integer.TYPE,
            Integer.class
    );

    PrimitiveReference LONG = new PrimitiveReferenceImpl(
            'J',
            "long",
            Long.TYPE,
            Long.class
    );

    PrimitiveReference SHORT = new PrimitiveReferenceImpl(
            'S',
            "short",
            Short.TYPE,
            Short.class
    );

    PrimitiveReference BOOLEAN = new PrimitiveReferenceImpl(
            'Z',
            "boolean",
            Boolean.TYPE,
            Boolean.class
    );

    PrimitiveReference VOID = new PrimitiveReferenceImpl(
            'V',
            "void",
            Void.TYPE,
            Void.class
    );

    static @NotNull PrimitiveReference @NotNull [] values() {
        return new PrimitiveReference[] {
                BYTE, CHAR, DOUBLE, FLOAT, INT,
                LONG, SHORT, BOOLEAN, VOID
        };
    }

    /**
     * Returns the PrimitiveReference corresponding to the given char by JVM notation. For instance,
     * J resolves to LONG. The character V will also resolve to VOID, as specified for method signatures.
     */
    static @Nullable PrimitiveReference getByChar(char c) {
        if (c < 'B' || c > 'Z') return null;
        return PrimitiveReferenceImpl.BY_CHAR[c - 'B'];
    }

    /**
     * A variant of {@link #getByChar(char)} that throws if the char is invalid.
     * @throws IllegalArgumentException The character is not a known binary name symbol (BCDFIJLSZ) or is the class symbol (L)
     */
    static @NotNull PrimitiveReference getByCharAssert(@Range(from=66, to=90) char c) throws IllegalArgumentException {
        PrimitiveReference ref = getByChar(c);
        if (ref == null) throw new IllegalArgumentException("Invalid primitive char: " + c);
        return ref;
    }

    /**
     * Returns the PrimitiveReference corresponding to the given class.
     * @throws IllegalArgumentException The class does not represent a primitive ({@link Class#isPrimitive()} returns false and the class is not a boxed primitive).
     */
    static @NotNull PrimitiveReference getByClass(@NotNull Class<?> clazz) throws IllegalArgumentException {
        if (!clazz.isPrimitive()) {
            for (PrimitiveReference pr : values()) {
                if (pr.resolveBoxed().equals(clazz)) return pr;
            }
            throw new IllegalArgumentException("Class does not represent a boxed primitive");
        }

        final String name = clazz.getName();
        if (name.isEmpty()) throw new AssertionError("Primitive class name is empty");

        switch (name.charAt(0)) {
            case 'b':
                if (Boolean.TYPE.equals(clazz)) return BOOLEAN;
                if (Byte.TYPE.equals(clazz)) return BYTE;
                break;
            case 'c':
                if (Character.TYPE.equals(clazz)) return CHAR;
                break;
            case 'd':
                if (Double.TYPE.equals(clazz)) return DOUBLE;
                break;
            case 'f':
                if (Float.TYPE.equals(clazz)) return FLOAT;
                break;
            case 'i':
                if (Integer.TYPE.equals(clazz)) return INT;
                break;
            case 'l':
                if (Long.TYPE.equals(clazz)) return LONG;
                break;
            case 's':
                if (Short.TYPE.equals(clazz)) return SHORT;
                break;
            case 'v':
                if (Void.TYPE.equals(clazz)) return VOID;
                break;
        }

        throw new AssertionError("No match found for primitive class name \"" + name + "\"");
    }

    //

    @Override
    default @NotNull String simpleName() {
        return this.name();
    }

    /**
     * @throws UnsupportedOperationException Always thrown, a PrimitiveReference may not have a component type.
     */
    @Contract("-> fail")
    @Override
    default @NotNull TypeReference componentType() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot get componentType() of PrimitiveReference");
    }

    @Contract("-> true")
    @Override
    default boolean isPrimitive() {
        return true;
    }

    @Override
    default boolean isVoid() {
        return this.resolve().equals(Void.TYPE);
    }

    @Contract("-> false")
    @Override
    default boolean isArray() {
        return false;
    }

    @Contract("-> false")
    @Override
    default boolean isClass() {
        return false;
    }

    @Contract(pure = true)
    @Override
    @NotNull Class<?> resolve();

    @Contract(pure = true)
    @Override
    @NotNull Class<?> resolveBoxed();

    @Contract(pure = true)
    @Override
    default @NotNull Class<?> resolve(boolean initialize, @NotNull ClassLoader classLoader) {
        return this.resolve();
    }

    @Contract(pure = true)
    @Override
    default @NotNull Class<?> resolveBoxed(boolean initialize, @NotNull ClassLoader classLoader) {
        return this.resolveBoxed();
    }

    @Contract("-> new")
    default @NotNull ClassReference boxed() {
        return ClassReference.of(this.resolveBoxed());
    }

}
