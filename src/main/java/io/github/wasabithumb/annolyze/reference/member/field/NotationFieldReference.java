package io.github.wasabithumb.annolyze.reference.member.field;

import io.github.wasabithumb.annolyze.reference.type.TypeReference;
import io.github.wasabithumb.annolyze.reference.type.boxed.ClassReference;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
final class NotationFieldReference extends AbstractFieldReference {

    private final String descriptor;
    private final TypeReference type;
    private final transient int accessFlags;
    public NotationFieldReference(
            @NotNull ClassReference parentClass,
            @NotNull String name,
            @NotNull String descriptor,
            int accessFlags
    ) {
        super(parentClass, name);
        this.descriptor = descriptor;
        this.type = TypeReference.of(descriptor);
        this.accessFlags = accessFlags;
    }

    @Override
    public @NotNull FieldAccessFlags flags() {
        return new FieldAccessFlags(this.accessFlags);
    }

    @Override
    public @NotNull TypeReference type() {
        return this.type;
    }

    @Override
    public @NotNull String descriptor() {
        return this.descriptor;
    }

}
