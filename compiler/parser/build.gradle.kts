plugins {
    `java-library`
    `java-project-convention`
    antlr
}

// Workaround for https://github.com/gradle/gradle/issues/820
configurations { api { setExtendsFrom(extendsFrom.filterNot { it == antlr.get() }) } }

spotless {
    antlr4 {
        licenseHeader(
            """
            /*
             * SPDX-FileCopyrightText:  © ${"$"}YEAR David Gregory
             * SPDX-License-Identifier: Apache-2.0
             */
            """
                .trimIndent()
        )
        antlr4Formatter()
    }
}

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

tasks.generateGrammarSource {
    // Working around ANTLR Gradle plugin bug with separate lexer & parser grammars
    // See: https://github.com/antlr/antlr4/issues/2335
    val outputDir = file("build/generated-src/antlr/main/org/mina_lang/parser")
    // the directory must exist or ANTLR bails
    doFirst { outputDir.mkdirs() }
    arguments.addAll(
        listOf(
            "-visitor",
            "-no-listener",
            // the lexer tokens file can't be found by the parser without this
            "-lib",
            outputDir.absolutePath,
            "-package",
            "org.mina_lang.parser"
        )
    )
}
