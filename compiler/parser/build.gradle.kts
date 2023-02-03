plugins {
    `java-library`
    `java-project-convention`
    antlr
}

dependencies {
    // String escaping
    implementation(libs.apacheCommonsText)

    // Syntax Tree Parsing
    antlr(libs.antlr)

    // Syntax Tree Definitions
    api(project(":compiler:syntax"))
}

tasks.generateGrammarSource {
    // Working around ANTLR Gradle plugin bug with separate lexer & parser grammars
    // See: https://github.com/antlr/antlr4/issues/2335
    val outputDir = file("build/generated-src/antlr/main/org/mina_lang/parser")
    // the directory must exist or ANTLR bails
    doFirst { outputDir.mkdirs() }
    arguments = arguments + listOf(
        "-visitor",
        "-no-listener",
        // the lexer tokens file can't be found by the parser without this
        "-lib", outputDir.absolutePath,
        "-package", "org.mina_lang.parser")
}
