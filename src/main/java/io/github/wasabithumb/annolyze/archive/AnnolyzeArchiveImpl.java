package io.github.wasabithumb.annolyze.archive;

import io.github.wasabithumb.annolyze.file.ClassFile;
import io.github.wasabithumb.annolyze.file.ClassFileInputStream;
import io.github.wasabithumb.annolyze.misc.IOBiFunction;
import static io.github.wasabithumb.annolyze.misc.PathUtil.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Utility for reading out the class files in an archive.
 */
@ApiStatus.Internal
class AnnolyzeArchiveImpl implements AnnolyzeArchive {

    private static final String PACKAGE_INFO = "package-info";

    protected final File file;
    protected final String prefix;

    @ApiStatus.Internal
    protected AnnolyzeArchiveImpl(@NotNull File file, @NotNull String prefix) {
        this.file = file;
        this.prefix = prefix;
    }

    @ApiStatus.Internal
    AnnolyzeArchiveImpl(@NotNull File file) {
        this(file, "");
    }

    //

    @Override
    public @NotNull AnnolyzeArchiveImpl sub(@NotNull String pkg) {
        return new AnnolyzeArchiveImpl(this.file, this.prefix + dotsToSlashes(pkg, false) + "/");
    }

    public @NotNull ClassFile read(final @NotNull String className) throws IOException {
        try (ZipFile zf = new ZipFile(this.file)) {
            ZipEntry ze = zf.getEntry(this.prefix + classNameToPath(className));
            if (ze == null) {
                throw new IOException("Class " + this.getPrefixAsPackage() + className + " not found in archive @ " +
                        this.file.getAbsolutePath());
            }
            return this.readStream(zf.getInputStream(ze), true);
        }
    }

    @Override
    public @NotNull @Unmodifiable List<String> list(final boolean recursive) throws IOException {
        return this.listInternal((ZipEntry ze, InputStream ignored) -> {
            String name = ze.getName();
            name = name.substring(this.prefix.length(), name.length() - 6);
            if (recursive) name = slashesToDots(name);
            return name;
        }, recursive);
    }

    @Override
    public @NotNull @Unmodifiable List<ClassFile> readAll(final boolean recursive) throws IOException {
        return this.listInternal(
                (ZipEntry ignored, InputStream stream) -> this.readStream(stream, false),
                recursive
        );
    }

    //

    protected @NotNull ClassFile readStream(@NotNull InputStream is, boolean close) throws IOException {
        try {
            ClassFileInputStream cfis = new ClassFileInputStream(new DataInputStream(is));
            return cfis.readClassFile();
        } finally {
            if (close) {
                is.close();
            }
        }
    }

    protected <T> @NotNull List<T> listInternal(
            @NotNull IOBiFunction<ZipEntry, InputStream, T> fn,
            boolean recursive
    ) throws IOException {
        try (FileInputStream fis = new FileInputStream(this.file);
             ZipInputStream zis = new ZipInputStream(fis)
        ) {
            List<T> ret = new LinkedList<>();
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (!this.shouldIncludeListEntry(ze.getName(), recursive)) continue;
                ret.add(fn.apply(ze, zis));
            }
            return Collections.unmodifiableList(ret);
        }
    }

    protected boolean shouldIncludeListEntry(@NotNull String name, boolean recursive) {
        final int prefixLen = this.prefix.length();
        if (name.length() <= prefixLen) return false;

        for (int i=0; i < prefixLen; i++) {
            if (name.charAt(i) != this.prefix.charAt(i)) return false;
        }

        final int subNameEnd = name.length() - 6; // .class
        if (subNameEnd < prefixLen) return false;

        for (int i=0; i < 6; i++) {
            if (name.charAt(subNameEnd + i) != DOT_CLASS.charAt(i)) return false;
        }

        // Exclude entries named package-info
        if ((subNameEnd - prefixLen) >= PACKAGE_INFO.length()) {
            boolean match = true;
            for (int i=0; i < PACKAGE_INFO.length(); i++) {
                if (name.charAt(subNameEnd + i) != PACKAGE_INFO.charAt(i)) {
                    match = false;
                    break;
                }
            }
            if (match) return false;
        }

        if (!recursive) {
            for (int i=prefixLen; i < subNameEnd; i++) {
                if (name.charAt(i) == '/') return false;
            }
        }

        return true;
    }

    protected @NotNull String getPrefixAsPackage() {
        return slashesToDots(this.prefix);
    }

}
