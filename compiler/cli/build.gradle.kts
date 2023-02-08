import org.asciidoctor.gradle.jvm.AsciidoctorTask

plugins {
    application
    `java-project-convention`
    alias(libs.plugins.asciiDoctor)
}

dependencies {
    // Command Line Arg Parsing
    implementation(libs.picocli)
    annotationProcessor(libs.picocliCodegen)

    // Compiler Main
    implementation(project(":compiler:main"))

    // Logging
    implementation(libs.slf4jApi)
    implementation(libs.logback)

    // Diagnostic Reporting
    implementation(libs.jansi)

    // Failable Streams
    implementation(libs.apacheCommonsLang)
}

tasks.register<JavaExec>("generateManpageAsciiDoc") {
    dependsOn(tasks.classes)
    group = "documentation"
    description = "Generate AsciiDoc manpage"
    classpath = files(listOf(configurations.compileClasspath, configurations.annotationProcessor, sourceSets.main.get().runtimeClasspath))
    main = "picocli.codegen.docgen.manpage.ManPageGenerator"
    args = listOf(application.mainClassName, "--outdir=${project.buildDir}/generated-picocli-docs", "-v")
}

tasks.named<AsciidoctorTask>("asciidoctor") {
    dependsOn("generateManpageAsciiDoc")
    setSourceDir("${project.buildDir}/generated-picocli-docs")
    setOutputDir("${project.buildDir}/docs")
    outputOptions {
        backends("manpage", "html5")
    }
}

tasks.withType<JavaCompile> {
	val compilerArgs = options.compilerArgs
	compilerArgs.add("-Aproject=${project.group}/${project.name}")
}

application {
    applicationName = "minac"
    mainClass.set("org.mina_lang.cli.MinaCommandLine")
}
