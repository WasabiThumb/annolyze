plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.4"
}

allprojects {
    group = "io.github.wasabithumb"
    version = "0.1.0"
}

description = "Reads annotation info from class files without linkage"

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

tasks.test {
    useJUnitPlatform()
}

allprojects {
    listOf(
        "java-library",
        "maven-publish",
        "signing",
        "net.thebugmc.gradle.sonatype-central-portal-publisher"
    ).forEach { pluginManager.apply(it) }

    val targetJavaVersion = 17
    java {
        val javaVersion = JavaVersion.toVersion(targetJavaVersion)
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion

        toolchain {
            languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
        }

        withSourcesJar()
        withJavadocJar()
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
        options.release.set(targetJavaVersion)
    }

    tasks.javadoc {
        (options as CoreJavadocOptions)
            .addBooleanOption("Xdoclint:none", true)
    }
}

centralPortal {
    name = rootProject.name
    jarTask = tasks.jar
    sourcesJarTask = tasks.sourcesJar
    javadocJarTask = tasks.javadocJar
    pom {
        name = "Annolyze"
        description = project.description
        url = "https://github.com/WasabiThumb/annolyze"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "wasabithumb"
                email = "wasabithumbs@gmail.com"
                organization = "Wasabi Codes"
                organizationUrl = "https://wasabithumb.github.io/"
                timezone = "-5"
            }
        }
        scm {
            connection = "scm:git:git://github.com/WasabiThumb/annolyze.git"
            url = "https://github.com/WasabiThumb/annolyze"
        }
    }
}
