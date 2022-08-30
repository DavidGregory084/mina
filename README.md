# mina

*mina* is a minimal viable functional programming language for the JVM.

Everything is currently under construction and subject to change.

The project is a multi-module Gradle project, making use of shared convention plugins in `buildSrc/`.

The project structure is as follows:

* `buildSrc/` - this contains shared Gradle build definitions
* `compiler` - this subfolder contains subprojects that implement the compiler phases.
    * `compiler/common` - this contains shared data structures that are used by all compiler phases. Many of these definitions exactly mirror those from the [Language Server Protocol](https://microsoft.github.io/language-server-protocol/). Mina is designed to integrate with LSP, but we don't want to depend on the Java LSP4J library throughout the compiler.
    * `compiler/syntax` - this contains the definition of the abstract syntax tree and utilities for working with trees.
    * `compiler/parser` - this contains the ANTLR grammar of the language and the parser for compilation units.
    * `compiler/renamer` - unimplemented; this will contain the name resolution phase for the compiler.
    * `compiler/outliner` - unimplemented; this will contain the outline type signature computation phase: we assume that well-formed user-supplied type signatures are correct, so we can start compiling definitions that depend on them earlier.
    * Missing phase folders:
        * `compiler/typechecker` - this will contain a phase for kind-checking data types and type-checking let bindings.
        * `compiler/simplifier` - this will contain a phase for lowering the user-facing syntax trees into the intermediate language.
        * `compiler/optimiser` - this will contain a phase for optimising the intermediate language.
    * `compiler/intermediate` - unimplemented; this will contain the intermediate language for the compiler optimisation phases.
    * `compiler/jvm` - unimplemented; this will contain the JVM bytecode generation phase for the compiler.
* `examples` - this contains examples of the Mina language syntax.
* `gradle/libs.versions.toml` - this is the version catalog declaring dependencies and their versions. See the Gradle documentation about [The version catalog TOML file format](https://docs.gradle.org/current/userguide/platforms.html#sub::toml-dependencies-format) for more details.
* `gradle-plugin` - mostly unimplemented; this will contain a Gradle plugin for declaring and building Mina projects.
* `lang-server` - this contains an LSP-compatible language server implemented using [lsp4j](https://github.com/eclipse/lsp4j). Since only the parsing phase of the Mina compiler is implemented, this server currently only parses source files and highlights syntactic errors.
* `proto` - unimplemented, currently nothing more than a copy of the Protobuf definitions from [DavidGregory084/inc](https://github.com/DavidGregory084/inc). This will contain Protobuf definitions for the language syntax and intermediate language. The AST of each namespace will be embedded in its Mina .class file as a custom class file attribute. The optimised intermediate language for definitions will be embedded in Mina .class files for the purposes of inlining. They will serve a similar purpose to Haskell .hi files.
* `vscode-plugin` - the VS Code plugin which interacts with the language server in `lang-server`.

## Getting started

Prerequisites:

* JDK 18+
* Node 16+

To get started:

* Build the entire project using `./gradlew build --info`. This also builds the VS Code plugin using [gradle-node-plugin](https://github.com/node-gradle/gradle-node-plugin) and publishes the language server to your local Ivy repository.
* Open [the VS Code extension file](./vscode-plugin/src/extension.ts) in VS Code and hit F5 to launch the Extension Development Host. Try opening the `examples/` folder from this project. You can examine the build server logging in the "Mina Language Server" window of the VS Code Output tab. Try making deliberate syntax errors or declaring overflowing numeric literals.

## Contributing to this project

I think it would be wise to set expectations around contributing to this project: at present it's very much incomplete, amorphous, incubating, and all of the other things that mean a broken and unfinished work in progress, and I have fairly strong opinions about what I would like to achieve.

It would likely be better to wait until the project has solidified a bit and is closer to completion if you'd like to contribute code.

Having said that, I would welcome GitHub discussions about the proposed language syntax, project structure, the name of the project, planned features, suggestions, and anything else you'd like to talk about.

## Notes

### Planned features

#### Major version shading

It's planned that the Mina compiler will have a concept of packages and package versioning, so that it can automatically shade Java packages by major version.

For example, for a namespace `mina/data/NonEmptyList`, the Java package compiled into the .class file and the group ID used in the dependency metadata will be something like `mina.v0.data`.

When declaring Mina Gradle projects, the project major version must be declared. When consuming Mina dependencies, the package major version metadata will be used in linking to the underlying .class file definitions.

This should ameliorate the pains of major version migration by allowing dependencies to continue to make use of older major package versions.

The exact details of how this will be implemented are unclear and subject to change.