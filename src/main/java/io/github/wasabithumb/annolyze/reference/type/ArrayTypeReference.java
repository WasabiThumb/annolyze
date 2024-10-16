package io.github.wasabithumb.annolyze.reference.type;

import org.jetbrains.annotations.NotNull;

public record ArrayTypeReference(TypeReference componentType) implements TypeReference {

    @Override
    public @NotNull String name() {
        return this.componentType.name() + "[]";
    }

    @Override
    public @NotNull String simpleName() {
        return this.componentType.simpleName() + "[]";
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isClass() {
        return false;
    }

    @Override
    public @NotNull Class<?> resolve() throws ClassNotFoundException {
        return this.componentType.resolve().arrayType();
    }

    @Override
    public @NotNull Class<?> resolve(boolean initialize, @NotNull ClassLoader classLoader) throws ClassNotFoundException {
        return this.componentType.resolve(initialize, classLoader).arrayType();
    }

    @Override
    public @NotNull Class<?> resolveBoxed() throws ClassNotFoundException {
        return this.componentType.resolveBoxed().arrayType();
    }

    @Override
    public @NotNull Class<?> resolveBoxed(boolean initialize, @NotNull ClassLoader classLoader) throws ClassNotFoundException {
        return this.componentType.resolveBoxed(initialize, classLoader).arrayType();
    }

    @Override
    public @NotNull String toString() {
        return "[" + this.componentType;
    }

    @Override
    public int hashCode() {
        return 217 + this.componentType.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ArrayTypeReference other) {
            return this.componentType.equals(other.componentType);
        }
        return false;
    }

}
