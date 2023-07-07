import com.google.protobuf.gradle.*
import org.gradle.accessors.dm.LibrariesForLibs

// Workaround for https://github.com/gradle/gradle/issues/15383
val libs = the<LibrariesForLibs>()

plugins {
    `java-library`
    id("com.google.protobuf")
}

repositories { maven("https://maven-central.storage-download.googleapis.com/maven2/") }

dependencies { implementation(libs.protobufJava) }

protobuf { protoc { artifact = libs.protoc.get().toString() } }
