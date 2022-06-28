plugins {
    `kotlin-dsl`
}

repositories {
    maven("https://maven-central.storage-download.googleapis.com/maven2/")
    mavenCentral()
}

dependencies {
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.8.18")
}
