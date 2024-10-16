package io.github.wasabithumb.annolyze.reference.type.primitive;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
final class PrimitiveReferenceImpl implements PrimitiveReference {

    static final PrimitiveReference[] BY_CHAR = new PrimitiveReference[25];

    private final char code;
    private final String name;
    private final Class<?> type;
    private final Class<?> boxedType;
    public PrimitiveReferenceImpl(
            char code,
            @NotNull String name,
            @NotNull Class<?> type,
            @NotNull Class<?> boxedType
    ) {
        BY_CHAR[code - 'B'] = this;
        this.code = code;
        this.name = name;
        this.type = type;
        this.boxedType = boxedType;
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }

    @Override
    public @NotNull Class<?> resolve() {
        return this.type;
    }

    @Override
    public @NotNull Class<?> resolveBoxed() {
        return this.boxedType;
    }

    @Override
    public @NotNull String toString() {
        return String.valueOf(this.code);
    }

    @Override
    public int hashCode() {
        return Character.hashCode(this.code);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof PrimitiveReferenceImpl other) {
            if (this.code == other.code) return true;
        }
        return super.equals(obj);
    }

}
