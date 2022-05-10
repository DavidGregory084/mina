import com.google.protobuf.gradle.*

plugins {
    application
    antlr
    `ivy-publish`
    id("com.google.protobuf") version "0.8.18"
    id("com.opencastsoftware.gradle.buildinfo") version "0.1.0"
}

repositories {
    maven("https://maven-central.storage-download.googleapis.com/maven2/")
    mavenCentral()
}

group = "org.mina-lang"
version = "0.1.0-SNAPSHOT"

val antlrVersion = "4.10.1"
val picocliVersion = "4.6.3"
val junixSocketVersion = "2.4.0"
val eclipseCollectionsVersion = "11.0.0"
val caffeineVersion = "3.1.0"
val lsp4jVersion = "0.12.0"
val protobufVersion = "3.20.0"
val slf4jVersion = "1.7.36"
val logbackVersion = "1.2.11"
val junitVersion = "5.8.2"

dependencies {
    antlr("org.antlr:antlr4:${antlrVersion}")

    implementation("info.picocli:picocli:${picocliVersion}")

    implementation("com.kohlschutter.junixsocket:junixsocket-core:${junixSocketVersion}@pom") {
        isTransitive = true
    }

    implementation("org.eclipse.collections:eclipse-collections-api:${eclipseCollectionsVersion}")
    implementation("org.eclipse.collections:eclipse-collections:${eclipseCollectionsVersion}")

    implementation("com.github.ben-manes.caffeine:caffeine:${caffeineVersion}")
    
    implementation("com.google.protobuf:protobuf-java:${protobufVersion}")

    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:${lsp4jVersion}")

    implementation("org.slf4j:slf4j-api:${slf4jVersion}")
    implementation("org.slf4j:jul-to-slf4j:${slf4jVersion}")
    runtimeOnly("ch.qos.logback:logback-classic:${logbackVersion}")

    testImplementation("org.junit.jupiter:junit-jupiter:${junitVersion}")
}

buildInfo {
    packageName.set("org.mina_lang")
    properties.set(mapOf("version" to project.version.toString()))
}

application {
    mainClass.set("org.mina_lang.main.Server")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-no-listener", "-package", "org.mina_lang.parser")
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