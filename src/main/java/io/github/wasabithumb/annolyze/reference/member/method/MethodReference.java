package io.github.wasabithumb.annolyze.reference.member.method;

import io.github.wasabithumb.annolyze.reference.member.MemberReference;
import io.github.wasabithumb.annolyze.reference.type.TypeReference;
import io.github.wasabithumb.annolyze.reference.type.boxed.ClassReference;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

@ApiStatus.NonExtendable
public interface MethodReference extends MemberReference<Method> {

    static @NotNull MethodReference of(
            @NotNull ClassReference declaringClass,
            @NotNull String name,
            @NotNull String descriptor,
            int accessFlags
    ) throws IllegalArgumentException {
        return new NotationMethodReference(declaringClass, name, descriptor, accessFlags);
    }

    static @NotNull MethodReference of(@NotNull Method method) {
        return new DirectMethodReference(method);
    }

    @ApiStatus.Internal
    static @NotNull MethodReference dummy(@NotNull String name, @NotNull TypeReference @NotNull ... parameterTypes) {
        return new DummyMethodReference(name, parameterTypes);
    }

    //

    @Override
    @NotNull MethodAccessFlags flags();

    /**
     * The name of this method.
     */
    @Override
    @NotNull String name();

    /**
     * The return type of this method.
     * May be {@link io.github.wasabithumb.annolyze.reference.type.primitive.PrimitiveReference#VOID VOID}.
     */
    @NotNull TypeReference returnType();

    /**
     * The parameter types of this method.
     */
    @NotNull TypeReference[] parameterTypes();

    @ApiStatus.Internal
    default @NotNull String parameterDescriptor() {
        StringBuilder ret = new StringBuilder();
        ret.append('(');
        for (TypeReference tr : this.parameterTypes())
            ret.append(tr);
        ret.append(')');
        return ret.toString();
    }

    @Override
    default @NotNull String descriptor() {
        return this.parameterDescriptor() + this.returnType();
    }

    @Contract(" -> false")
    @Override
    default boolean isField() {
        return false;
    }

    @Contract(" -> true")
    @Override
    default boolean isMethod() {
        return true;
    }

}
