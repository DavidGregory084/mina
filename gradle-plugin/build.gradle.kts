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
}

gradlePlugin.testSourceSets(functionalTestSourceSet)

tasks.named<Task>("check") {
    // We use the published CLI artifact in the functional tests
    dependsOn(":compiler:mina-compiler:publishToMavenLocal")
    dependsOn(functionalTest)
}
