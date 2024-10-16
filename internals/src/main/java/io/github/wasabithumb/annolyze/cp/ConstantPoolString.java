package io.github.wasabithumb.annolyze.cp;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * A string or reference to a string in the {@link ConstantPool constant pool}.
 */
@ApiStatus.Internal
public sealed interface ConstantPoolString {

    static @NotNull ConstantPoolString of(@NotNull String value) {
        return new ConstantPoolString.Literal(value);
    }

    static @NotNull ConstantPoolString of(int... target) {
        if (target.length == 0) throw new IllegalArgumentException("Targets may not be empty");
        return new ConstantPoolString.Reference(target);
    }

    //

    int[] target() throws UnsupportedOperationException;

    @NotNull String value() throws UnsupportedOperationException;

    boolean isLiteral();

    //

    record Literal(@NotNull String value) implements ConstantPoolString {

        @Contract("-> fail")
        @Override
        public int[] target() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isLiteral() {
            return true;
        }

        @Override
        public String toString() {
            return "Literal  [" + this.value + "]";
        }
    }

    record Reference(int[] target) implements ConstantPoolString {

        @Contract("-> fail")
        @Override
        public @NotNull String value() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isLiteral() {
            return false;
        }

        @Override
        public String toString() {
            return "Reference" + Arrays.toString(this.target);
        }
    }

}
