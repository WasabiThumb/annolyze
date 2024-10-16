package io.github.wasabithumb.annolyze.reference.member;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Describes the access flags common to all {@link MemberReference members}.
 */
@ApiStatus.NonExtendable
public interface MemberAccessFlags {

    // https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.5
    // https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.6

    int ACC_PUBLIC    = 0x0001;
    int ACC_PRIVATE   = 0x0002;
    int ACC_PROTECTED = 0x0004;
    int ACC_STATIC    = 0x0008;
    int ACC_FINAL     = 0x0010;
    int ACC_SYNTHETIC = 0x1000;

    //

    int value();

    default boolean check(int flag) {
        return (this.value() & flag) == flag;
    }

    default boolean isPublic() {
        return this.check(ACC_PUBLIC);
    }

    default boolean isPrivate() {
        return this.check(ACC_PRIVATE);
    }

    default boolean isProtected() {
        return this.check(ACC_PROTECTED);
    }

    default boolean isStatic() {
        return this.check(ACC_STATIC);
    }

    default boolean isFinal() {
        return this.check(ACC_FINAL);
    }

    default boolean isSynthetic() {
        return this.check(ACC_SYNTHETIC);
    }

    /**
     * Returns the Java keywords applied to a field that would result in the flags represented by this object,
     * where applicable.
     */
    default @NotNull String keywords() {
        StringBuilder ret = new StringBuilder();
        boolean prefix = switch (this.value() & 7) {
            case ACC_PUBLIC -> {
                ret.append("public");
                yield true;
            }
            case ACC_PRIVATE -> {
                ret.append("private");
                yield true;
            }
            case ACC_PROTECTED -> {
                ret.append("protected");
                yield true;
            }
            default -> false;
        };
        if (this.isStatic()) {
            if (prefix) ret.append(' ');
            ret.append("static");
            prefix = true;
        }
        if (this.isFinal()) {
            if (prefix) ret.append(' ');
            ret.append("final");
        }
        return ret.toString();
    }

}
