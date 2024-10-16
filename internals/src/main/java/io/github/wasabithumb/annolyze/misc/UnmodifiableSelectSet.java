package io.github.wasabithumb.annolyze.misc;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A set which selects from the backing set all values which may be cast to the given class.
 * The backing set MUST not change.
 */
@ApiStatus.Internal
public final class UnmodifiableSelectSet<A, B extends A> extends AbstractSet<B> {

    private static final int UNKNOWN_SIZE = -1;

    private final Set<A> backing;
    private final Class<B> valueType;
    private final Predicate<Object> predicate;
    private int size;
    UnmodifiableSelectSet(
            @NotNull Set<A> backing,
            @NotNull Class<B> valueType,
            @NotNull Predicate<Object> predicate,
            int size
    ) {
        this.backing = backing;
        this.valueType = valueType;
        this.predicate = predicate;
        this.size = size;
    }

    public UnmodifiableSelectSet(
            @NotNull Set<A> backing,
            @NotNull Class<B> valueType,
            @NotNull Predicate<B> predicate
    ) {
        this(
                backing,
                valueType,
                (Object value) -> valueType.isInstance(value) && predicate.test(valueType.cast(value)),
                UNKNOWN_SIZE
        );
    }

    public UnmodifiableSelectSet(@NotNull Set<A> backing, @NotNull Class<B> valueType) {
        this(backing, valueType, valueType::isInstance, UNKNOWN_SIZE);
    }

    //

    @Override
    public synchronized int size() {
        int size = this.size;
        if (size == UNKNOWN_SIZE) {
            size = 0;
            for (A value : this.backing) {
                if (this.valueType.isInstance(value)) size++;
            }
            this.size = size;
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        return this.backing.isEmpty() || this.size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        if (!this.predicate.test(o)) return false;
        return this.backing.contains(o);
    }

    @Override
    public @NotNull Stream<B> stream() {
        return this.backing.stream()
                .filter(this::filter)
                .map(this::map);
    }

    @Override
    public @NotNull Iterator<B> iterator() {
        return this.stream().iterator();
    }

    private boolean filter(A value) {
        return this.predicate.test(value);
    }

    private B map(A value) {
        return this.valueType.cast(value);
    }

}
