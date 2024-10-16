package io.github.wasabithumb.annolyze.misc;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class PathUtil {

    public static final String DOT_CLASS = ".class";

    @ApiStatus.Internal
    public static @NotNull String dotsToSlashes(@NotNull String className, boolean addSuffix) {
        final StringBuilder path = new StringBuilder(className.length() + (addSuffix ? 6 : 0));
        char c;
        for (int i=0; i < className.length(); i++) {
            c = className.charAt(i);
            if (c == '.') c = '/';
            path.append(c);
        }
        if (addSuffix) path.append(DOT_CLASS);
        return path.toString();
    }

    @ApiStatus.Internal
    public static @NotNull String classNameToPath(@NotNull String className) {
        return dotsToSlashes(className, true);
    }

    @ApiStatus.Internal
    public static @NotNull String slashesToDots(@NotNull String slashed) {
        final int len = slashed.length();
        char[] ret = new char[len];

        char c;
        for (int i=0; i < len; i++) {
            c = slashed.charAt(i);
            if (c == '/') c = '.';
            ret[i] = c;
        }

        return new String(ret);
    }

}
