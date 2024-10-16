package io.github.wasabithumb.annolyze;

import io.github.wasabithumb.annolyze.archive.AnnolyzeArchive;
import io.github.wasabithumb.annolyze.directory.AnnolyzeDirectory;
import io.github.wasabithumb.annolyze.file.ClassFile;
import io.github.wasabithumb.annolyze.file.ClassFileInputStream;
import io.github.wasabithumb.annolyze.misc.PathUtil;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URISyntaxException;

/**
 * Entry point for the Annolyze library.
 * @since 0.1.0
 */
public final class Annolyze {

    // Using direct IO

    /**
     * Reads a class file skeleton from the provided stream.
     * @throws io.github.wasabithumb.annolyze.file.except.ClassFileReadException An exception caused by malformed
     * class file data. Includes {@link EOFException} (wrapped as
     * {@link io.github.wasabithumb.annolyze.file.except.ClassFileIncompleteDataException ClassFileIncompleteDataException})
     * when the end of the stream cuts off class file data.
     * @throws IOException A generic IO exception from the backing stream.
     */
    public static @NotNull ClassFile read(@NotNull InputStream stream) throws IOException {
        return (new ClassFileInputStream(stream)).readClassFile();
    }

    /**
     * Reads a class file skeleton from the provided stream.
     * @throws io.github.wasabithumb.annolyze.file.except.ClassFileReadException An exception caused by malformed
     * class file data. Includes {@link EOFException} (wrapped as
     * {@link io.github.wasabithumb.annolyze.file.except.ClassFileIncompleteDataException ClassFileIncompleteDataException})
     * when the end of the stream cuts off class file data.
     * @throws IOException A generic IO exception from the backing stream.
     */
    public static @NotNull ClassFile read(@NotNull DataInputStream stream) throws IOException {
        return (new ClassFileInputStream(stream)).readClassFile();
    }

    /**
     * Reads a class file skeleton from the provided file.
     * @throws io.github.wasabithumb.annolyze.file.except.ClassFileReadException An exception caused by malformed
     * class file data. Includes {@link EOFException} (wrapped as
     * {@link io.github.wasabithumb.annolyze.file.except.ClassFileIncompleteDataException ClassFileIncompleteDataException})
     * when the end of the stream cuts off class file data.
     * @throws IOException A generic IO exception from the {@link FileInputStream}.
     */
    public static @NotNull ClassFile read(@NotNull File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             DataInputStream dis = new DataInputStream(fis);
             ClassFileInputStream cfis = new ClassFileInputStream(dis)
        ) {
            return cfis.readClassFile();
        }
    }

    // Using ClassLoader

    private static @NotNull ClassFile read(
            @NotNull ClassLoader classLoader,
            @NotNull String className,
            @NotNull String classLoaderTag
    ) throws IOException {
        try (InputStream is = classLoader.getResourceAsStream(PathUtil.classNameToPath(className))) {
            if (is == null) throw new IOException("Failed to locate class \"" + className +
                    "\" using the " + classLoaderTag + " class loader.");
            try (DataInputStream dis = new DataInputStream(is);
                 ClassFileInputStream cfis = new ClassFileInputStream(dis)
            ) {
                return cfis.readClassFile();
            }
        }
    }

    /**
     * Reads a class file skeleton using the specified {@link ClassLoader} and specified class name.
     * @throws io.github.wasabithumb.annolyze.file.except.ClassFileReadException An exception caused by malformed class
     * file data.
     * @throws IOException A generic IO exception thrown by the {@link ClassLoader} resource stream.
     */
    public static @NotNull ClassFile read(@NotNull ClassLoader classLoader, @NotNull String className) throws IOException {
        return read(classLoader, className, "specified");
    }

    /**
     * Reads a class file skeleton using the {@link Thread#getContextClassLoader() context class loader} and specified
     * class name. This should be used when the class you wish to inspect is visible to the class invoking this method.
     * @throws io.github.wasabithumb.annolyze.file.except.ClassFileReadException An exception caused by malformed class
     * file data.
     * @throws IOException A generic IO exception thrown by the {@link ClassLoader} resource stream.
     * @see #read(ClassLoader, String)
     */
    public static @NotNull ClassFile read(@NotNull String className) throws IOException {
        return read(inferClassLoader(), className, "inferred");
    }

    // Using Archive

    /**
     * Provides a wrapper around the specified archive (JAR or ZIP) that assists with inspecting the class files that
     * it contains.
     * @throws IllegalArgumentException Provided file is not an archive.
     */
    public static @NotNull AnnolyzeArchive archive(@NotNull File file) throws IllegalArgumentException {
        return AnnolyzeArchive.of(file);
    }

    /**
     * Provides the {@link #archive(File)} instance for the <i>code source of the class invoking this method</i>.
     * This is hacky, but provides a very good implementation of logic you might otherwise seek to implement.
     * @throws IllegalStateException The code source may not be retrieved or is not an archive (JAR or ZIP); may be a {@link #directory() directory}.
     * @throws AssertionError The code source is not a URI; should not throw.
     */
    public static @NotNull AnnolyzeArchive archive() throws IllegalStateException {
        final File file = getCallerSource();
        try {
            return archive(file);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Code source is invalid", e);
        }
    }

    // Using Directory

    /**
     * Provides a wrapper around the specified directory that assists with inspecting the class files that
     * it contains.
     * @throws IllegalArgumentException Provided file is not a directory.
     */
    public static @NotNull AnnolyzeDirectory directory(@NotNull File dir) throws IllegalArgumentException {
        return AnnolyzeDirectory.of(dir);
    }

    /**
     * Provides the {@link #directory(File)} instance for the <i>code source of the class invoking this method</i>.
     * This is hacky, but provides a very good implementation of logic you might otherwise seek to implement.
     * @throws IllegalStateException The code source may not be retrieved or is not a directory; may be an {@link #archive() archive}.
     * @throws AssertionError The code source is not a URI; should not throw.
     */
    public static @NotNull AnnolyzeDirectory directory() throws IllegalStateException {
        final File file = getCallerSource();
        try {
            return directory(file);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Code source is invalid", e);
        }
    }

    // Utilities

    private static @NotNull File getCallerSource() throws IllegalStateException {
        return getClassSource(getCallerClass());
    }

    private static @NotNull File getClassSource(@NotNull Class<?> cls) throws IllegalStateException {
        File file;
        try {
            file = new File(cls.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new AssertionError("Code source is not a valid URI");
        } catch (SecurityException e) {
            throw new IllegalStateException("A security rule has denied access to the code source location", e);
        }
        return file;
    }

    private static @NotNull Class<?> getCallerClass() throws IllegalStateException {
        final String ourClassName = Annolyze.class.getName();
        final String threadClassName = Thread.class.getName();
        final Thread currentThread = Thread.currentThread();

        final ClassLoader contextClassLoader = currentThread.getContextClassLoader();
        final ClassLoader[] knownClassLoaders = (contextClassLoader == null) ? new ClassLoader[] {
                ClassLoader.getSystemClassLoader(),
                Annolyze.class.getClassLoader()
        } : new ClassLoader[] {
                ClassLoader.getSystemClassLoader(),
                contextClassLoader,
                Annolyze.class.getClassLoader()
        };
        int chosenClassLoader = knownClassLoaders.length - 1;

        StackTraceElement[] elements = currentThread.getStackTrace();
        StackTraceElement element;
        String name = null;
        String tmp;
        for (int i=1; i < elements.length; i++) {
            element = elements[i];
            tmp = element.getClassName();
            if (tmp.equals(ourClassName)) continue;
            if (tmp.startsWith(threadClassName)) continue;
            name = tmp;
            tmp = element.getClassLoaderName();
            for (int z=0; z < knownClassLoaders.length; z++) {
                if (tmp == null) {
                    if (knownClassLoaders[z].getName() == null && z != (knownClassLoaders.length - 1)) {
                        chosenClassLoader = z;
                    }
                } else if (tmp.equals(knownClassLoaders[z].getName())) {
                    chosenClassLoader = z;
                    break;
                }
            }
            break;
        }
        if (name == null) throw new IllegalStateException("Cannot invoke this method anonymously");

        Class<?> ret;
        try {
            ret = Class.forName(name, false, knownClassLoaders[chosenClassLoader]);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Calling class cannot be found with inferred class loader", e);
        }
        return ret;
    }

    private static @NotNull ClassLoader inferClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = ClassLoader.getSystemClassLoader();
        return cl;
    }

}
