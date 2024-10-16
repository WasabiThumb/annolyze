package io.github.wasabithumb.annolyze.directory;

import io.github.wasabithumb.annolyze.Annolyze;
import io.github.wasabithumb.annolyze.file.ClassFile;
import io.github.wasabithumb.annolyze.misc.IOBiFunction;
import io.github.wasabithumb.annolyze.misc.PathUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApiStatus.Internal
class AnnolyzeDirectoryImpl implements AnnolyzeDirectory {

    protected final File dir;

    @ApiStatus.Internal
    AnnolyzeDirectoryImpl(@NotNull File dir) throws IllegalArgumentException {
        if (!dir.isDirectory()) throw new IllegalArgumentException("Path \"" + dir.getAbsolutePath() +
                "\" is not a directory");
        this.dir = dir;
    }

    private @NotNull File navigate(@NotNull String dotPath, boolean addSuffix) {
        final int pkgLen = dotPath.length();
        File file = this.dir;
        int start = 0;
        int end = 0;

        boolean done;
        do {
            done = (end == pkgLen);
            if (done || dotPath.charAt(end) == '.') {
                String chunk = dotPath.substring(start, end);
                if (done && addSuffix) chunk += PathUtil.DOT_CLASS;
                file = new File(file, chunk);
                start = ++end;
            } else {
                end++;
            }
        } while (!done);

        return file;
    }

    @Override
    public @NotNull AnnolyzeDirectory sub(@NotNull String pkg) {
        return new AnnolyzeDirectoryImpl(this.navigate(pkg, false));
    }

    @Override
    public @NotNull ClassFile read(@NotNull String className) throws IOException {
        final File file = this.navigate(className, true);
        return Annolyze.read(file);
    }

    @Override
    public @NotNull @Unmodifiable List<String> list(boolean recursive) throws IOException {
        return Collections.unmodifiableList(this.list0(
                recursive,
                (String path, File ignored) -> path,
                true
        ));
    }

    @Override
    public @NotNull @Unmodifiable List<ClassFile> readAll(boolean recursive) throws IOException {
        return Collections.unmodifiableList(this.list0(
                recursive,
                (String ignored, File file) -> Annolyze.read(file),
                false
        ));
    }

    //

    private <T> @NotNull List<T> list0(
            boolean recursive,
            @NotNull IOBiFunction<String, File, T> extract,
            boolean requiresPath
    ) throws IOException {
        return this.list00("", this.dir, recursive, extract, requiresPath);
    }

    private <T> @NotNull List<T> list00(
            @NotNull String prefix,
            @NotNull File root,
            boolean recursive,
            @NotNull IOBiFunction<String, File, T> extract,
            boolean requiresPath
    ) throws IOException {
        final File[] ents = root.listFiles();
        if (ents == null) return Collections.emptyList();
        final List<T> list = new ArrayList<>(ents.length);
        this.list000(list, prefix, ents, recursive, extract, requiresPath);
        return list;
    }

    private <T> void list000(
            @NotNull List<T> list,
            @NotNull String prefix,
            @NotNull File @NotNull [] ents,
            boolean recursive,
            @NotNull IOBiFunction<String, File, T> extract,
            boolean requiresPath
    ) throws IOException {
        for (File ent : ents) {
            if (ent.isDirectory()) {
                if (recursive) {
                    list.addAll(this.list00(
                            requiresPath ? prefix + ent.getName() + "." : prefix,
                            ent,
                            true,
                            extract,
                            requiresPath
                    ));
                }
                continue;
            } else if (!ent.isFile()) {
                continue;
            }

            String name = ent.getName();
            if (name.length() < 7 || !name.endsWith(PathUtil.DOT_CLASS)) continue;
            if (name.length() == 18 && name.startsWith("package-info")) continue;

            list.add(extract.apply(
                    requiresPath ? prefix + name.substring(0, name.length() - 6) : prefix,
                    ent
            ));
        }
    }

}
