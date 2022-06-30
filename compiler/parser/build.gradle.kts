plugins {
    `java-library`
    `java-project-convention`
    antlr
}

dependencies {
    // Immutable Collections
    implementation(libs.bundles.eclipse.collections)

    // Syntax Tree Parsing
    antlr(libs.antlr)

    // Common Definitions
    implementation(project(":compiler:common"))

    // Syntax Tree Definitions
    implementation(project(":compiler:syntax"))
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-no-listener", "-package", "org.mina_lang.parser")
}
