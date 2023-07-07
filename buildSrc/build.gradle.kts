plugins {
    `kotlin-dsl`
    alias(libs.plugins.spotless)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

spotless {
    ratchetFrom("origin/main")

    kotlinGradle {
        encoding("UTF-8")
        target("**/*.gradle.kts")
        ktfmt().kotlinlangStyle()
        indentWithSpaces()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

dependencies {
    implementation(libs.protobufPlugin)
    implementation(libs.gradleVersionsPlugin)
    implementation(libs.spotlessGradlePlugin)

    // Workaround for https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
