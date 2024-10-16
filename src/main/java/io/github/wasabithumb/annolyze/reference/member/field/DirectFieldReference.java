package io.github.wasabithumb.annolyze.reference.member.field;

import io.github.wasabithumb.annolyze.reference.type.TypeReference;
import io.github.wasabithumb.annolyze.reference.type.boxed.ClassReference;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

@ApiStatus.Internal
final class DirectFieldReference extends AbstractFieldReference {

    private final TypeReference type;
    private final transient int flags;
    public DirectFieldReference(@NotNull Field field) {
        super(ClassReference.of(field.getDeclaringClass()), field.getName());
        this.type = TypeReference.of(field.getType());
        this.flags = field.getModifiers();
    }

    @Override
    public @NotNull FieldAccessFlags flags() {
        return new FieldAccessFlags(this.flags);
    }

    @Override
    public @NotNull TypeReference type() {
        return this.type;
    }

}
