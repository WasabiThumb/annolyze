package io.github.wasabithumb.annolyze.reference.member.method;

import io.github.wasabithumb.annolyze.reference.type.TypeReference;
import io.github.wasabithumb.annolyze.reference.type.boxed.ClassReference;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.Objects;

@ApiStatus.Internal
abstract class AbstractMethodReference implements MethodReference {

    protected final ClassReference declaringClass;
    protected final String name;
    protected AbstractMethodReference(@NotNull ClassReference declaringClass, @NotNull String name) {
        this.declaringClass = declaringClass;
        this.name = name;
    }

    @Override
    public @NotNull ClassReference declaringClass() {
        return this.declaringClass;
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }

    @Override
    public @NotNull Method resolve() throws ReflectiveOperationException {
        final TypeReference[] parameterTypeReferences = this.parameterTypes();
        final int parameterTypeCount = parameterTypeReferences.length;
        final Class<?>[] parameterTypes = new Class<?>[parameterTypeCount];
        for (int i=0; i < parameterTypes.length; i++)
            parameterTypes[i] = parameterTypeReferences[i].resolve();
        return this.declaringClass.resolve().getDeclaredMethod(this.name, parameterTypes);
    }

    @Override
    public @NotNull Method resolve(boolean initialize, @NotNull ClassLoader loader) throws ReflectiveOperationException {
        final TypeReference[] parameterTypeReferences = this.parameterTypes();
        final int parameterTypeCount = parameterTypeReferences.length;
        final Class<?>[] parameterTypes = new Class<?>[parameterTypeCount];
        for (int i=0; i < parameterTypes.length; i++)
            parameterTypes[i] = parameterTypeReferences[i].resolve(initialize, loader);
        return this.declaringClass.resolve(initialize, loader).getDeclaredMethod(this.name, parameterTypes);
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
