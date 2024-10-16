package io.github.wasabithumb.annolyze.file;

import io.github.wasabithumb.annolyze.cp.ConstantPool;
import io.github.wasabithumb.annolyze.cp.ConstantPoolException;
import io.github.wasabithumb.annolyze.cp.ConstantPoolString;
import io.github.wasabithumb.annolyze.file.except.*;
import io.github.wasabithumb.annolyze.reference.member.field.FieldReference;
import io.github.wasabithumb.annolyze.reference.member.method.MethodReference;
import io.github.wasabithumb.annolyze.reference.type.boxed.ClassReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A {@link FilterInputStream} that reads the backing stream as a {@link ClassFile}.
 */
public class ClassFileInputStream extends FilterInputStream {

    private static final int[] MAGIC = new int[] { 0xCA, 0xFE, 0xBA, 0xBE };
    private static final String ATTR_ANNOTATIONS = "RuntimeVisibleAnnotations";

    public ClassFileInputStream(@NotNull InputStream in) {
        super((in instanceof DataInputStream) ? in : new DataInputStream(in));
    }

    public ClassFileInputStream(@NotNull DataInputStream in) {
        super(in);
    }

    /**
     * Reads the content of this stream as a class file.
     * @throws ClassFileReadException The content of the stream is unprocessable as class file data
     * @throws IOException Generic IO exception from backing stream
     */
    public @NotNull ClassFile readClassFile() throws IOException {
        try {
            return this.readClassFile0();
        } catch (ConstantPoolException ex) {
            throw new ClassFileInvalidDataException("Illegal constant pool", ex);
        }
    }

    private @NotNull ClassFile readClassFile0() throws IOException {
        this.readMagic();
        this.skipU2(); // minor_version
        this.readMajorVersion();

        final ConstantPool constantPool = this.readConstantPoolAndAccessFlags();
        final ClassFile.Builder builder = ClassFile.builder();

        final int classRefIndex = this.readU2();
        final ClassReference classRef = this.classReferenceFromConstantPool(constantPool, classRefIndex, true);
        builder.setClass(classRef);

        this.skipU2(); // super_class
        this.skipN(2L * this.readU2()); // interfaces_count, interfaces

        int count;
        count = this.readU2();
        for (int i=0; i < count; i++) this.readField(builder, classRef, constantPool);
        count = this.readU2();
        for (int i=0; i < count; i++) this.readMethod(builder, classRef, constantPool);
        this.readAttributesForAnnotations(constantPool, builder::addAnnotation);

        return builder.build();
    }

    //

    private void readField(
            @NotNull ClassFile.Builder builder,
            @NotNull ClassReference classRef,
            @NotNull ConstantPool constantPool
    ) throws IOException {
        final int accessFlags = this.readU2();
        final String name = constantPool.get(this.readU2());
        final String descriptor = constantPool.get(this.readU2());

        final FieldReference field = FieldReference.of(classRef, name, descriptor, accessFlags);
        builder.addMember(field);

        this.readAttributesForAnnotations(
                constantPool,
                (ClassReference annotation) -> builder.addAnnotation(field, annotation)
        );
    }

    private void readMethod(
            @NotNull ClassFile.Builder builder,
            @NotNull ClassReference classRef,
            @NotNull ConstantPool constantPool
    ) throws IOException {
        final int accessFlags = this.readU2();
        final String name = constantPool.get(this.readU2());
        final String descriptor = constantPool.get(this.readU2());

        if (name.equals("<clinit>")) {
            // Manually exclude <clinit>
            this.readAttributesForAnnotations(constantPool, (ClassReference ignored) -> { });
            return;
        }

        final MethodReference method = MethodReference.of(classRef, name, descriptor, accessFlags);
        builder.addMember(method);

        this.readAttributesForAnnotations(
                constantPool,
                (ClassReference annotation) -> builder.addAnnotation(method, annotation)
        );
    }

    private void readAttributesForAnnotations(
            @NotNull ConstantPool constantPool,
            @NotNull Consumer<ClassReference> withAnnotation
    ) throws IOException {
        final int count = this.readU2();
        for (int i=0; i < count; i++)
            this.readAttributeForAnnotations(constantPool, withAnnotation);
    }

    private void readAttributeForAnnotations(
            @NotNull ConstantPool constantPool,
            @NotNull Consumer<ClassReference> withAnnotation
    ) throws IOException {
        final int nameIndex = this.readU2();
        if (!Objects.equals(ATTR_ANNOTATIONS, constantPool.get(nameIndex))) {
            this.skipN(this.readU4());
            return;
        }
        this.skipU4();

        final int numAnnotations = this.readU2();
        for (int i=0; i < numAnnotations; i++) {
            withAnnotation.accept(this.classReferenceFromConstantPool(constantPool, this.readU2(), false));
            this.skipAttributeElementValuePairs(this.readU2());
        }
    }

    private void skipAttributeElementValuePairs(int numPairs) throws IOException {
        for (int i=0; i < numPairs; i++) {
            this.skipU2(); // element_name_index
            this.skipAttributeElementValue();
        }
    }

    private void skipAttributeElementValue() throws IOException {
        // https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html#jvms-4.7.16.1
        switch (this.readU1()) { // tag
            case 'e': // Enum class
                this.skipU2();
            case 'B': // byte
            case 'C': // char
            case 'D': // double
            case 'F': // float
            case 'I': // int
            case 'J': // long
            case 'S': // short
            case 'Z': // boolean
            case 's': // String
            case 'c': // Class
                this.skipU2();
                break;
            case '@': // Annotation interface
                this.skipU2();
                this.skipAttributeElementValuePairs(this.readU2());
                break;
            case '[':
                final int count = this.readU2();
                for (int i=0; i < count; i++) this.skipAttributeElementValue();
                break;
        }
    }

    private @NotNull ConstantPool readConstantPoolAndAccessFlags() throws IOException {
        int constantPoolCount = this.readU2();
        if (constantPoolCount == 0) throw new ClassFileInvalidDataException("Constant pool size is 0");
        final ConstantPool constantPool = new ConstantPool(constantPoolCount);
        int tag = -1;
        for (int i=1; i < constantPoolCount; i++) {
            // This is a weird hack; it seems like the constant pool is some combination length encoded
            // and null-terminated. This isn't documented.
            tag = this.readU1();
            if (tag == 0) break;
            constantPool.set(i, this.readConstantPoolInfo(tag));
        }
        if (tag == 0) {
            this.skipU1(); // access_flags
        } else {
            this.skipU2(); // access_flags
        }
        return constantPool;
    }

    private void readMagic() throws IOException {
        int read;
        for (int i=0; i < MAGIC.length; i++) {
            read = this.read();
            if (read == -1) {
                throw new ClassFileMalformedHeaderException(
                        "Malformed header in class file",
                        this.genericEOF()
                );
            }
            if (read != MAGIC[i]) {
                throw new ClassFileMalformedHeaderException(
                        "Expected " + ((byte) MAGIC[i]) + " at position " + i + ", got " + ((byte) read)
                );
            }
        }
    }

    private void readMajorVersion() throws IOException {
        final int major = this.readU2();
        final int max = ClassFile.getMaxMajorVersion();
        if (major > max) {
            throw new ClassFileUnsupportedMajorVersionException(
                    "Unsupported class file major version " + major + " (expected at most " + max + ")"
            );
        }
    }

    private int readU1() throws IOException {
        int ret = this.read();
        if (ret == -1) throw new ClassFileIncompleteDataException("Failed to read U1 field", this.genericEOF());
        return ret;
    }

    private int readU2() throws IOException {
        int hi, lo;

        hi = this.read();
        if (hi == -1) throw new ClassFileIncompleteDataException("Failed to read U2 field (high byte)", this.genericEOF());

        lo = this.read();
        if (lo == -1) throw new ClassFileIncompleteDataException("Failed to read U2 field (low byte)", this.genericEOF());

        return (hi << 8) | lo;
    }

    private long readU4() throws IOException {
        int read;
        try {
            read = ((DataInputStream) this.in).readInt();
        } catch (EOFException e) {
            throw new ClassFileIncompleteDataException("Failed to read U4 field", e);
        }
        return Integer.toUnsignedLong(read);
    }

    private void skipU1() throws IOException {
        if (this.skip(1L) != 1L)
            throw new ClassFileIncompleteDataException("Failed to skip U1 field", this.genericEOF());
    }

    private void skipU2() throws IOException {
        if (this.skip(2L) != 2L)
            throw new ClassFileIncompleteDataException("Failed to skip U2 field", this.genericEOF());
    }

    private void skipU4() throws IOException {
        if (this.skip(4L) != 4L)
            throw new ClassFileIncompleteDataException("Failed to skip U4 field", this.genericEOF());
    }

    private void skipN(long count) throws IOException {
        if (this.skip(count) != count)
            throw new ClassFileIncompleteDataException("Failed to skip block of length " + count, this.genericEOF());
    }

    /**
     * Reads the entirety of a {@code cp_info} struct, returning a string if the struct holds UTF-8. Otherwise,
     * we don't care about it (return null).
     */
    private @Nullable ConstantPoolString readConstantPoolInfo(final int tag) throws IOException {
        switch (tag) {
            case 1:  // CONSTANT_Utf8
                return ConstantPoolString.of(this.readModifiedUTF8());
            case 5:  // CONSTANT_Long
            case 6:  // CONSTANT_Double
                this.skipU4();
            case 3:  // CONSTANT_Integer
            case 4:  // CONSTANT_Float
                this.skipU4();
                break;
            case 7:  // CONSTANT_Class
            case 8:  // CONSTANT_String
            case 16: // CONSTANT_MethodType
            case 19: // CONSTANT_Module
            case 20: // CONSTANT_Package
                return ConstantPoolString.of(this.readU2());
            case 9:  // CONSTANT_Fieldref
            case 10: // CONSTANT_Methodref
            case 11: // CONSTANT_InterfaceMethodref
            case 12: // CONSTANT_NameAndType
                return ConstantPoolString.of(this.readU2(), this.readU2());
            case 15: // CONSTANT_MethodHandle
                this.skipU1();
                this.skipU2();
                break;
            case 17: // CONSTANT_Dynamic
            case 18: // CONSTANT_InvokeDynamic
                this.skipU2();
                this.skipU2();
                break;
            default:
                throw new ClassFileInvalidDataException("Unrecognized constant pool info tag: " + tag);
        }
        return null;
    }

    private @NotNull ClassReference classReferenceFromConstantPool(
            @NotNull ConstantPool pool,
            int index,
            boolean addPrefixSuffix
    ) throws IOException {
        String data = pool.get(index, 1);
        if (addPrefixSuffix) data = "L" + data + ";";
        try {
            return ClassReference.of(data);
        } catch (IllegalArgumentException e) {
            throw new ClassFileInvalidDataException("Invalid class reference \"" + data + "\" in constant pool", e);
        }
    }

    private @NotNull String readModifiedUTF8() throws IOException {
        try {
            return ((DataInputStream) this.in).readUTF();
        } catch (EOFException e) {
            throw new ClassFileIncompleteDataException("Incomplete string", e);
        } catch (UTFDataFormatException e) {
            throw new ClassFileInvalidDataException("Invalid string", e);
        }
    }

    private @NotNull EOFException genericEOF() {
        return new EOFException("Unexpected end of stream");
    }

}
