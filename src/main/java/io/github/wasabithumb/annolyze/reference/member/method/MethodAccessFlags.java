package io.github.wasabithumb.annolyze.reference.member.method;

import io.github.wasabithumb.annolyze.reference.member.AbstractMemberAccessFlags;
import org.jetbrains.annotations.NotNull;

public final class MethodAccessFlags extends AbstractMemberAccessFlags {

    public static final int ACC_SYNCHRONIZED = 0x0020;
    public static final int ACC_BRIDGE       = 0x0040;
    public static final int ACC_VARARGS      = 0x0080;
    public static final int ACC_NATIVE       = 0x0100;
    public static final int ACC_ABSTRACT     = 0x0400;
    public static final int ACC_STRICT       = 0x0800;

    public MethodAccessFlags(int value) {
        super(value);
    }

    public boolean isSynchronized() {
        return this.check(ACC_SYNCHRONIZED);
    }

    public boolean isBridge() {
        return this.check(ACC_BRIDGE);
    }

    public boolean isVarargs() {
        return this.check(ACC_VARARGS);
    }

    public boolean isNative() {
        return this.check(ACC_NATIVE);
    }

    public boolean isAbstract() {
        return this.check(ACC_ABSTRACT);
    }

    public boolean isStrict() {
        return this.check(ACC_STRICT);
    }

    @Override
    protected @NotNull String extraKeywords() {
        StringBuilder ret = new StringBuilder();
        boolean prefix = false;

        if (this.isAbstract()) {
            ret.append("abstract");
            prefix = true;
        }

        if (this.isSynchronized()) {
            if (prefix) ret.append(' ');
            ret.append("synchronized");
            prefix = true;
        }

        if (this.isNative()) {
            if (prefix) ret.append(' ');
            ret.append("native");
        }

        return ret.toString();
    }
}
