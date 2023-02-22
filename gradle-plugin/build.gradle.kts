plugins {
    `java-gradle-plugin`
    `java-project-convention`
    alias(libs.plugins.gradleBuildInfo)
}

gradlePlugin {
    val mina by plugins.creating {
        id = "org.mina-lang.gradle"
        implementationClass = "org.mina_lang.gradle.MinaPlugin"
    }
}

buildInfo {
    packageName.set("org.mina_lang.gradle")
    properties.set(mapOf("version" to project.version.toString()))
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
    tasks[getCompileTaskName("java")].dependsOn("generateBuildInfo")
    java.srcDir(project.buildDir.resolve("generated/sources/buildinfo/java/main"))
}

configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
    useJUnitPlatform()
    // We use the published CLI artifact in the functional tests
    // and sadly Gradle doesn't seem to have a way of publishing transitively
    // or on aggregate from a child project
    dependsOn(":compiler:mina-compiler-common:publishToMavenLocal")
    dependsOn(":compiler:mina-compiler-syntax:publishToMavenLocal")
    dependsOn(":compiler:mina-compiler-parser:publishToMavenLocal")
    dependsOn(":compiler:mina-compiler-renamer:publishToMavenLocal")
    dependsOn(":compiler:mina-compiler-typechecker:publishToMavenLocal")
    dependsOn(":compiler:mina-compiler-jvm:publishToMavenLocal")
    dependsOn(":compiler:mina-compiler-main:publishToMavenLocal")
    dependsOn(":compiler:mina-compiler:publishToMavenLocal")
}

gradlePlugin.testSourceSets(functionalTestSourceSet)

tasks.named<Task>("check") {
    dependsOn(functionalTest)
}
