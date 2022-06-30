plugins {
    application
    `java-project-convention`
    id("com.opencastsoftware.gradle.buildinfo") version "0.1.0"
}

val picocliVersion = "4.6.3"
val junixSocketVersion = "2.5.0"
val caffeineVersion = "3.1.0"
val lsp4jVersion = "0.14.0"
val logbackVersion = "1.2.11"

dependencies {
    // Command Line Arg Parsing
    implementation("info.picocli:picocli:${picocliVersion}")

    // Dependency Injection
    implementation(libs.guice)

    // Immutable Collections
    implementation(libs.bundles.eclipse.collections)

    // Unix Socket Support
    implementation("com.kohlschutter.junixsocket:junixsocket-core:${junixSocketVersion}@pom") {
        isTransitive = true
    }

    // Language Server Protocol
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:${lsp4jVersion}")

    // Syntax Trees
    implementation(project(":compiler:syntax"))

    // Parser
    implementation(project(":compiler:parser"))

    // Logging
    implementation(libs.bundles.slf4j)
    runtimeOnly("ch.qos.logback:logback-classic:${logbackVersion}")
}

buildInfo {
    packageName.set("org.mina_lang")
    properties.set(mapOf("version" to project.version.toString()))
}

application {
    mainClass.set("org.mina_lang.langserver.Launcher")
}
