# mina

[![Build status](https://badge.buildkite.com/fc749e98ce02606567678244499e97a8202999e22b0a6219fb.svg?branch=main)](https://buildkite.com/mina-lang/mina)
[![License](https://img.shields.io/badge/license-Apache--2.0-green)](https://opensource.org/licenses/Apache-2.0)

*mina* is a minimum viable functional programming language for the JVM.

Everything is currently under construction and subject to change.

The project is a multi-module Gradle project, making use of shared convention plugins in `buildSrc/`.

The project structure is as follows:

* `buildSrc/` - this contains shared Gradle build definitions
* `compiler` - this subfolder contains subprojects that implement the compiler phases.
    * `compiler/common` - this contains shared data structures that are used by all compiler phases. Many of these definitions exactly mirror those from the [Language Server Protocol](https://microsoft.github.io/language-server-protocol/). Mina is designed to integrate with LSP, but we don't want to depend on the Java LSP4J library throughout the compiler.
    * `compiler/syntax` - this contains the definition of the abstract syntax tree and utilities for working with trees.
    * `compiler/parser` - this contains the ANTLR grammar of the language and the batch-mode parsing phase for namespace definitions.
    * `compiler/renamer` - this contains the name resolution phase, which is responsible for resolving imports and attributing syntax trees with fully qualified names.
    * `compiler/typechecker` - this contains the phase responsible for kind-checking data types and type-checking let bindings.
    * `compiler/intermediate` - unimplemented; this will contain the intermediate language for the compiler optimisation phases.
    * `compiler/jvm` - this contains the JVM bytecode generation phase for the compiler, currently only implemented for unoptimised syntax trees.
    * `compiler/main` - this contains the compiler driver which is responsible for orchestrating compilation of source files.
    * `compiler/cli` - this contains the command line interface to the Mina compiler, `minac`.
* `examples` - this contains examples of the Mina language syntax.
* `gradle/libs.versions.toml` - this is the version catalog declaring dependencies and their versions. See the Gradle documentation about [The version catalog TOML file format](https://docs.gradle.org/current/userguide/platforms.html#sub::toml-dependencies-format) for more details.
* `gradle-plugin` - this contains a Gradle plugin for declaring and building Mina projects. At present it only provides tasks for compiling Mina code.
* `lang-server` - this contains an LSP-compatible language server implemented using [lsp4j](https://github.com/eclipse/lsp4j). This server currently implements text document synchronisation and build server connectivity using the [Build Server Protocol](https://build-server-protocol.github.io/), but otherwise its functionality is extremely limited.
* `proto` - unimplemented, currently nothing more than a copy of the Protobuf definitions from [DavidGregory084/inc](https://github.com/DavidGregory084/inc). This will contain Protobuf definitions for the language syntax and intermediate language. The AST of each namespace will be embedded in its Mina .class file as a custom class file attribute. The optimised intermediate language for definitions will be embedded in Mina .class files for the purposes of inlining. They will serve a similar purpose to Haskell .hi files.
* `vscode-plugin` - the VS Code plugin which interacts with the language server in `lang-server`.

## Getting started

Prerequisites:

* JDK 17+
* Node 18+

To get started:

* Build the entire project using `./gradlew build publish publishToMavenLocal --info`. This also builds the VS Code plugin using [gradle-node-plugin](https://github.com/node-gradle/gradle-node-plugin) and publishes the project artifacts to the local Ivy and Maven repositories so that they can be resolved for use by the VS Code plugin and Gradle plugin.
* Open [the VS Code extension file](./vscode-plugin/src/extension.ts) in VS Code and hit F5 to launch the Extension Development Host. Try opening the `examples/` folder from this project. You can examine the language and build server logging in the "Mina Language Server" window of the VS Code Output tab.

## Contributing to this project

I think it would be wise to set expectations around contributing to this project: at present it's very much incomplete, amorphous, incubating, and all of the other things that mean a broken and unfinished work in progress, and I have fairly strong opinions about what I would like to achieve.

It would likely be better to wait until the project has solidified a bit and is closer to completion if you'd like to contribute code.

Having said that, I would welcome discussions about the proposed language syntax, project structure, the name of the project, planned features, suggestions, and anything else you'd like to talk about.

## License

All code in this repository is licensed under the Apache License, Version 2.0. See [LICENSE](./LICENSE).