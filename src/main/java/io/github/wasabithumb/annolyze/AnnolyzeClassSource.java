package io.github.wasabithumb.annolyze;

import io.github.wasabithumb.annolyze.archive.AnnolyzeArchive;
import io.github.wasabithumb.annolyze.file.ClassFile;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.util.List;

/**
 * Utility for reading out the class files from a source.
 * @see AnnolyzeArchive
 */
@ApiStatus.NonExtendable
public interface AnnolyzeClassSource {

    /**
     * <p>
     *     Returns a subset of the current source including classes that open to the specified package.
     * </p>
     * <p>
     *     For instance, {@code sub("a.b.c").read("D")} is identical to {@code read("a.b.c.D")}
     * </p>
     */
    @NotNull
    AnnolyzeClassSource sub(@NotNull String pkg);

    /**
     * Reads from this source the class skeleton with the given name.
     * @throws io.github.wasabithumb.annolyze.file.except.ClassFileReadException An exception caused by malformed
     * class file data.
     * @throws IOException A generic IO exception from the backing stream.
     */
    @NotNull
    ClassFile read(final @NotNull String className) throws IOException;

    /**
     * Lists all classes in this source.
     * @param recursive If false, only top-level classes will be returned.
     * @throws IOException A generic IO exception.
     */
    @NotNull @Unmodifiable
    List<String> list(final boolean recursive) throws IOException;

    /**
     * Lists all classes in this source. Alias for {@code list(true)}.
     * @see #list(boolean)
     * @throws IOException A generic IO exception.
     */
    default @NotNull @Unmodifiable List<String> list() throws IOException {
        return this.list(true);
    }

    /**
     * Reads all class skeletons in this source.
     * @param recursive If false, only top-level classes will be read.
     * @see #list(boolean)
     * @throws io.github.wasabithumb.annolyze.file.except.ClassFileReadException An exception caused by malformed
     * class file data.
     * @throws IOException A generic IO exception from the backing stream.
     */
    @NotNull @Unmodifiable List<ClassFile> readAll(final boolean recursive) throws IOException;

    /**
     * Reads all class skeletons in this source. Alias for {@code readAll(true)}.
     * @see #readAll(boolean)
     * @see #list()
     * @throws io.github.wasabithumb.annolyze.file.except.ClassFileReadException An exception caused by malformed
     * class file data.
     * @throws IOException A generic IO exception from the backing stream.
     */
    default @NotNull @Unmodifiable List<ClassFile> readAll() throws IOException {
        return this.readAll(true);
    }

}
