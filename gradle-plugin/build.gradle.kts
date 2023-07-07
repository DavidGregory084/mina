plugins {
    `java-gradle-plugin`
    `java-project-convention`
    alias(libs.plugins.gradleBuildInfo)
}

gradlePlugin {
    plugins.create("mina") {
        id = "org.mina-lang.gradle"
        implementationClass = "org.mina_lang.gradle.MinaPlugin"
    }
}

buildInfo {
    packageName.set("org.mina_lang.gradle")
    properties.set(mapOf("version" to project.version.toString()))
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
                        }
                    }
                }
            }
        }
    }
}

tasks.check { dependsOn(testing.suites.named("functionalTest")) }
