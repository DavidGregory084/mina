// Workaround for https://github.com/gradle/gradle/issues/15383
dependencyResolutionManagement {
    versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }
}

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
