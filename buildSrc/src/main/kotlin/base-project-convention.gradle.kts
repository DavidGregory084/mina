plugins {
    id("com.github.ben-manes.versions")
    id("com.diffplug.spotless")
}

group = "org.mina-lang"

version = "0.1.0-SNAPSHOT"

repositories { mavenCentral() }

spotless {
    ratchetFrom("origin/main")

    kotlinGradle {
        encoding("UTF-8")
        target("*.gradle.kts")
        ktfmt().kotlinlangStyle()
        indentWithSpaces()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
