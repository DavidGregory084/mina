import com.diffplug.gradle.spotless.JsonExtension
import com.github.gradle.node.npm.task.NpmTask
import org.gradle.internal.os.OperatingSystem

plugins {
    `base-project-convention`
    alias(libs.plugins.gradleNode)
}

node { download.set(false) }

spotless {
    typescript {
        target("src/**/*.ts")
        prettier()
    }

    json {
        target("tsconfig.json", "language-configuration.json", "syntaxes/**/*.json")
        prettier()
    }

    format("packageJson", JsonExtension::class.java) {
        target("package.json")
        // This should preferably be done using a JSON patch
        val numericId = """(0|[1-9]\d*)"""
        val idChar = """[0-9a-zA-Z-]"""
        val nonDigit = """[a-zA-Z-]"""
        val alphaNum = """(?:0|[1-9]\d*|\d*${nonDigit}${idChar}*)"""
        val preRelease = """(?:-(${alphaNum}(?:\.${alphaNum})*))"""
        val buildId = """(?:\+(${idChar}+(?:\.${idChar}+)*))"""
        val semVer = """${numericId}\.${numericId}\.${numericId}${preRelease}?${buildId}?"""
        replaceRegex(
            "package-version",
            """(?<="version": ")${semVer}(?=",)""",
            project.version.toString()
        )
        replaceRegex(
            "server-version",
            """(?<="default": ")${semVer}(?=",)""",
            project.version.toString()
        )
        prettier()
    }
}

val npmBuild by
    tasks.registering(NpmTask::class) {
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
            if (OperatingSystem.current().isLinux) listOf("run", "test-linux")
            else listOf("run", "test")
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

tasks.build { dependsOn(npmBuild) }
