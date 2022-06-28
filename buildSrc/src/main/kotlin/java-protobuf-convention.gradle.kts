import com.google.protobuf.gradle.*

plugins {
    `java-library`
    id("com.google.protobuf")
}

repositories {
    maven("https://maven-central.storage-download.googleapis.com/maven2/")
}

val protobufVersion = "3.20.0"

dependencies {
    implementation("com.google.protobuf:protobuf-java:${protobufVersion}")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${protobufVersion}"
    }
}
