package io.github.wasabithumb.annolyze.reference.member.field;

import io.github.wasabithumb.annolyze.reference.type.boxed.ClassReference;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Objects;

@ApiStatus.Internal
abstract class AbstractFieldReference implements FieldReference {

    protected final ClassReference parentClass;
    protected final String name;
    protected AbstractFieldReference(@NotNull ClassReference parentClass, @NotNull String name) {
        this.parentClass = parentClass;
        this.name = name;
    }

    @Override
    public @NotNull ClassReference declaringClass() {
        return this.parentClass;
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }

    @Override
    public @NotNull Field resolve() throws ReflectiveOperationException {
        return this.parentClass.resolve().getDeclaredField(this.name);
    }

    @Override
    public @NotNull Field resolve(boolean initialize, @NotNull ClassLoader loader) throws ReflectiveOperationException {
        return this.parentClass.resolve(initialize, loader).getDeclaredField(this.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.parentClass, this.name, this.descriptor());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof FieldReference other) {
            if (Objects.equals(this.name(), other.name()) &&
                    Objects.equals(this.descriptor(), other.descriptor())
            ) return true;
        }
        return super.equals(obj);
    }

}
