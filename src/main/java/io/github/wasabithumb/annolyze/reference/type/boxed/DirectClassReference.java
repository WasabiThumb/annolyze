package io.github.wasabithumb.annolyze.reference.type.boxed;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
final class DirectClassReference extends AbstractClassReference {

    private final Class<?> provided;
    public DirectClassReference(Class<?> value) {
        if (value.isArray()) throw new IllegalArgumentException("Cannot pass array type to DirectClassReference");
        this.provided = value;
        this.setLocalResolution(value);
    }

    @Override
    public @NotNull String name() {
        return this.provided.getName();
    }

    @Override
    public @NotNull String simpleName() {
        return this.provided.getSimpleName();
    }

    @Override
    public @NotNull String toString() {
        final String name = this.provided.getName();
        StringBuilder ret = new StringBuilder(name.length() + 2);
        ret.append('L');

        char c;
        for (int i=0; i < name.length(); i++) {
            c = name.charAt(i);
            if (c == '.') {
                c = '/';
            }
            ret.append(c);
        }

        ret.append(';');
        return ret.toString();
    }

}
