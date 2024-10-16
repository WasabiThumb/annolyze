package io.github.wasabithumb.annolyze.reference.member.method;

import io.github.wasabithumb.annolyze.reference.type.TypeReference;
import io.github.wasabithumb.annolyze.reference.type.boxed.ClassReference;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

@ApiStatus.Internal
final class DirectMethodReference extends AbstractMethodReference {

    private final TypeReference returnType;
    private final Class<?>[] parameterTypeClasses;
    private final transient int flags;
    public DirectMethodReference(@NotNull Method method) {
        super(ClassReference.of(method.getDeclaringClass()), method.getName());
        this.returnType = TypeReference.of(method.getReturnType());
        this.parameterTypeClasses = method.getParameterTypes();
        this.flags = method.getModifiers();
    }

    @Override
    public @NotNull MethodAccessFlags flags() {
        return new MethodAccessFlags(this.flags);
    }

    @Override
    public @NotNull TypeReference returnType() {
        return this.returnType;
    }

    @Override
    public @NotNull TypeReference @NotNull [] parameterTypes() {
        final int parameterTypeCount = this.parameterTypeClasses.length;
        final TypeReference[] parameterTypes = new TypeReference[parameterTypeCount];
        for (int i=0; i < parameterTypeCount; i++)
            parameterTypes[i] = TypeReference.of(this.parameterTypeClasses[i]);
        return parameterTypes;
    }

}
