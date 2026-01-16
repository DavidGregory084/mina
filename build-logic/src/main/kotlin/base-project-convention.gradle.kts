plugins {
    id("com.github.ben-manes.versions")
    id("com.diffplug.spotless")
    id("me.qoomon.git-versioning")
}

group = "org.mina-lang"

version = "0.0.0-SNAPSHOT"

val majorVersion =
    "\${describe.tag.version.major:-0}"
val minorVersion =
    "\${describe.tag.version.minor:-0}"
val patchVersion =
    "\${describe.tag.version.patch.next:-0}" // By default we're working toward the next snapshot
val branchVersionFormat =
    "${majorVersion}.${minorVersion}.${patchVersion}-SNAPSHOT"

gitVersioning.apply {
    refs {
        branch(".+") {
            describeTagPattern = "v(?<version>.+)"
            version = branchVersionFormat
        }
        tag("v(?<version>.+)") {
            version = "\${ref.version}"
        }
    }
    rev {
        describeTagPattern = "v(?<version>.+)"
        version = branchVersionFormat
    }
}

repositories { mavenCentral() }

spotless {
    ratchetFrom("origin/main")

    kotlinGradle {
        encoding("UTF-8")
        target("*.gradle.kts")
        ktfmt().kotlinlangStyle()
        leadingTabsToSpaces()
        trimTrailingWhitespace()
        endWithNewline()
    }
}
