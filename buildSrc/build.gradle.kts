plugins {
    `kotlin-dsl`
}

repositories {
    maven("https://maven-central.storage-download.googleapis.com/maven2/")
    mavenCentral()
    gradlePluginPortal() 
}

dependencies {
    implementation(libs.protobufPlugin)
    implementation(libs.gradleVersionsPlugin)

    // Workaround for https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
