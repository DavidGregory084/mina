plugins {
    `java-library`
    `java-project-convention`
    antlr
}

val antlrVersion = "4.10.1"
val eclipseCollectionsVersion = "11.0.0"
val lsp4jVersion = "0.14.0"

dependencies {
    // Immutable Collections
    implementation("org.eclipse.collections:eclipse-collections-api:${eclipseCollectionsVersion}")
    implementation("org.eclipse.collections:eclipse-collections:${eclipseCollectionsVersion}")

    // Syntax Tree Parsing
    antlr("org.antlr:antlr4:${antlrVersion}")

    // Common Definitions
    implementation(project(":compiler:common"))

    // Syntax Tree Definitions
    implementation(project(":compiler:syntax"))
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor", "-no-listener", "-package", "org.mina_lang.parser")
}
