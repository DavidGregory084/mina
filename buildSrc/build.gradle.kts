plugins {
    `kotlin-dsl`
}

repositories {
    maven("https://maven-central.storage-download.googleapis.com/maven2/")
    mavenCentral()
    gradlePluginPortal() 
}

dependencies {
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.8.18")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.42.0")
}
