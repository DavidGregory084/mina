plugins {
    `java-library`
    id("java-project-convention")
}

spotless {
    antlr4 {
        target("src/**/*.g4")
        licenseHeader(
                """
            /*
             * SPDX-FileCopyrightText:  © ${"$"}YEAR David Gregory
             * SPDX-License-Identifier: Apache-2.0
             */
            """
                    .trimIndent()
            )
            .updateYearWithLatest(false)
    }
}

val antlr by configurations.creating

dependencies {
    // String Escaping
    implementation(libs.apacheCommonsText)

    // Parser Generation
    antlr(libs.antlr)

    // Syntax Tree Parsing
    api(libs.antlrRuntime)

    // Syntax Tree Definitions
    api(project(":compiler:mina-compiler-syntax"))
}

val antlrSrcDir = file("src/main/antlr/org/mina_lang/parser")
val antlrOutDir = file("build/generated-src/antlr/main/org/mina_lang/parser")

val lexerSrcFile = antlrSrcDir.resolve("MinaLexer.g4")

val generateMinaLexer by
    tasks.creating(JavaExec::class) {
        mainClass = "org.antlr.v4.Tool"
        classpath(antlr)
        doFirst { mkdir(antlrOutDir) }
        args(
            listOf(
                "-no-listener",
                "-o",
                antlrOutDir.absolutePath,
                "-package",
                "org.mina_lang.parser",
                lexerSrcFile.absolutePath,
            )
        )
    }

val parserSrcFile = antlrSrcDir.resolve("MinaParser.g4")

val generateMinaParser by
    tasks.creating(JavaExec::class) {
        dependsOn(generateMinaLexer)
        mainClass = "org.antlr.v4.Tool"
        classpath(antlr)
        args(
            listOf(
                "-visitor",
                "-no-listener",
                "-lib",
                antlrOutDir.absolutePath,
                "-o",
                antlrOutDir.absolutePath,
                "-package",
                "org.mina_lang.parser",
                parserSrcFile.absolutePath,
            )
        )
    }

val generatedSrcDir = file("build/generated-src/antlr/main/")

sourceSets { main { java { srcDirs(generatedSrcDir) } } }

tasks.compileJava { dependsOn(generateMinaParser) }
