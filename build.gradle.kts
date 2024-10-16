plugins {
    id("java-library")
}

allprojects {
    group = "io.github.wasabithumb"
    version = "0.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":internals"))
    compileOnly("org.jetbrains:annotations:26.0.1")
    testImplementation("org.jetbrains:annotations:26.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.compileJava {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

tasks.test {
    useJUnitPlatform()
}
