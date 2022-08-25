import org.gradle.accessors.dm.LibrariesForLibs

// Workaround for https://github.com/gradle/gradle/issues/15383
val libs = the<LibrariesForLibs>()

plugins {
    java
    `ivy-publish`
    eclipse
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
}

java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("--enable-preview")
}

eclipse.jdt.file {
    withProperties {
        setProperty("org.eclipse.jdt.core.compiler.problem.enablePreviewFeatures", "enabled")
        setProperty("org.eclipse.jdt.core.compiler.problem.reportPreviewFeatures", "ignore")
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    jvmArgs("--enable-preview")
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
