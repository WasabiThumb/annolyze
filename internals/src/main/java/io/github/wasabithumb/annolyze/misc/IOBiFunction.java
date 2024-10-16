package io.github.wasabithumb.annolyze.misc;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@ApiStatus.Internal
@FunctionalInterface
public interface IOBiFunction<A1, A2, B> {

    @NotNull B apply(@NotNull A1 arg1, @NotNull A2 arg2) throws IOException;

}
