
description = "Non-API runtime utilities for Annolyze"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.0.1")
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

centralPortal {
    name = "${rootProject.name}-internals"
    jarTask = tasks.jar
    sourcesJarTask = tasks.sourcesJar
    javadocJarTask = tasks.javadocJar
    pom {
        name = "Annolyze Internals"
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
