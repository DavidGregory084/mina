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
}
