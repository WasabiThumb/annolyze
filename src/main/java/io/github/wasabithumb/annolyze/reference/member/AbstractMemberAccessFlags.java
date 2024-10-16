package io.github.wasabithumb.annolyze.reference.member;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public abstract class AbstractMemberAccessFlags implements MemberAccessFlags {

    protected final int value;
    protected AbstractMemberAccessFlags(int value) {
        this.value = value;
    }

    @Override
    public int value() {
        return this.value;
    }

    @Override
    public final @NotNull String keywords() {
        final String base = MemberAccessFlags.super.keywords();
        final String extra = this.extraKeywords();
        if (base.isEmpty()) {
            return extra;
        } else if (extra.isEmpty()) {
            return base;
        } else {
            return base + " " + extra;
        }
    }

    protected @NotNull String extraKeywords() {
        return "";
    }

    @Override
    public @NotNull String toString() {
        return "MemberAccessFlags[value=" + this.value + ", keywords=" + this.keywords() + "]";
    }

    @Override
    public int hashCode() {
        return this.value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof MemberAccessFlags other) {
            if (this.value == other.value()) return true;
        }
        return super.equals(obj);
    }

}
