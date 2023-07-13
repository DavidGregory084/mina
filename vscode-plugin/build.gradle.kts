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

        val languageServerVersionPath =
            "/contributes/configuration/0/properties/mina.languageServer.version/default"
        val profilingAgentVersionPath =
            "/contributes/configuration/2/properties/mina.languageServer.profiling.agentVersion/default"

        // Pending https://github.com/diffplug/spotless/pull/1753
        //
        // applyJsonPatch(
        //     listOf(
        //         // Replace VS code default configuration for language server version
        //         mapOf(
        //             "op" to "replace",
        //             "path" to languageServerVersionPath,
        //             "value" to rootProject.project("mina-lang-server").version.toString()
        //         ),
        //         // Replace VS code default configuration for profiling agent version
        //         mapOf(
        //             "op" to "replace",
        //             "path" to profilingAgentVersionPath,
        //             "value" to libs.versions.pyroscopeAgent.get()
        //         )
        //     )
        // )

        prettier()
    }
}

val npmVersion by
    tasks.registering(NpmTask::class) {
        dependsOn(tasks.npmInstall)
        npmCommand.set(
            listOf(
                "version",
                "--no-git-tag-version",
                "--allow-same-version",
                project.version.toString()
            )
        )
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
