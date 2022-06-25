import com.google.protobuf.gradle.*

plugins {
    `java-library`
    `ivy-publish`
    id("com.google.protobuf") version "0.8.18"
}

repositories {
    mavenCentral()
}

group = "org.mina-lang"
version = "0.1.0-SNAPSHOT"

val protobufVersion = "3.20.0"

dependencies {
    // Intermediate Language Serialization
    implementation("com.google.protobuf:protobuf-java:${protobufVersion}")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${protobufVersion}"
    }
}

publishing {
    publications {
        create<IvyPublication>("ivyLocal") {
            from(components["java"])
        }
    }
    repositories {
        ivy {
            url = uri("${System.getProperty("user.home")}/.ivy2/local")
            layout("ivy")
        }
    }
}