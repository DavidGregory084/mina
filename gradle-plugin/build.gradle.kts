plugins {
    `java-gradle-plugin`
    id("java-project-convention")
    alias(libs.plugins.gradleBuildInfo)
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
}

val pluginId = "org.mina-lang.gradle"

gradlePlugin {
    plugins.create("mina") {
        id = pluginId
        implementationClass = "org.mina_lang.gradle.MinaPlugin"
    }
}

buildInfo {
    packageName.set("org.mina_lang.gradle")
    properties.set(mapOf("version" to project.version.toString(), "pluginId" to pluginId))
}

dependencies {
    implementation(libs.gradleBspPlugin)
    compileOnly(project(":compiler:mina-compiler-main"))
    compileOnly(libs.yvette)
}

testing {
    suites {
        register<JvmTestSuite>("functionalTest") {
            sources {
                gradlePlugin.testSourceSets(this)

                dependencies {
                    implementation(project())
                    implementation(libs.junitJupiter)
                    implementation(libs.hamcrest)
                }

                targets {
                    all {
                        testTask.configure {
                            dependsOn(":compiler:mina-compiler-common:publishToMavenLocal")
                            dependsOn(":compiler:mina-compiler-syntax:publishToMavenLocal")
                            dependsOn(":compiler:mina-compiler-parser:publishToMavenLocal")
                            dependsOn(":compiler:mina-compiler-renamer:publishToMavenLocal")
                            dependsOn(":compiler:mina-compiler-typechecker:publishToMavenLocal")
                            dependsOn(":compiler:mina-compiler-jvm:publishToMavenLocal")
                            dependsOn(":compiler:mina-compiler-main:publishToMavenLocal")
                            dependsOn(":compiler:mina-compiler:publishToMavenLocal")
                            dependsOn(":mina-runtime:publishToMavenLocal")
                        }
                    }
                }
            }
        }
    }
}

tasks.check { dependsOn(testing.suites.named("functionalTest")) }
