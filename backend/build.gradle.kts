import com.google.protobuf.gradle.*

plugins {
    java
    id("com.google.protobuf") version "0.8.18"
}

repositories {
    maven("https://maven-central.storage-download.googleapis.com/maven2/")
    mavenCentral()
}

val eclipseCollectionsVersion = "11.0.0"
val protobufVersion = "3.20.0"
val junitVersion = "5.8.2"

dependencies {
    implementation("org.eclipse.collections:eclipse-collections-api:${eclipseCollectionsVersion}")
    implementation("org.eclipse.collections:eclipse-collections:${eclipseCollectionsVersion}")
    implementation("com.google.protobuf:protobuf-java:${protobufVersion}")

    testImplementation("org.junit.jupiter:junit-jupiter:${junitVersion}")
}

tasks.withType<Test> {
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
