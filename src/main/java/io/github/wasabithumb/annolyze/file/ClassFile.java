package io.github.wasabithumb.annolyze.file;

import io.github.wasabithumb.annolyze.reference.member.field.FieldReference;
import io.github.wasabithumb.annolyze.reference.member.MemberReference;
import io.github.wasabithumb.annolyze.reference.member.method.MethodReference;
import io.github.wasabithumb.annolyze.reference.type.TypeReference;
import io.github.wasabithumb.annolyze.reference.type.boxed.ClassReference;
import io.github.wasabithumb.annolyze.misc.UnmodifiableSelectSet;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Represents the parsed content of a {@code .class} file (that which is relevant for this library).
 * This includes the root class reference, root class annotations, member references and member annotations.
 * Note that inner classes are always separate {@code .class} files; this library makes no attempt to distinguish
 * this case.
 */
public final class ClassFile {

    private static final int MAX_MAJOR_VERSION;
    static {
        int maxMajorVersion = 65; // Java 21
        final String sysProp = System.getProperty("java.version");
        if (sysProp != null) {
            int sysVersion = 0;
            char c;
            // Not accurate for Java < 9 which would set sysVersion to 1. Doesn't matter, of course.
            for (int i=0; i < sysProp.length(); i++) {
                c = sysProp.charAt(i);
                if (c < '0' || c > '9') break;
                sysVersion = (sysVersion * 10) + (c - '0');
            }
            maxMajorVersion = Math.max(maxMajorVersion, sysVersion + 44);
        }
        MAX_MAJOR_VERSION = maxMajorVersion;
    }

    /**
     * Returns the maximum <a href="https://javaalmanac.io/bytecode/versions/">class file major version</a> for this
     * library. The default is 65 (corresponding to Java 21), however if the Java version of the current runtime
     * is greater than 21, this returns {@code VERSION + 44} (the class file major version for that release).
     * This means that attempting to read a class file that the JVM understands should never throw while also
     * conveying the fact that this library was made to read up to Java 21 class files. As a result, this
     * library <i>breaks forward compatibility</i> in that its usage is technically unsafe in Java 22+.
     * The intent is to keep the library updated to support the most recent LTS version of Java.
     */
    public static int getMaxMajorVersion() {
        return MAX_MAJOR_VERSION;
    }

    @Contract("-> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    //

    private final ClassReference reference;
    private final List<ClassReference> annotations;
    private final Map<MemberReference<?>, List<ClassReference>> members;

    @ApiStatus.Internal
    public ClassFile(
            @NotNull ClassReference reference,
            @NotNull List<ClassReference> annotations,
            @NotNull Map<MemberReference<?>, List<ClassReference>> members
    ) {
        this.reference = reference;
        this.annotations = Collections.unmodifiableList(annotations);
        this.members = Collections.unmodifiableMap(members);
    }

    /**
     * Returns a {@link ClassReference} which points to the class declared in this class file.
     */
    public @NotNull ClassReference reference() {
        return this.reference;
    }

    /**
     * Alias for {@code reference().name()}
     * @see ClassReference#name()
     */
    public @NotNull String name() {
        return this.reference.name();
    }

    /**
     * Alias for {@code reference().simpleName()}
     * @see ClassReference#simpleName()
     */
    public @NotNull String simpleName() {
        return this.reference.simpleName();
    }

    /**
     * Returns the annotations declared on the class in this class file. Does not include the annotations
     * declared on class members.
     */
    public @NotNull @Unmodifiable List<ClassReference> getAnnotations() {
        return this.annotations;
    }

    /**
     * Returns the annotations declared on a member of the class in this class file.
     */
    public @NotNull @Unmodifiable List<ClassReference> getAnnotations(@NotNull MemberReference<?> member) {
        List<ClassReference> ret = this.members.get(member);
        if (ret == null) return Collections.emptyList();
        return Collections.unmodifiableList(ret);
    }

    /**
     * Returns the members declared on the class in this class file.
     * @see #getFields()
     * @see #getMethods()
     */
    public @NotNull @Unmodifiable Set<MemberReference<?>> getMembers() {
        return this.members.keySet();
    }

    /**
     * Returns the field reference specified in this class file that shares the provided name, or null
     * if no match.
     */
    public @Nullable FieldReference getField(@NotNull String name) {
        for (FieldReference fr : this.getFields()) {
            if (fr.name().equals(name)) return fr;
        }
        return null;
    }

    /**
     * Identical to {@code getAnnotations(getField(...))}, with null propagation.
     */
    public @Nullable @Unmodifiable List<ClassReference> getFieldAnnotations(@NotNull String name) {
        FieldReference field = this.getField(name);
        if (field == null) return null;
        return this.getAnnotations(field);
    }

    /**
     * Returns the fields declared on the class in this class file.
     * This is a subset of {@link #getMembers()}.
     */
    public @NotNull @Unmodifiable Set<FieldReference> getFields() {
        return new UnmodifiableSelectSet<>(this.getMembers(), FieldReference.class);
    }

    /**
     * Returns the public fields declared on the class in this class file.
     * @see #getFields()
     */
    public @NotNull @Unmodifiable Set<FieldReference> getPublicFields() {
        return new UnmodifiableSelectSet<>(
                this.getMembers(),
                FieldReference.class,
                (FieldReference ref) -> ref.flags().isPublic()
        );
    }

    /**
     * Returns the method reference specified in this class file that shares the provided signature, or null
     * if no match.
     */
    public @Nullable MethodReference getMethod(@NotNull String name, @NotNull TypeReference... params) {
        final MethodReference dummy = MethodReference.dummy(name, params);
        for (MethodReference mr : this.getMethods()) {
            if (mr.equals(dummy)) return mr;
        }
        return null;
    }

    /**
     * Identical to {@code getAnnotations(getMethod(...))}, with null propagation.
     */
    public @Nullable @Unmodifiable List<ClassReference> getMethodAnnotations(@NotNull String name, @NotNull TypeReference... params) {
        MethodReference method = this.getMethod(name, params);
        if (method == null) return null;
        return this.getAnnotations(method);
    }

    /**
     * Returns the method reference specified in this class file that shares the provided signature, or null
     * if no match.
     */
    public @Nullable MethodReference getMethod(@NotNull String name, @NotNull Class<?> @NotNull ... params) {
        final int paramCount = params.length;
        final TypeReference[] paramRefs = new TypeReference[paramCount];
        for (int i=0; i < paramCount; i++) paramRefs[i] = TypeReference.of(params[i]);
        return this.getMethod(name, paramRefs);
    }

    /**
     * Identical to {@code getAnnotations(getMethod(...))}, with null propagation.
     */
    public @Nullable @Unmodifiable List<ClassReference> getMethodAnnotations(@NotNull String name, @NotNull Class<?>... params) {
        MethodReference method = this.getMethod(name, params);
        if (method == null) return null;
        return this.getAnnotations(method);
    }

    /**
     * Returns the methods declared on this class in the class file.
     * This is a subset of {@link #getMembers()}.
     */
    public @NotNull @Unmodifiable Set<MethodReference> getMethods() {
        return new UnmodifiableSelectSet<>(this.getMembers(), MethodReference.class);
    }

    /**
     * Returns the public methods declared on the class in this class file.
     * @see #getMembers()
     */
    public @NotNull @Unmodifiable Set<MethodReference> getPublicMethods() {
        return new UnmodifiableSelectSet<>(
                this.getMembers(),
                MethodReference.class,
                (MethodReference ref) -> ref.flags().isPublic()
        );
    }

    /**
     * Formats this ClassFile in a way that approximates the structure of a {@code .java} source file.
     * Consider this a rudimentary decompilation.
     * @param stable If set to true, extra work will be done to make sure that this always returns the same value
     *               for equivalent instances.
     */
    public @NotNull String toString(boolean stable) {
        StringBuilder ret = new StringBuilder();

        final String name = this.name();
        final int wherePkg = name.lastIndexOf('.');
        if (wherePkg != -1) {
            ret.append("package ")
                    .append(name, 0, wherePkg)
                    .append(";\n\n");
        }

        List<ClassReference> ownAnnotations = this.getAnnotations();
        if (stable) {
            ownAnnotations = new ArrayList<>(ownAnnotations);
            Collections.sort(ownAnnotations);
        }
        for (ClassReference ref : ownAnnotations) {
            ret.append('@').append(ref.name()).append('\n');
        }

        ret.append("class ");
        if (wherePkg == -1) {
            ret.append(name);
        } else {
            ret.append(name, wherePkg + 1, name.length());
        }
        ret.append(" {\n\n");

        Collection<FieldReference> fields = this.getFields();
        Collection<MethodReference> methods = this.getMethods();
        if (stable) {
            fields = new ArrayList<>(fields);
            Collections.sort((List<FieldReference>) fields);
            methods = new ArrayList<>(methods);
            Collections.sort((List<MethodReference>) methods);
        }

        for (FieldReference field : fields) {
            List<ClassReference> memberAnnotations = this.getAnnotations(field);
            if (stable) {
                memberAnnotations = new ArrayList<>(memberAnnotations);
                Collections.sort(memberAnnotations);
            }
            for (ClassReference ref : memberAnnotations) {
                ret.append("\t@").append(ref.name()).append('\n');
            }

            ret.append('\t')
                    .append(field.flags().keywords())
                    .append(' ')
                    .append(field.type().name())
                    .append(' ')
                    .append(field.name())
                    .append(";\n\n");
        }

        if (!fields.isEmpty() && !methods.isEmpty()) ret.append("\n");

        for (MethodReference method : methods) {
            List<ClassReference> memberAnnotations = this.getAnnotations(method);
            if (stable) {
                memberAnnotations = new ArrayList<>(memberAnnotations);
                Collections.sort(memberAnnotations);
            }
            for (ClassReference ref : memberAnnotations) {
                ret.append("\t@").append(ref.name()).append('\n');
            }

            ret.append('\t');

            String keywords = method.flags().keywords();
            if (!keywords.isEmpty()) ret.append(keywords).append(' ');

            if (method.name().equals("<init>")) {
                ret.append(this.simpleName());
            } else {
                ret.append(method.returnType().name())
                        .append(' ')
                        .append(method.name());
            }

            ret.append('(');
            TypeReference[] params = method.parameterTypes();
            for (int i=0; i < params.length; i++) {
                if (i != 0) ret.append(", ");
                ret.append(params[i].name());
            }
            ret.append(") {}\n\n");
        }

        ret.append("}");
        return ret.toString();
    }

    /**
     * Alias for {@code toString(false)}
     * @see #toString(boolean)
     */
    public @NotNull String toString() {
        return this.toString(false);
    }

    //

    public static final class Builder {

        private boolean open = true;
        private ClassReference reference = null;
        private final List<ClassReference> annotations = new ArrayList<>();
        private final Map<MemberReference<?>, List<ClassReference>> annotatedMembers = new HashMap<>();

        @ApiStatus.Internal
        public Builder() { }

        private void assertOpen() throws IllegalStateException {
            // Guard against collection mutation after passing to the ClassFile, which expects immutable.
            if (!this.open) throw new IllegalStateException("Cannot use ClassFile.Builder after #build()");
        }

        @Contract("_ -> this")
        public @NotNull Builder setClass(@NotNull ClassReference reference) {
            this.assertOpen();
            this.reference = reference;
            return this;
        }

        @Contract("_ -> this")
        public @NotNull Builder addAnnotation(@NotNull ClassReference annotation) {
            this.assertOpen();
            this.annotations.add(annotation);
            return this;
        }

        private @NotNull List<ClassReference> getMemberAnnotations(@NotNull MemberReference<?> member) {
            this.assertOpen();
            return this.annotatedMembers.computeIfAbsent(
                    member,
                    (MemberReference<?> k) -> new ArrayList<>()
            );
        }

        @Contract("_ -> this")
        public @NotNull Builder addMember(@NotNull MemberReference<?> member) {
            this.getMemberAnnotations(member).clear();
            return this;
        }

        @Contract("_, _ -> this")
        public @NotNull Builder addAnnotation(@NotNull MemberReference<?> member, @NotNull ClassReference annotation) {
            this.getMemberAnnotations(member).add(annotation);
            return this;
        }

        @Contract("-> new")
        public @NotNull ClassFile build() {
            this.assertOpen();
            if (this.reference == null) throw new IllegalStateException("Cannot call build() before setClass()");
            this.open = false;
            return new ClassFile(
                    this.reference,
                    this.annotations,
                    this.annotatedMembers
            );
        }

    }

}
