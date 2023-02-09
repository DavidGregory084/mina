import org.gradle.accessors.dm.LibrariesForLibs

// Workaround for https://github.com/gradle/gradle/issues/15383
val libs = the<LibrariesForLibs>()

plugins {
    java
    `ivy-publish`
    jacoco
    id("com.github.ben-manes.versions")
}

repositories {
    mavenCentral()
}

group = "org.mina-lang"
version = "0.1.0-SNAPSHOT"

dependencies {
    testImplementation(libs.junitJupiter)
    testImplementation(libs.hamcrest)
    testImplementation(libs.hamcrestOptional)
    testImplementation(libs.jqwik)
    testImplementation(libs.equalsVerifier)
    testImplementation(libs.toStringVerifier)
    constraints {
        implementation("org.apache.commons:commons-text:[1.10.0,)") {
            because("CVE-2022-42889")
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<Javadoc> {
    source = sourceSets.main.get().allJava

    classpath = configurations["compileClasspath"]

    options {
        this as StandardJavadocDocletOptions
        addBooleanOption("Xdoclint:none", true)
        addBooleanOption("-allow-script-in-comments", true)
        header("""
        <script src="https://polyfill.io/v3/polyfill.min.js?features=es6"></script>
        <script>MathJax = {chtml:{displayAlign:'left'}};</script>
        <script id="MathJax-script" async src="https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js"></script>
        """.stripIndent())
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    enableAssertions = true
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
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
