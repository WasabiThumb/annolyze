package io.github.wasabithumb.annolyze.reference.member.field;

import io.github.wasabithumb.annolyze.reference.member.MemberReference;
import io.github.wasabithumb.annolyze.reference.type.TypeReference;
import io.github.wasabithumb.annolyze.reference.type.boxed.ClassReference;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

@ApiStatus.NonExtendable
public interface FieldReference extends MemberReference<Field> {

    static @NotNull FieldReference of(
            @NotNull ClassReference parentClass,
            @NotNull String name,
            @NotNull String descriptor,
            int accessFlags
    ) throws IllegalArgumentException {
        return new NotationFieldReference(parentClass, name, descriptor, accessFlags);
    }

    static @NotNull FieldReference of(@NotNull Field field) {
        return new DirectFieldReference(field);
    }

    //

    @Override
    @NotNull FieldAccessFlags flags();

    /**
     * The name of this field.
     */
    @Override
    @NotNull String name();

    /**
     * Returns a reference to the type of this field.
     */
    @NotNull TypeReference type();

    /**
     * Alias for {@code type().toString()}
     * @see #type()
     */
    @Override
    default @NotNull String descriptor() {
        return this.type().toString();
    }

    @Contract(" -> true")
    @Override
    default boolean isField() {
        return true;
    }

    @Contract(" -> false")
    @Override
    default boolean isMethod() {
        return false;
    }

}
