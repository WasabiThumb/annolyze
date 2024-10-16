package io.github.wasabithumb.annolyze.archive;

import io.github.wasabithumb.annolyze.AnnolyzeClassSource;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Locale;

/**
 * Utility for reading out the class files in an archive.
 */
@ApiStatus.NonExtendable
public interface AnnolyzeArchive extends AnnolyzeClassSource {

    @Contract(value = "_ -> new")
    static @NotNull AnnolyzeArchive of(@NotNull File file) throws IllegalArgumentException {
        if (!file.isFile())
            throw new IllegalArgumentException("Path \"" + file + "\" is not a file");

        final String name = file.getName().toLowerCase(Locale.ROOT);
        if (!name.endsWith(".jar") && !name.endsWith(".zip"))
            throw new IllegalArgumentException("File \"" + file + "\" is not an archive (JAR or ZIP)");

        return new AnnolyzeArchiveImpl(file);
    }

    //

    @Override
    @NotNull AnnolyzeArchive sub(@NotNull String pkg);

}
