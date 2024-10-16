package io.github.wasabithumb.annolyze;

import io.github.wasabithumb.annolyze.archive.AnnolyzeArchive;
import io.github.wasabithumb.annolyze.cp.ConstantPool;
import io.github.wasabithumb.annolyze.directory.AnnolyzeDirectory;
import io.github.wasabithumb.annolyze.file.ClassFile;
import io.github.wasabithumb.annolyze.reference.member.field.FieldReference;
import io.github.wasabithumb.annolyze.reference.type.TypeReference;
import io.github.wasabithumb.annolyze.reference.type.boxed.ClassReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingConsumer;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnnolyzeTest {

    // Ensure that AnnolyzeTest can read itself using the context class loader, and verify that the method
    // read() exists and has the @Test annotation.
    @Test()
    void read() {
        assertDoesNotThrow(this::read0);
    }

    private void read0() throws Exception {
        final ClassFile cf = Annolyze.read(this.getClass().getName());
        assertEquals("io.github.wasabithumb.annolyze.AnnolyzeTest", cf.name());
        assertEquals("AnnolyzeTest", cf.simpleName());

        final String str = cf.toString(true);
        System.out.println(str);
        final String expectedSequence1 = """
                \t@org.junit.jupiter.api.Test
                \tvoid read() {}

                \tprivate void read0() {}""";
        assertTrue(str.contains(expectedSequence1));

        final List<ClassReference> annotations = cf.getMethodAnnotations("read", new TypeReference[0]);
        assertNotNull(annotations);
        assertEquals(1, annotations.size());
        assertEquals(ClassReference.of(Test.class), annotations.get(0));
    }

    // Ensure that Annolyze can read the "dummy" classes using the directory loader
    @Test()
    void directory() {
        final AnnolyzeDirectory dir = Annolyze.directory().sub("io.github.wasabithumb.annolyze.dummy");

        directory(dir, "DummyA", (ClassFile cf) -> {
            assertNotNull(cf.getField("DUMMY_FIELD_ONE"));
            assertNotNull(cf.getField("DUMMY_FIELD_TWO"));
            assertNotNull(cf.getField("DUMMY_FIELD_THREE"));
            assertNotNull(cf.getField("DUMMY_FIELD_FOUR"));
        });

        assertDoesNotThrow(() -> {
            assertNotEquals(0, dir.list(false).size());
            assertNotEquals(0, dir.list(true).size());
        });
    }

    private void directory(@NotNull AnnolyzeDirectory dir, @NotNull String name, @Nullable ThrowingConsumer<ClassFile> checks) {
        final ClassFile file = assertDoesNotThrow(() -> dir.read(name));
        if (checks != null) assertDoesNotThrow(() -> checks.accept(file));
    }

    // Ensure that Annolyze can read classes using the archive loader
    // We use the "internals" JAR as :internals:jar is guaranteed to run before :test, convenient!
    @Test()
    void archive() {
        final File internalsJAR = assertDoesNotThrow(() ->
                new File(ConstantPool.class.getProtectionDomain().getCodeSource().getLocation().toURI()));

        assertTrue(internalsJAR.isFile());
        assertTrue(internalsJAR.getName().endsWith(".jar"));

        assertDoesNotThrow(() -> {
            final AnnolyzeArchive archive = Annolyze.archive(internalsJAR);

            final ClassFile a = archive.read("io.github.wasabithumb.annolyze.cp.ConstantPool");
            final String as = a.toString(true);

            FieldReference field = a.getField("data");
            assertNotNull(field);
            assertTrue(field.flags().isFinal());

            final ClassFile b = archive.sub("io.github.wasabithumb.annolyze.cp").read("ConstantPool");
            final String bs = b.toString(true);

            field = b.getField("data");
            assertNotNull(field);
            assertTrue(field.flags().isFinal());

            assertEquals(as, bs);

            assertEquals(0, archive.list(false).size());
            assertTrue(archive.list().size() > 2);
        });
    }

}