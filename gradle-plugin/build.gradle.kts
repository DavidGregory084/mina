plugins {
    `java-gradle-plugin`
    jacoco
}

repositories {
    mavenCentral()
}

val junitVersion = "5.8.2"
val jacocoVersion = "0.8.8"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:${junitVersion}")
}

gradlePlugin {
    val mina by plugins.creating {
        id = "org.mina_lang.gradle"
        implementationClass = "org.mina_lang.gradle.MinaPlugin"
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
}

gradlePlugin.testSourceSets(functionalTestSourceSet)

tasks.named<Task>("check") {
    dependsOn(functionalTest)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

jacoco {
    toolVersion = "${jacocoVersion}"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}
