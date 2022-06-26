plugins {
    `java-library`
    `ivy-publish`
    jacoco
    antlr
}

repositories {
    mavenCentral()
}

group = "org.mina-lang"
version = "0.1.0-SNAPSHOT"

val antlrVersion = "4.10.1"
val eclipseCollectionsVersion = "11.0.0"
val lsp4jVersion = "0.14.0"
val junitVersion = "5.8.2"
val jacocoVersion = "0.8.8"

dependencies {
    // Immutable Collections
    implementation("org.eclipse.collections:eclipse-collections-api:${eclipseCollectionsVersion}")
    implementation("org.eclipse.collections:eclipse-collections:${eclipseCollectionsVersion}")

    // Syntax Tree Parsing
    antlr("org.antlr:antlr4:${antlrVersion}")

    // Common Definitions
    implementation("org.mina-lang:common:${version}")

    // Syntax Tree Definitions
    implementation("org.mina-lang:syntax:${version}")

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

jacoco {
    toolVersion = "${jacocoVersion}"
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-no-listener", "-package", "org.mina_lang.parser")
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