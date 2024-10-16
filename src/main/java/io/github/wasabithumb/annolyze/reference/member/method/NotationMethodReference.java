package io.github.wasabithumb.annolyze.reference.member.method;

import io.github.wasabithumb.annolyze.reference.type.ArrayTypeReference;
import io.github.wasabithumb.annolyze.reference.type.TypeReference;
import io.github.wasabithumb.annolyze.reference.type.boxed.ClassReference;
import io.github.wasabithumb.annolyze.reference.type.primitive.PrimitiveReference;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.CharBuffer;
import java.util.Objects;
import java.util.function.Consumer;

@ApiStatus.Internal
final class NotationMethodReference extends AbstractMethodReference {

    private static final String ERR_DESCRIPTOR = "Invalid method descriptor";

    private static int readDescriptor(
            @NotNull CharSequence descriptor,
            @Nullable Consumer<TypeReference> useParameterType,
            @Nullable Consumer<TypeReference> useReturnType
    ) throws IllegalArgumentException {
        final boolean doUseParameterType = (useParameterType != null);

        final int len = descriptor.length();
        if (len < 3) throwInvalidDescriptor(descriptor, "length < 3");
        if (descriptor.charAt(0) != '(') throwInvalidDescriptor(descriptor, "[0] != '('");

        int i = 1;
        int parameterCount = 0;
        int arrayDepth = 0;
        char c;
        TypeReference tmp;
        for (; i < len; i++) {
            c = descriptor.charAt(i);
            if (c == ')') {
                if (arrayDepth != 0)
                    throwInvalidDescriptor(descriptor, "array char [ does not precede another descriptor");
                break;
            }
            if (c == '[') {
                arrayDepth++;
                continue;
            }
            if (c == 'L') {
                int start = i;
                int whereTerminator = -1;
                while ((++i) < len) {
                    if (descriptor.charAt(i) == ';') {
                        whereTerminator = i;
                        break;
                    }
                }
                if (whereTerminator == -1)
                    throwInvalidDescriptor(descriptor, "char L is not closed by char ;");
                tmp = ClassReference.of(descriptor.subSequence(start, whereTerminator + 1));
            } else if ((tmp = PrimitiveReference.getByChar(c)) == null) {
                throwInvalidDescriptor(descriptor, "illegal char: " + c);
            }
            if (doUseParameterType) {
                while (arrayDepth > 0) {
                    tmp = new ArrayTypeReference(tmp);
                    arrayDepth--;
                }
                useParameterType.accept(tmp);
            } else {
                arrayDepth = 0;
            }
            parameterCount++;
        }
        if (i == len) throwInvalidDescriptor(descriptor, "missing end parenthesis");

        if (useReturnType != null) {
            TypeReference returnType;
            try {
                returnType = TypeReference.of(descriptor.subSequence(i + 1, descriptor.length()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(ERR_DESCRIPTOR + " \"" + descriptor + "\" (invalid return type)", e);
            }
            useReturnType.accept(returnType);
        }

        return parameterCount;
    }

    @Contract("_, _ -> fail")
    private static void throwInvalidDescriptor(
            @NotNull CharSequence descriptor,
            @NotNull String detail
    ) throws IllegalArgumentException {
        throw new IllegalArgumentException(ERR_DESCRIPTOR + " \"" + descriptor + "\" (" + detail + ")");
    }

    //

    private final String descriptor;
    private final int parameterCount;
    private final TypeReference returnType;
    private final transient int accessFlags;
    private final int argEnd;
    public NotationMethodReference(
            @NotNull ClassReference declaringClass,
            @NotNull String name,
            @NotNull String descriptor,
            int accessFlags
    ) {
        super(declaringClass, name);
        this.descriptor = descriptor;

        TypeReference[] returnType = new TypeReference[1];
        this.parameterCount = readDescriptor(
                CharBuffer.wrap(descriptor),
                null,
                (TypeReference ref) -> returnType[0] = ref
        );
        this.returnType = Objects.requireNonNull(returnType[0]);
        this.accessFlags = accessFlags;

        int argEnd = descriptor.length() - 2;
        while (argEnd >= 0) {
            if (descriptor.charAt(argEnd) == ')') break;
            argEnd--;
        }
        this.argEnd = argEnd + 1;
    }

    @Override
    public @NotNull MethodAccessFlags flags() {
        return new MethodAccessFlags(this.accessFlags);
    }

    @Override
    public @NotNull TypeReference returnType() {
        return this.returnType;
    }

    @Override
    public @NotNull TypeReference[] parameterTypes() {
        final TypeReference[] ret = new TypeReference[this.parameterCount];
        final int[] head = new int[] { 0 };
        readDescriptor(
                CharBuffer.wrap(this.descriptor),
                (TypeReference ref) -> ret[head[0]++] = ref,
                null
        );
        return ret;
    }

    @ApiStatus.Internal
    @Override
    public @NotNull String parameterDescriptor() {
        return this.descriptor.substring(0, this.argEnd);
    }

    @Override
    public @NotNull String descriptor() {
        return this.descriptor;
    }

}
