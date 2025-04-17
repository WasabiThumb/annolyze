# NOTICE
Since the release of Java 24 (and by extension the [class-file API](https://openjdk.org/jeps/484)), this library is redundant.
The relevant documentation can be found [here](https://docs.oracle.com/en/java/javase/24/docs/api/java.base/java/lang/classfile/package-summary.html#reading-classfiles-heading).
Hence, this repository has been archived. To see the original README, click the spoiler below.

<details>
<summary>Original README</summary>
    
# Annolyze
A Java 17 - 21[†](#support) library for reading annotation & basic layout info from ``.class`` files without loading them into the JVM. This approach simply ignores linking steps, providing utilities to resolve references at runtime.
Also provides utilities for class sources like ``ClassLoader``, archives and directories.

## Usage
### Java API
The ``Annolyze`` entry point is comprised of various wrappers around ``ClassFileInputStream``, which is safe to use in isolation.
```java
// Read a class file on the filesystem.
ClassFile cf = Annolyze.read(new File("Foo.class"));
cf.simpleName();                                        // Foo
cf.reference().resolve();                               // Foo.class
cf.getAnnotations();                                    // List<ClassReference>
cf.getMembers();                                        // Set<MemberReference<?>>
cf.getFieldAnnotations("bar");                          // List<ClassReference>
cf.getMethodAnnotations("baz", PrimitiveReference.INT); // List<ClassReference>
cf.getMethodAnnotations("baz", Integer.TYPE);           // List<ClassReference>

// Read a class file in a JAR. If unspecified, the code source of the caller
// is asserted to be an archive.
Annolyze.archive(new File("lib.jar"))
    .read("com.example.lib.Hello");                     // ClassFile

// Read a class file in a directory. Dot characters (.) are parsed as file separators.
// If unspecified, the code source of the caller is asserted to be a directory.
// Fully qualified names are still required.
Annolyze.directory()
    .sub("com.example")
    .read("lib.Hello");                                 // ClassFile
```

### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("io.github.wasabithumb:annolyze:0.1.0")
}
```

### Gradle (Groovy DSL)
```groovy
dependencies {
    implementation 'io.github.wasabithumb:annolyze:0.1.0'
}
```

### Maven
```xml
<dependencies>
    <dependency>
        <groupId>io.github.wasabithumb</groupId>
        <artifactId>annolyze</artifactId>
        <version>0.1.0</version>
    </dependency>
    <dependency>
        <groupId>io.github.wasabithumb</groupId>
        <artifactId>annolyze-internals</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

## Mission
Meant primarily for use in compile-time preprocessing, such as with Gradle to create indexes for annotated classes,
methods or fields to eliminate search time & memory usage at runtime. If target classes are likely to be loaded, or
avoiding linkage errors is not a concern, then regular Java reflection may be a better fit.

## Support
Annolyze was written against the [Java 21 (major version 65) class file format](https://docs.oracle.com/javase/specs/jvms/se21/html/jvms-4.html). This means it will always be able to read class files with a major version of 65 or less. However, if the
runtime JRE is detected to have a version greater than 21, this library will *allow* reading class files built for that
version and all lower versions. In this case, behavior is undefined (though likely to succeed).

Annolyze may receive updates to continue supporting the class file major version for the latest LTS release of Java.

## License
```
Copyright 2024 Wasabi Codes

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
</details>
