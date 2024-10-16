package io.github.wasabithumb.annolyze.reference.member.method;

import io.github.wasabithumb.annolyze.reference.type.TypeReference;
import io.github.wasabithumb.annolyze.reference.type.boxed.ClassReference;
import io.github.wasabithumb.annolyze.reference.type.primitive.PrimitiveReference;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * All values are explicitly set, used for it's hashCode & equals collision
 */
@ApiStatus.Internal
final class DummyMethodReference implements MethodReference {

    private final String name;
    private final TypeReference[] parameterTypes;
    public DummyMethodReference(@NotNull String name, @NotNull TypeReference @NotNull [] parameterTypes) {
        this.name = name;
        this.parameterTypes = Arrays.copyOf(parameterTypes, parameterTypes.length);
    }

    @Override
    public @NotNull MethodAccessFlags flags() {
        return new MethodAccessFlags(0);
    }

    @Override
    public @NotNull ClassReference declaringClass() {
        return ClassReference.of(Object.class);
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }

    @Override
    public @NotNull Method resolve() {
        throw new UnsupportedOperationException("Cannot resolve dummy reference");
    }

    @Override
    public @NotNull Method resolve(boolean initialize, @NotNull ClassLoader loader) {
        throw new UnsupportedOperationException("Cannot resolve dummy reference");
    }

    @Override
    public @NotNull TypeReference returnType() {
        return PrimitiveReference.VOID;
    }

    @Override
    public @NotNull TypeReference[] parameterTypes() {
        return Arrays.copyOf(this.parameterTypes, this.parameterTypes.length);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name(), this.parameterDescriptor());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof MethodReference other) {
            if (Objects.equals(this.name(), other.name()) &&
                    Objects.equals(this.parameterDescriptor(), other.parameterDescriptor())
            ) return true;
        }
        return super.equals(obj);
    }

}
