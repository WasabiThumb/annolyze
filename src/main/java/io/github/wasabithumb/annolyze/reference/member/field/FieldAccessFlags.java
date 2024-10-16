package io.github.wasabithumb.annolyze.reference.member.field;

import io.github.wasabithumb.annolyze.reference.member.AbstractMemberAccessFlags;
import org.jetbrains.annotations.NotNull;

public final class FieldAccessFlags extends AbstractMemberAccessFlags {

    public static final int ACC_VOLATILE  = 0x0040;
    public static final int ACC_TRANSIENT = 0x0080;
    public static final int ACC_ENUM      = 0x4000;

    public FieldAccessFlags(int value) {
        super(value);
    }

    public boolean isVolatile() {
        return this.check(ACC_VOLATILE);
    }

    public boolean isTransient() {
        return this.check(ACC_TRANSIENT);
    }

    public boolean isEnum() {
        return this.check(ACC_ENUM);
    }

    private static final int ACC_VOLATILE_TRANSIENT = ACC_VOLATILE | ACC_TRANSIENT;

    @Override
    protected @NotNull String extraKeywords() {
        return switch (this.value & ACC_VOLATILE_TRANSIENT) {
            case ACC_VOLATILE -> "volatile";
            case ACC_TRANSIENT -> "transient";
            case ACC_VOLATILE_TRANSIENT -> "volatile transient";
            default -> "";
        };
    }

}
