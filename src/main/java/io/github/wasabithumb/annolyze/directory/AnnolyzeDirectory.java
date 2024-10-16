package io.github.wasabithumb.annolyze.directory;

import io.github.wasabithumb.annolyze.AnnolyzeClassSource;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Utility for reading out the class files in a directory.
 */
@ApiStatus.NonExtendable
public interface AnnolyzeDirectory extends AnnolyzeClassSource {

    static @NotNull AnnolyzeDirectory of(@NotNull File directory) throws IllegalArgumentException {
        return new AnnolyzeDirectoryImpl(directory);
    }

    //

    @Override
    @NotNull AnnolyzeDirectory sub(@NotNull String pkg);

}
