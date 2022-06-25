plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

group = "org.mina-lang"
version = "0.1.0-SNAPSHOT"

dependencies {
    // Intermediate Language Definitions
    implementation("org.mina-lang:intermediate:${version}")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
