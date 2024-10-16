package io.github.wasabithumb.annolyze.cp;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A subset of the JVM constant pool that holds only strings.
 */
@ApiStatus.Internal
public class ConstantPool {

    final ConstantPoolString[] data;
    public ConstantPool(int size) {
        this.data = new ConstantPoolString[size - 1];
    }

    public void set(int index, @Nullable ConstantPoolString value) {
        this.data[index - 1] = value;
    }

    private @NotNull String get(int index, int[] sub, int subOffset, final int cycleGuard) throws ConstantPoolException {
        if (index == 0) throw new ConstantPoolException("Invalid constant pool entry #0");
        ConstantPoolString str = this.data[index - 1];
        if (str == null) {
            throw new ConstantPoolException("Constant pool entry #" + cycleGuard + " refers to a non-string");
        }
        if (str.isLiteral()) return str.value();

        int n;
        if (subOffset < sub.length) {
            n = sub[subOffset++];
        } else {
            n = 0;
        }

        final int[] targets = str.target();
        final int target = targets[Math.min(n, targets.length - 1)];
        if (cycleGuard == target) {
            throw new ConstantPoolException("Constant pool entry #" + cycleGuard + " refers to itself");
        }
        return this.get(target, sub, subOffset, cycleGuard);
    }

    public @NotNull String get(int index, int... sub) throws ConstantPoolException {
        return this.get(index, sub, 0, index);
    }

    public @NotNull String get(int index) throws ConstantPoolException {
        return this.get(index, 0);
    }

}
