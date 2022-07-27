rootProject.name = "mina"

include("gradle-plugin")
project(":gradle-plugin").name = "mina-gradle-plugin"

// Common data structures
include("compiler:common")

// Frontend
include("compiler:syntax")
include("compiler:parser")
include("compiler:outliner")
include("compiler:renamer")

// Optimizer & code generation
include("compiler:intermediate")
include("compiler:jvm")

// Language server
include("lang-server")
