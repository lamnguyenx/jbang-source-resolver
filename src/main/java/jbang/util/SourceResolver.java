package jbang.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves the filesystem path of the currently executing jbang script.
 *
 * <p>Works in three modes:
 * <ol>
 *   <li><b>Shebang</b> — when the script is executed directly (e.g. {@code ./script.java}),
 *       jbang exposes the source path in the {@code jbang.source} system property.</li>
 *   <li><b>jbang CLI</b> — when invoked as {@code jbang script.java}, the source file name
 *       is reconstructed from the jbang jar cache directory name and located by walking
 *       upward from the current working directory.</li>
 *   <li><b>Fallback</b> — throws if none of the above succeed.</li>
 * </ol>
 */
public final class SourceResolver {

    private SourceResolver() {}

    /**
     * Returns the absolute, normalized path to the currently executing jbang script.
     *
     * @return script source path
     * @throws IllegalStateException if the path cannot be determined
     */
    public static Path resolve() {
        String sourceProp = System.getProperty("jbang.source");
        if (sourceProp != null) {
            return Paths.get(sourceProp).toAbsolutePath().normalize();
        }

        String filename = extractSourceFilenameFromClasspath();
        if (filename != null) {
            Path cwd = Paths.get(System.getProperty("user.dir"));
            try (var walk = Files.find(cwd, 10,
                    (path, attrs) -> path.getFileName().toString().equals(filename))) {
                return walk.findFirst()
                        .orElse(cwd.resolve(filename))
                        .toAbsolutePath()
                        .normalize();
            } catch (IOException e) {
                return cwd.resolve(filename).toAbsolutePath().normalize();
            }
        }

        throw new IllegalStateException(
                "Cannot determine script source path. " +
                "Make sure you are running via jbang or that 'jbang.source' system property is set.");
    }

    /**
     * Extracts the original {@code *.java} filename from the jbang jar cache directory name.
     *
     * <p>jbang stores cached jars in directories named
     * {@code <filename>.java.<64-char-sha256>}.  This method strips the trailing hash.
     */
    static String extractSourceFilenameFromClasspath() {
        String cp = System.getProperty("java.class.path");
        for (String path : cp.split(File.pathSeparator)) {
            if (path.contains(".jbang/cache/jars/")) {
                File parent = new File(path).getParentFile();
                if (parent != null) {
                    String name = parent.getName();
                    int dot = name.lastIndexOf('.');
                    if (dot > 0) {
                        String hash = name.substring(dot + 1);
                        if (hash.length() == 64) {
                            return name.substring(0, dot);
                        }
                    }
                }
                break;
            }
        }
        return null;
    }
}
