package io.github.wasabithumb.annolyze.reference.type.boxed;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
final class NotationClassReference extends AbstractClassReference {

    private static @Nullable String getNotationError(@NotNull CharSequence notation) {
        final int len = notation.length();
        if (len < 3) return "length < 3";
        if (notation.charAt(0) != 'L') return "[0] != 'L'";
        if (notation.charAt(len - 1) != ';') return "[-1] != ';'";
        if (notation.charAt(1) == '/') return "[1] == '/'";
        if (notation.charAt(len - 2) == '/') return "[-2] == '/'";
        return null;
    }

    @Contract("_ -> param1")
    private static @NotNull CharSequence validateNotation(@NotNull CharSequence notation) throws IllegalArgumentException {
        String err = getNotationError(notation);
        if (err == null) return notation;
        throw new IllegalArgumentException("Invalid class notation \"" + notation + "\" (" + err + ")");
    }

    private final String notation;
    public NotationClassReference(@NotNull CharSequence notation) throws IllegalArgumentException {
        this.notation = validateNotation(notation).toString();
    }

    @Override
    public @NotNull String name() {
        final int len = this.notation.length();
        StringBuilder sb = new StringBuilder(len - 2);
        sb.append(this.notation, 1, len - 1);

        char c;
        for (int i=0; i < sb.length(); i++) {
            c = sb.charAt(i);
            if (c == '/') sb.setCharAt(i, '.');
        }

        return sb.toString();
    }

    @Override
    public @NotNull String simpleName() {
        final int len = this.notation.length();
        char c;
        for (int i=(len - 2); i >= 2; i--) {
            c = this.notation.charAt(i);
            if (c == '/') return this.notation.substring(i + 1, len - 1);
        }
        return this.notation.substring(1, len - 1);
    }

    @Override
    public @NotNull String toString() {
        return this.notation;
    }

}
