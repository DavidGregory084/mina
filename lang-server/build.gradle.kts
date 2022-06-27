plugins {
    application
    `ivy-publish`
    jacoco
    id("com.opencastsoftware.gradle.buildinfo") version "0.1.0"
    id("com.github.ben-manes.versions") version "0.42.0"
}

repositories {
    mavenCentral()
}

group = "org.mina-lang"
version = "0.1.0-SNAPSHOT"

val picocliVersion = "4.6.3"
val guiceVersion = "5.1.0"
val junixSocketVersion = "2.4.0"
val eclipseCollectionsVersion = "11.0.0"
val caffeineVersion = "3.1.0"
val lsp4jVersion = "0.14.0"
val slf4jVersion = "1.7.36"
val logbackVersion = "1.2.11"
val junitVersion = "5.8.2"
val jacocoVersion = "0.8.8"

dependencies {
    // Command Line Arg Parsing
    implementation("info.picocli:picocli:${picocliVersion}")

    // Dependency Injection
    implementation("com.google.inject:guice:${guiceVersion}")

    // Immutable Collections
    implementation("org.eclipse.collections:eclipse-collections-api:${eclipseCollectionsVersion}")
    implementation("org.eclipse.collections:eclipse-collections:${eclipseCollectionsVersion}")

    // Unix Socket Support
    implementation("com.kohlschutter.junixsocket:junixsocket-core:${junixSocketVersion}@pom") {
        isTransitive = true
    }

    // Language Server Protocol
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:${lsp4jVersion}")

    // Syntax Trees
    implementation("org.mina-lang:syntax:${version}")

    // Parser
    implementation("org.mina-lang:parser:${version}")

    // Logging
    implementation("org.slf4j:slf4j-api:${slf4jVersion}")
    implementation("org.slf4j:jul-to-slf4j:${slf4jVersion}")
    runtimeOnly("ch.qos.logback:logback-classic:${logbackVersion}")

    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:${junitVersion}")
}

buildInfo {
    packageName.set("org.mina_lang")
    properties.set(mapOf("version" to project.version.toString()))
}

application {
    mainClass.set("org.mina_lang.langserver.Launcher")
}

tasks.withType<Test> {
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