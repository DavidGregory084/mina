plugins {
    `java-library`
    id("java-project-convention")
}

dependencies {
    // Syntax Tree Definitions
    api(project(":compiler:mina-compiler-syntax"))

    // Intermediate Language Definitions
    api(project(":compiler:mina-compiler-intermediate"))
}

testing {
    suites {
        val test by
            getting(JvmTestSuite::class) {
                dependencies {
                    // Syntax Node Generators
                    implementation(project(":compiler:mina-compiler-testing"))
                }
            }
    }
}
