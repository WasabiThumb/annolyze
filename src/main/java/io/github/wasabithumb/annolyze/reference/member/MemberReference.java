package io.github.wasabithumb.annolyze.reference.member;

import io.github.wasabithumb.annolyze.reference.Reference;
import io.github.wasabithumb.annolyze.reference.type.boxed.ClassReference;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Member;

/**
 * A {@link Reference} to a {@link Member member} (for this library, that means either a
 * {@link java.lang.reflect.Method method} or a {@link java.lang.reflect.Field field}).
 * This is not an entry point.
 */
@ApiStatus.NonExtendable
public interface MemberReference<T extends Member> extends Reference<T>, Comparable<MemberReference<?>> {

    @NotNull MemberAccessFlags flags();

    boolean isField();

    boolean isMethod();

    /**
     * Returns a reference to the class which this member is declared in.
     */
    @NotNull ClassReference declaringClass();

    /**
     * Returns the descriptor for this member, in the form of {@code <type>} (fields) or
     * {@code (<type>...)<type>} (methods). The hashCode and equals methods of implementations should
     * use this value.
     */
    @NotNull String descriptor();

    @Override
    default int compareTo(@NotNull MemberReference<?> other) {
        final String myName = this.name();
        final String otherName = other.name();

        final boolean myCon = myName.equals("<init>");
        final boolean otherCon = otherName.equals("<init>");
        if (myCon || otherCon) {
            if (myCon == otherCon) return 0;
            return myCon ? -1 : 1;
        }

        int cmp = myName.compareTo(otherName);
        if (cmp == 0) cmp = this.descriptor().compareTo(other.descriptor());
        return cmp;
    }

}
