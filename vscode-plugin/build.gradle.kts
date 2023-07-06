import com.github.gradle.node.npm.task.NpmTask
import org.gradle.internal.os.OperatingSystem

plugins {
    alias(libs.plugins.gradleNode)
    id("com.diffplug.spotless")
}

node {
    download.set(false)
}

spotless {
    ratchetFrom("origin/main")

    typescript {
        target("src/**/*.ts")
        prettier()
    }

    json {
        target("syntaxes/**/*.json")
        prettier()
    }
}

val npmBuild by tasks.registering(NpmTask::class) {
    dependsOn(":compiler:mina-compiler-common:publish")
    dependsOn(":compiler:mina-compiler-syntax:publish")
    dependsOn(":compiler:mina-compiler-parser:publish")
    dependsOn(":compiler:mina-compiler-renamer:publish")
    dependsOn(":compiler:mina-compiler-typechecker:publish")
    dependsOn(":compiler:mina-compiler-jvm:publish")
    dependsOn(":compiler:mina-compiler-main:publish")
    dependsOn(":mina-lang-server:publish")
    dependsOn(tasks.npmInstall)
    npmCommand.set(
        if (OperatingSystem.current().isLinux)
            listOf("run", "test-linux")
        else
            listOf("run", "test")
    )
    inputs.dir("src")
    inputs.dir("syntaxes")
    inputs.dir("node_modules")
    inputs.file("package.json")
    inputs.file("package-lock.json")
    inputs.file("language-configuration.json")
    inputs.file("tsconfig.json")
    outputs.dir("${project.projectDir}/out")
}

tasks.build {
    dependsOn(npmBuild)
}