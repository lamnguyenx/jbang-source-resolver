# jbang-source-resolver

Tiny zero-dependency utility that resolves the filesystem path of the
currently executing **jbang** script.

## Usage

```java
///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.github.lamnguyenx:jbang-source-resolver:1.0.0

import jbang.util.SourceResolver;
import java.nio.file.Path;

class MyScript {
    public static void main(String[] args) {
        Path source = SourceResolver.resolve();
        System.out.println("Running: " + source);
    }
}
```

## How it works

1. **Shebang mode** — when executed directly (`./script.java`), jbang sets the
   `jbang.source` system property and the library returns it immediately.
2. **CLI mode** — when invoked as `jbang script.java`, the library reconstructs
   the original filename from the jbang jar-cache directory name, then walks
   upward from the current working directory to locate the source file.
3. **Fallback** — throws `IllegalStateException` if the path cannot be determined.

## Installation

Published via [JitPack](https://jitpack.io):

```groovy
// Gradle
implementation 'com.github.lamnguyenx:jbang-source-resolver:1.0.0'
```

```xml
<!-- Maven -->
<dependency>
    <groupId>com.github.lamnguyenx</groupId>
    <artifactId>jbang-source-resolver</artifactId>
    <version>1.0.0</version>
</dependency>
```

## License

MIT
