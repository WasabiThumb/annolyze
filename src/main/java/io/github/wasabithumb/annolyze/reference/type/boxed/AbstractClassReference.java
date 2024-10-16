package io.github.wasabithumb.annolyze.reference.type.boxed;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
sealed abstract class AbstractClassReference implements ClassReference permits DirectClassReference, NotationClassReference {

    private final ThreadLocal<Class<?>> localResolution = new ThreadLocal<>();

    protected final void setLocalResolution(@NotNull Class<?> resolution) {
        synchronized (this.localResolution) {
            this.localResolution.set(resolution);
        }
    }

    protected @NotNull Class<?> resolveOnce() throws ClassNotFoundException {
        return Class.forName(this.name());
    }

    //

    @Override
    public @NotNull Class<?> resolve() throws ClassNotFoundException {
        synchronized (this.localResolution) {
            Class<?> value = this.localResolution.get();
            if (value == null) {
                value = this.resolveOnce();
                this.localResolution.set(value);
            }
            return value;
        }
    }

    @Override
    public @NotNull Class<?> resolve(boolean initialize, @NotNull ClassLoader classLoader) throws ClassNotFoundException {
        return Class.forName(this.name(), initialize, classLoader);
    }

    @Override
    public abstract @NotNull String toString();

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof AbstractClassReference other) {
            if (this.toString().equals(other.toString())) return true;
        }
        return super.equals(obj);
    }

}
