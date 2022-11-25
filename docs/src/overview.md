# Overview

**Mina** is a functional programming language for the Java Virtual Machine.

It is a strictly-evaluated, statically-typed language.

## Quickstart

### Syntax

The keywords `namespace`, `import`, `data`, `let`, `if`, `then`, `else`, `match`, `with` and `case` are reserved.

`(` and `)` are delimiters for values. They are used for value parameter lists, and for applying values to functions.

`[` and `]` are delimiters for types. They are used for type parameter lists, and for applying types to type constructors.

`{` and `}` are delimiters for lexical scopes.

### Namespaces

**Mina** code is organised into `namespace`s:

```
namespace Mina/Example {}
```

**namespaces** are nothing more than containers for declarations.

### Declarations

`let` bindings can be used to declare values:

```
namespace Mina/Example {
    let one = 1
}
```

`data` declarations can be used to declare types:

```
namespace Mina/Example {
    data Choice {
        case Yes()
        case No()
    }
}
```

### Expressions

We can use literals to declare the [primitive types](https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html) of the Java virtual machine:

```
let boolean    = true  // Boolean
let int        = 1     // Int
let intSuff    = 1i    // Int with optional suffix
let long       = 1l    // Long
let double     = 1.1   // Double
let doubleSuff = 1.1d  // Double with optional suffix
let float      = 1.1f  // Float
let char       = 'a'   // Char
let string     = "abc" // String
```

We can declare functions using `->`:

```
let id    = a -> a
let const = (a, b) -> a
```

We can call functions by applying parameters within parentheses:

```
let id    = a -> a
let one   = id(1)
let const = (a, b) -> a
let two   = const(2, 1)
```

We can choose between expressions using `if`:

```
let choice = if true then 1 else 0
```

Nested declarations can be declared using blocks:

```
let outer = {
    let inner = 1
    inner
}
```

The final expression in a block denotes the return value of that block.
