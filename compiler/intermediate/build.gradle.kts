import com.google.protobuf.gradle.*

plugins {
    `java-library`
    `ivy-publish`
    jacoco
    id("com.google.protobuf") version "0.8.18"
}

repositories {
    mavenCentral()
}

group = "org.mina-lang"
version = "0.1.0-SNAPSHOT"

val protobufVersion = "3.20.0"
val junitVersion = "5.8.2"
val jacocoVersion = "0.8.8"

dependencies {
    // Intermediate Language Serialization
    implementation("com.google.protobuf:protobuf-java:${protobufVersion}")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:${junitVersion}")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

jacoco {
    toolVersion = "${jacocoVersion}"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
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