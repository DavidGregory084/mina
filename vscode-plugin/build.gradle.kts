import com.github.gradle.node.npm.task.NpmTask
import org.gradle.internal.os.OperatingSystem

plugins {
    alias(libs.plugins.gradleNode)
}

node {
    download.set(false)
}

tasks.register<NpmTask>("build") {
    dependsOn(":lang-server:publish")
    dependsOn(tasks.npmInstall)
    npmCommand.set(
        if (OperatingSystem.current().isLinux())
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